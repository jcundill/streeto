package org.streeto;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.profiles.EncodedValue;
import com.graphhopper.routing.profiles.UnsignedDecimalEncodedValue;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.EncodedValueOld;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PMap;

import java.util.*;

public class StreetOFlagEncoder extends AbstractFlagEncoder {

    @Override
    public String toString() {
        return "streeto";
    }

    static final int SLOW_SPEED = 2;
    static final int MEAN_SPEED = 5;
    static final int FERRY_SPEED = 15;
    final Set<String> safeHighwayTags;
    final Set<String> allowedHighwayTags;
    final Set<String> avoidHighwayTags;
    final Map<String, Integer> hikingNetworkToCode;
    protected final HashSet<String> sidewalkValues;
    protected final HashSet<String> sidewalksNoValues;
    private DecimalEncodedValue priorityWayEncoder;
    private EncodedValueOld relationCodeEncoder;

    public StreetOFlagEncoder() {
        this(4, 1.0D);
    }

    public StreetOFlagEncoder(PMap properties) {
        this((int)properties.getLong("speedBits", 4L), properties.getDouble("speedFactor", 1.0D));
        this.properties = properties;
        this.setBlockFords(properties.getBool("block_fords", true));
    }

    public StreetOFlagEncoder(String propertiesStr) {
        this(new PMap(propertiesStr));
    }

    public StreetOFlagEncoder(int speedBits, double speedFactor) {
        super(speedBits, speedFactor, 0);
        this.safeHighwayTags = new HashSet();
        this.allowedHighwayTags = new HashSet();
        this.avoidHighwayTags = new HashSet();
        this.hikingNetworkToCode = new HashMap();
        this.sidewalkValues = new HashSet(5);
        this.sidewalksNoValues = new HashSet(5);

        restrictions.addAll(List.of("foot", "access"));
        restrictedValues.add("private");
        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("military");
        restrictedValues.add("emergency");

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

        setBlockByDefault(false);
        absoluteBarriers.add("fence");
//        potentialBarriers.add("gate")
//        potentialBarriers.add("cattle_grid")

        safeHighwayTags.add("footway");
        safeHighwayTags.add("path");
        safeHighwayTags.add("steps");
        safeHighwayTags.add("pedestrian");
        safeHighwayTags.add("living_street");
        safeHighwayTags.add("track");
        safeHighwayTags.add("residential");
        safeHighwayTags.add("platform");
        safeHighwayTags.add("cycleway");
        safeHighwayTags.add("unclassified");
        safeHighwayTags.add("road");
        // disallowed in some countries
        safeHighwayTags.add("bridleway");

        avoidHighwayTags.add("secondary");
        avoidHighwayTags.add("secondary_link");
        avoidHighwayTags.add("tertiary");
        avoidHighwayTags.add("tertiary_link");

        // for now no explicit avoiding #257
        //avoidHighwayTags.add("cycleway");
        allowedHighwayTags.addAll(safeHighwayTags);
        allowedHighwayTags.addAll(avoidHighwayTags);


        this.hikingNetworkToCode.put("iwn", PriorityCode.UNCHANGED.getValue());
        this.hikingNetworkToCode.put("nwn", PriorityCode.UNCHANGED.getValue());
        this.hikingNetworkToCode.put("rwn", PriorityCode.UNCHANGED.getValue());
        this.hikingNetworkToCode.put("lwn", PriorityCode.UNCHANGED.getValue());
        this.maxPossibleSpeed = 15;
        this.speedDefault = 5.0D;
        this.init();
    }

    public int getVersion() {
        return 5;
    }

    public void createEncodedValues(List<EncodedValue> registerNewEncodedValue, String prefix, int index) {
        super.createEncodedValues(registerNewEncodedValue, prefix, index);
        registerNewEncodedValue.add(this.speedEncoder = new UnsignedDecimalEncodedValue(EncodingManager.getKey(prefix, "average_speed"), this.speedBits, this.speedFactor, false));
        registerNewEncodedValue.add(this.priorityWayEncoder = new UnsignedDecimalEncodedValue(EncodingManager.getKey(prefix, "priority"), 3, PriorityCode.getFactor(1), false));
    }

    public int defineRelationBits(int index, int shift) {
        this.relationCodeEncoder = new EncodedValueOld("RelationCode", shift, 3, 1.0D, 0L, 7);
        return shift + this.relationCodeEncoder.getBits();
    }

    public int defineTurnBits(int index, int shift) {
        return shift;
    }

    public boolean isTurnRestricted(long flags) {
        return false;
    }

    public double getTurnCost(long flag) {
        return 0.0D;
    }

    public long getTurnFlags(boolean restricted, double costs) {
        return 0L;
    }

