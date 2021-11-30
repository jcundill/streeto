package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.Instruction;
import org.jetbrains.annotations.NotNull;
import org.streeto.StreetOPreferences;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static org.streeto.utils.CollectionHelpers.first;
import static org.streeto.utils.CollectionHelpers.last;
import static org.streeto.utils.DistUtils.dist;

public class LegComplexityScorer extends AbstractLegScorer {
    private final double turnDensity;

    private static final List<Integer> turnInstructions = List.of(
            Instruction.TURN_LEFT,
            Instruction.TURN_RIGHT,
            Instruction.TURN_SLIGHT_LEFT,
            Instruction.TURN_SLIGHT_RIGHT,
            Instruction.TURN_SHARP_LEFT,
            Instruction.TURN_SHARP_RIGHT,
            Instruction.KEEP_LEFT,
            Instruction.KEEP_RIGHT,
            Instruction.U_TURN_LEFT,
            Instruction.U_TURN_RIGHT,
            Instruction.U_TURN_UNKNOWN,
            Instruction.LEAVE_ROUNDABOUT
    );

    public LegComplexityScorer(StreetOPreferences preferences) {
        super(preferences.getLegComplexityWeighting());
        this.turnDensity = preferences.getTurnDensity();
    }

    private double evaluate(GHResponse leg) {
        var instructions = leg.getBest().getInstructions();

        var turns = instructions.stream()
                .filter(it -> turnInstructions.contains(it.getSign()))
                .count();
        var points = leg.getBest().getPoints();
        if (leg.getBest().getDistance() == 0.0) return 0.0; //in the same place - not complex at all
        if (turns == 0.0) return 0.0; // no decisions - not complex at all
        var legTurnDensity = turnDensity * turns / leg.getBest().getDistance();
        return min(legTurnDensity, 1.0);
    }

    /**
     * scores each numbered control based on the complexity of the route to that control.
     * i.e. control 2 is in a bad place as the route from 1 to 2 was too direct
     */
    @NotNull
    @Override
    public List<Double> apply(List<GHResponse> routedLegs) {
        return routedLegs.stream()
                .map(this::evaluate)
                .collect(Collectors.toList());
    }
}

