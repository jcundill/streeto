package org.streeto;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.util.spatialrules.TransportationMode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PMap;

import java.util.*;

import static com.graphhopper.routing.util.EncodingManager.getKey;
import static com.graphhopper.routing.util.PriorityCode.*;
import static com.graphhopper.routing.util.spatialrules.TransportationMode.FOOT;

/**
 * Defines bit layout for pedestrians (speed, access, surface, ...). Here we put a penalty on unsafe
 * roads only. If you wish to also prefer routes due to beauty like hiking routes use the
 * HikeFlagEncoder instead.
 * <p>
 *
 * @author Peter Karich
 * @author Nop
 * @author Karl Hübner
 */
public class StreetOFlagEncoder extends AbstractFlagEncoder {
    static final int SLOW_SPEED = 2;
    static final int MEAN_SPEED = 5;
    static final int FERRY_SPEED = 15;
    final Set<String> safeHighwayTags = new HashSet<>();
    final Set<String> allowedHighwayTags = new HashSet<>();
    final Set<String> avoidHighwayTags = new HashSet<>();
    final Set<String> allowedSacScale = new HashSet<>();
    protected final HashSet<String> sidewalkValues = new HashSet<>(5);
    protected final HashSet<String> sidewalksNoValues = new HashSet<>(5);
    protected boolean speedTwoDirections = false;
    private DecimalEncodedValue priorityWayEncoder;

    public StreetOFlagEncoder() {
        this(4, 1);
    }

    public StreetOFlagEncoder(PMap properties) {
        this(properties.getInt("speed_bits", 4), properties.getDouble("speed_factor", 1));

        blockPrivate(true);
        blockFords(false);
        blockBarriersByDefault(false);
    }

    protected StreetOFlagEncoder(int speedBits, double speedFactor) {
        super(speedBits, speedFactor, 0);
        restrictions.addAll(Arrays.asList("foot", "access"));

        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("military");
        restrictedValues.add("emergency");
        restrictedValues.add("private");

        intendedValues.add("yes");
        intendedValues.add("designated");
        intendedValues.add("official");
        intendedValues.add("permissive");

        sidewalksNoValues.add("no");
        sidewalksNoValues.add("none");
        // see #712
        sidewalksNoValues.add("separate");

        sidewalkValues.add("yes");
        sidewalkValues.add("both");
        sidewalkValues.add("left");
        sidewalkValues.add("right");

        blockBarriersByDefault(false);
        absoluteBarriers.add("fence");
        potentialBarriers.add("gate");
        potentialBarriers.add("cattle_grid");

        safeHighwayTags.add("footway");
        safeHighwayTags.add("path");
        safeHighwayTags.add("steps");
        safeHighwayTags.add("pedestrian");
        safeHighwayTags.add("living_street");
        safeHighwayTags.add("track");
        safeHighwayTags.add("residential");
        safeHighwayTags.add("service");
        safeHighwayTags.add("platform");

        avoidHighwayTags.add("trunk");
        avoidHighwayTags.add("trunk_link");
        avoidHighwayTags.add("primary");
        avoidHighwayTags.add("primary_link");
        avoidHighwayTags.add("secondary");
        avoidHighwayTags.add("secondary_link");
        avoidHighwayTags.add("tertiary");
        avoidHighwayTags.add("tertiary_link");

        // for now no explicit avoiding #257
        //avoidHighwayTags.add("cycleway");
        allowedHighwayTags.addAll(safeHighwayTags);
        allowedHighwayTags.addAll(avoidHighwayTags);
        allowedHighwayTags.add("cycleway");
        allowedHighwayTags.add("unclassified");
        allowedHighwayTags.add("road");
        // disallowed in some countries
        allowedHighwayTags.add("bridleway");

        allowedSacScale.add("hiking");
        allowedSacScale.add("mountain_hiking");
        allowedSacScale.add("demanding_mountain_hiking");

        maxPossibleSpeed = FERRY_SPEED;
        speedDefault = MEAN_SPEED;
    }

    @Override
    public int getVersion() {
        return 5;
    }

    @Override
    public TransportationMode getTransportationMode() {
        return FOOT;
    }

    @Override
    public void createEncodedValues(List<EncodedValue> registerNewEncodedValue, String prefix, int index) {
        // first two bits are reserved for route handling in superclass
        super.createEncodedValues(registerNewEncodedValue, prefix, index);
        // larger value required - ferries are faster than pedestrians
        registerNewEncodedValue.add(avgSpeedEnc = new UnsignedDecimalEncodedValue(getKey(prefix, "average_speed"), speedBits, speedFactor, speedTwoDirections));
        registerNewEncodedValue.add(priorityWayEncoder = new UnsignedDecimalEncodedValue(getKey(prefix, "priority"), 3, PriorityCode.getFactor(1), speedTwoDirections));

        //footRouteEnc = getEnumEncodedValue(RouteNetwork.key("foot"), RouteNetwork.class);
    }