    public EncodingManager.Access getAccess(ReaderWay way) {
        String highwayValue = way.getTag("highway");
        if (highwayValue == null) {
            EncodingManager.Access acceptPotentially = EncodingManager.Access.CAN_SKIP;
            if (way.hasTag("route", this.ferries)) {
                String footTag = way.getTag("foot");
                if (footTag == null || this.intendedValues.contains(footTag)) {
                    acceptPotentially = EncodingManager.Access.FERRY;
                }
            }

            if (way.hasTag("railway", "platform")) {
                acceptPotentially = EncodingManager.Access.WAY;
            }

            if (way.hasTag("man_made", "pier")) {
                acceptPotentially = EncodingManager.Access.WAY;
            }

            if (!acceptPotentially.canSkip()) {
                return way.hasTag(this.restrictions, this.restrictedValues) && !this.getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way) ? EncodingManager.Access.CAN_SKIP : acceptPotentially;
            } else {
                return EncodingManager.Access.CAN_SKIP;
            }
        } else {
            String sacScale = way.getTag("sac_scale");
            if (sacScale != null && !"hiking".equals(sacScale) && !"mountain_hiking".equals(sacScale) && !"demanding_mountain_hiking".equals(sacScale) && !"alpine_hiking".equals(sacScale)) {
                return EncodingManager.Access.CAN_SKIP;
            } else if (way.hasTag("foot", this.intendedValues)) {
                return EncodingManager.Access.WAY;
            } else if (way.hasTag(this.restrictions, this.restrictedValues) && !this.getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way)) {
                return EncodingManager.Access.CAN_SKIP;
            } else if (way.hasTag("sidewalk", this.sidewalkValues)) {
                return EncodingManager.Access.WAY;
            } else if (!this.allowedHighwayTags.contains(highwayValue)) {
                return EncodingManager.Access.CAN_SKIP;
            } else if (way.hasTag("motorroad", "yes")) {
                return EncodingManager.Access.CAN_SKIP;
            } else if (!this.isBlockFords() || !way.hasTag("highway", "ford") && !way.hasTag("ford")) {
                return this.getConditionalTagInspector().isPermittedWayConditionallyRestricted(way) ? EncodingManager.Access.CAN_SKIP : EncodingManager.Access.WAY;
            } else {
                return EncodingManager.Access.CAN_SKIP;
            }
        }
    }

    public long handleRelationTags(long oldRelationFlags, ReaderRelation relation) {
        int code = 0;
        if (!relation.hasTag("route", "hiking") && !relation.hasTag("route", "foot")) {
            if (relation.hasTag("route", "ferry")) {
                code = PriorityCode.AVOID_IF_POSSIBLE.getValue();
            }
        } else {
            Integer val = this.hikingNetworkToCode.get(relation.getTag("network"));
            if (val != null) {
                code = val;
            } else {
                code = this.hikingNetworkToCode.get("lwn");
            }
        }

        int oldCode = (int)this.relationCodeEncoder.getValue(oldRelationFlags);
        return oldCode < code ? this.relationCodeEncoder.setValue(0L, code) : oldRelationFlags;
    }

    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access access, long relationFlags) {
        if (access.canSkip()) {
            return edgeFlags;
        } else {
            if (!access.isFerry()) {
                String sacScale = way.getTag("sac_scale");
                if (sacScale != null) {
                    if ("hiking".equals(sacScale)) {
                        this.speedEncoder.setDecimal(false, edgeFlags, 5.0D);
                    } else {
                        this.speedEncoder.setDecimal(false, edgeFlags, 2.0D);
                    }
                } else {
                    this.speedEncoder.setDecimal(false, edgeFlags, 5.0D);
                }

                this.accessEnc.setBool(false, edgeFlags, true);
                this.accessEnc.setBool(true, edgeFlags, true);
            } else {
                double ferrySpeed = this.getFerrySpeed(way);
                this.setSpeed(false, edgeFlags, ferrySpeed);
                this.accessEnc.setBool(false, edgeFlags, true);
                this.accessEnc.setBool(true, edgeFlags, true);
            }

            int priorityFromRelation = 0;
            if (relationFlags != 0L) {
                priorityFromRelation = (int)this.relationCodeEncoder.getValue(relationFlags);
            }

            this.priorityWayEncoder.setDecimal(false, edgeFlags, PriorityCode.getFactor(this.handlePriority(way, priorityFromRelation)));
            return edgeFlags;
        }
    }

    protected int handlePriority(ReaderWay way, int priorityFromRelation) {
        TreeMap<Double, Integer> weightToPrioMap = new TreeMap();
        if (priorityFromRelation == 0) {
            weightToPrioMap.put(0.0D, PriorityCode.UNCHANGED.getValue());
        } else {
            weightToPrioMap.put(110.0D, priorityFromRelation);
        }

        this.collect(way, weightToPrioMap);
        return weightToPrioMap.lastEntry().getValue();
    }

    void collect(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap) {
        String highway = way.getTag("highway");
        if (way.hasTag("foot", "designated")) {
            weightToPrioMap.put(100.0D, PriorityCode.PREFER.getValue());
        }
        if(way.hasTag("foot", "destination") || way.hasTag("foot", "customers") || way.hasTag("foot", "delivery")) {
            weightToPrioMap.put(100.0D, PriorityCode.AVOID_AT_ALL_COSTS.getValue());
        }

        double maxSpeed = this.getMaxSpeed(way);
        if (this.safeHighwayTags.contains(highway) || maxSpeed > 0.0D && maxSpeed <= 20.0D) {
            weightToPrioMap.put(40.0D, PriorityCode.PREFER.getValue());
            if (way.hasTag("tunnel", this.intendedValues)) {
                if (way.hasTag("sidewalk", this.sidewalksNoValues)) {
                    weightToPrioMap.put(40.0D, PriorityCode.AVOID_IF_POSSIBLE.getValue());
                } else {
                    weightToPrioMap.put(40.0D, PriorityCode.UNCHANGED.getValue());
                }
            }
        } else if ((maxSpeed > 50.0D || this.avoidHighwayTags.contains(highway)) && !way.hasTag("sidewalk", this.sidewalkValues)) {
            weightToPrioMap.put(45.0D, PriorityCode.AVOID_AT_ALL_COSTS.getValue());
        }
    }

    public boolean supports(Class<?> feature) {
        return super.supports(feature) || PriorityWeighting.class.isAssignableFrom(feature);
    }

}
