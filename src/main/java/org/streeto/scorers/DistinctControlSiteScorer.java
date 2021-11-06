package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.streeto.ControlSiteFinder;
import org.streeto.StreetOPreferences;
import org.streeto.utils.CollectionHelpers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.graphhopper.util.AngleCalc.ANGLE_CALC;
import static java.lang.Math.abs;
import static java.lang.Math.toDegrees;
import static org.streeto.ControlSiteFinder.ControlType;
import static org.streeto.utils.CollectionHelpers.first;
import static org.streeto.utils.CollectionHelpers.last;

public class DistinctControlSiteScorer extends AbstractLegScorer {

    private final double junctionScoreFactor;
    private final ControlSiteFinder csf;
    private final double minTurnAngle;


    public DistinctControlSiteScorer(StreetOPreferences preferences, ControlSiteFinder csf) {
        super(preferences.getDistinctControlSiteWeighting());
        this.junctionScoreFactor = preferences.getJunctionScoreFactor();
        this.minTurnAngle = preferences.getMinTurnAngle();
        this.csf = csf;
    }

    @Override
    public List<Double> apply(List<GHResponse> routedLegs) {
        return Stream.concat(CollectionHelpers.windowed(routedLegs, 2)
                        .map(this::evaluate), Stream.of(1.0))
                .collect(Collectors.toList());
    }

    private double evaluate(List<GHResponse> currAndNext) {
        PointList currPoints = first(currAndNext).getBest().getPoints();
        PointList nextPoints = last(currAndNext).getBest().getPoints();
        if (nextPoints.size() < 2 || currPoints.size() < 3)
            return 0.0;

        GHPoint location = last(currPoints);
        ControlType controlType = csf.getFeatureAtLocation(location);
        if (controlType == ControlType.FURNITURE)
            return 1.0;
        else if (controlType == ControlType.TOWER)
            return junctionScoreFactor;
        else {
            GHPoint before = currPoints.get(currPoints.size() - 2);
            GHPoint after = nextPoints.get(1);//csf.getNextEdgePoint(before, location);
            if (Objects.equals(before, after)) {
                //dog leg - score the previous point
                before = currPoints.get(currPoints.size() - 3);
                location = currPoints.get(currPoints.size() - 2);
                after = currPoints.get(currPoints.size() - 1);
            }
            var angle = turnAngle(before, location, after); // 0 straight line, 180 u turn
            if (angle > minTurnAngle) return 1.0;
            else return angle / 180.0;
        }
    }

    private double turnAngle(GHPoint prev, GHPoint curr, GHPoint next) {
        var anglePrev = ANGLE_CALC.calcOrientation(prev.lat, prev.lon, curr.lat, curr.lon);
        var angleNext = ANGLE_CALC.calcOrientation(curr.lat, curr.lon, next.lat, next.lon);
        return abs(toDegrees(ANGLE_CALC.alignOrientation(anglePrev, angleNext) - anglePrev));
    }

}