    /**
     * Some ways are okay but not separate for pedestrians.
     */
    @Override
    public EncodingManager.Access getAccess(ReaderWay way) {
        String highwayValue = way.getTag("highway");
        if (highwayValue == null) {
            EncodingManager.Access acceptPotentially = EncodingManager.Access.CAN_SKIP;

            if (way.hasTag("route", ferries)) {
                return EncodingManager.Access.CAN_SKIP; // haven't got time to get a ferry
            }

            // special case not for all acceptedRailways, only platform
            if (way.hasTag("railway", "platform"))
                acceptPotentially = EncodingManager.Access.WAY;

            if (way.hasTag("man_made", "pier"))
                acceptPotentially = EncodingManager.Access.WAY;

            if (!acceptPotentially.canSkip()) {
                if (way.hasTag(restrictions, restrictedValues) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way))
                    return EncodingManager.Access.CAN_SKIP;
                return acceptPotentially;
            }

            return EncodingManager.Access.CAN_SKIP;
        }

        // commented out, will run up anything
        // other scales are too dangerous, see http://wiki.openstreetmap.org/wiki/Key:sac_scale
//        if (way.getTag("sac_scale") != null && !way.hasTag("sac_scale", allowedSacScale))
//            return EncodingManager.Access.CAN_SKIP;

        // no need to evaluate ferries or fords - already included here
        if (way.hasTag("foot", intendedValues))
            return EncodingManager.Access.WAY;

        // check access restrictions
        if (way.hasTag(restrictions, restrictedValues) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way))
            return EncodingManager.Access.CAN_SKIP;

        if (way.hasTag("sidewalk", sidewalkValues))
            return EncodingManager.Access.WAY;

        if (!allowedHighwayTags.contains(highwayValue))
            return EncodingManager.Access.CAN_SKIP;

        if (way.hasTag("motorroad", "yes"))
            return EncodingManager.Access.CAN_SKIP;

        // do not get our feet wet, "yes" is already included above
        if (way.hasTag("highway", "ford") || way.hasTag("ford"))
            return EncodingManager.Access.WAY;

        if (getConditionalTagInspector().isPermittedWayConditionallyRestricted(way))
            return EncodingManager.Access.CAN_SKIP;

        return EncodingManager.Access.WAY;
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access access) {
        if (access.canSkip())
            return edgeFlags;

        int priorityFromRelation = UNCHANGED.getValue();
        accessEnc.setBool(false, edgeFlags, true);
        accessEnc.setBool(true, edgeFlags, true);
        if (!access.isFerry()) {
            String sacScale = way.getTag("sac_scale");
            if (sacScale != null) {
                setSpeed(edgeFlags, "hiking".equals(sacScale) ? MEAN_SPEED : SLOW_SPEED);
            } else {
                setSpeed(edgeFlags, way.hasTag("highway", "steps") ? MEAN_SPEED - 2 : MEAN_SPEED);
            }
        } else {
            priorityFromRelation = PriorityCode.AVOID_IF_POSSIBLE.getValue();
            double ferrySpeed = getFerrySpeed(way);
            setSpeed(edgeFlags, ferrySpeed);
        }

        priorityWayEncoder.setDecimal(false, edgeFlags, PriorityCode.getFactor(handlePriority(way, priorityFromRelation)));
        return edgeFlags;
    }

    void setSpeed(IntsRef edgeFlags, double speed) {
        if (speed > getMaxSpeed())
            speed = getMaxSpeed();
        avgSpeedEnc.setDecimal(false, edgeFlags, speed);
        if (speedTwoDirections)
            avgSpeedEnc.setDecimal(true, edgeFlags, speed);
    }

    int handlePriority(ReaderWay way, Integer priorityFromRelation) {
        TreeMap<Double, Integer> weightToPrioMap = new TreeMap<>();
        if (priorityFromRelation == null)
            weightToPrioMap.put(0d, UNCHANGED.getValue());
        else
            weightToPrioMap.put(110d, priorityFromRelation);

        collect(way, weightToPrioMap);

        // pick priority with biggest order value
        return weightToPrioMap.lastEntry().getValue();
    }

    /**
     * @param weightToPrioMap associate a weight with every priority. This sorted map allows
     *                        subclasses to 'insert' more important priorities as well as overwrite determined priorities.
     */

    void collect(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap) {
        String highway = way.getTag("highway");
        if (way.hasTag("foot", "designated")) {
            weightToPrioMap.put(100.0D, PriorityCode.UNCHANGED.getValue());
        }
        if (way.hasTag("foot", "destination") || way.hasTag("foot", "customers") || way.hasTag("foot", "delivery")) {
            weightToPrioMap.put(100.0D, PriorityCode.AVOID_AT_ALL_COSTS.getValue());
        }

        double maxSpeed = this.getMaxSpeed(way);
        if (this.safeHighwayTags.contains(highway) || maxSpeed > 0.0D && maxSpeed <= 20.0D) {
            if (way.hasTag("tunnel", this.intendedValues)) {
                if (way.hasTag("sidewalk", this.sidewalksNoValues)) {
                    weightToPrioMap.put(40.0D, PriorityCode.AVOID_AT_ALL_COSTS.getValue());
                } else {
                    weightToPrioMap.put(40.0D, PriorityCode.UNCHANGED.getValue());
                }
            }
        } else if ((maxSpeed > 50.0D || this.avoidHighwayTags.contains(highway)) && !way.hasTag("sidewalk", this.sidewalkValues)) {
            weightToPrioMap.put(45.0D, PriorityCode.AVOID_AT_ALL_COSTS.getValue());
        }
    }


    @Override
    public boolean supports(Class<?> feature) {
        if (super.supports(feature))
            return true;

        return PriorityWeighting.class.isAssignableFrom(feature);
    }

    @Override
    public String toString() {
        return "streeto";
    }
}
