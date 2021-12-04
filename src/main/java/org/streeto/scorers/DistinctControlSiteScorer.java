package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;
import org.streeto.ControlType;
import org.streeto.StreetOPreferences;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.graphhopper.util.AngleCalc.ANGLE_CALC;
import static java.lang.Math.abs;
import static java.lang.Math.toDegrees;
import static org.streeto.utils.CollectionHelpers.dropLast;
import static org.streeto.utils.CollectionHelpers.last;

public class DistinctControlSiteScorer extends AbstractLegScorer {

    private final double junctionScoreFactor;
    private final double bendScoreFactor;
    private final ControlSiteFinder csf;
    private final double minTurnAngle;


    public DistinctControlSiteScorer(StreetOPreferences preferences, ControlSiteFinder csf) {
        super(preferences.getDistinctControlSiteWeighting());
        this.junctionScoreFactor = preferences.getJunctionScoreFactor();
        this.bendScoreFactor = preferences.getBendScoreFactor();
        this.minTurnAngle = preferences.getMinTurnAngle();
        this.csf = csf;
    }

    @Override
    public List<Double> apply(List<GHResponse> routedLegs) {
        // drop the last - the finish is where we said we wanted it - so scores 1.0
        return Stream.concat(dropLast(routedLegs, 1).stream().map(this::evaluate), Stream.of(1.0)).collect(Collectors.toList());
    }

    private double evaluate(GHResponse leg) {
        PointList currPoints = leg.getBest().getPoints();
        GHPoint location = last(currPoints);
        Optional<ControlSite> maybeSite = csf.findNearestControlSiteTo(location);
        double score = 0.0;
        if (maybeSite.isPresent()) {
            var site = maybeSite.get();
            if (site.getType() == ControlType.FURNITURE) {
                score = 1.0;
            } else if (site.getType() == ControlType.TOWER) {
                score = junctionScoreFactor;
            } else if (site.getType() == ControlType.PILLAR) {
                score = calculateTurnAngleAt(location);
            }
        }
        return scoreFunction(score);
    }

    private double calculateTurnAngleAt(GHPoint location) {
        var pl = csf.getWayGeometry(location);
        var size = pl.size();
        for (int i = 1; i < size - 1; i++) { // can't be first or last element
            var curr = pl.get(i);
            if (curr.lat == location.lat && curr.lon == location.lon) {
                var prev = pl.get(i - 1);
                var next = pl.get(i + 1);
                var angle = turnAngle(prev, location, next);
                if (angle > minTurnAngle) return bendScoreFactor;
                else return 0.0;
            }
        }
        // we did our best - reject it as we just don't understand this way geometry
        return 0.0;
    }

    private double turnAngle(GHPoint prev, GHPoint curr, GHPoint next) {
        var anglePrev = ANGLE_CALC.calcOrientation(prev.lat, prev.lon, curr.lat, curr.lon);
        var angleNext = ANGLE_CALC.calcOrientation(curr.lat, curr.lon, next.lat, next.lon);
        return abs(toDegrees(ANGLE_CALC.alignOrientation(anglePrev, angleNext) - anglePrev));
    }

}
