package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.Instruction;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.streeto.utils.CollectionHelpers.streamFromIterable;

public class LegComplexityScorer extends AbstractLegScorer {

    private static final List<Integer> turnInstructions = List.of(
            Instruction.TURN_LEFT,
            Instruction.TURN_RIGHT,
            Instruction.TURN_SHARP_LEFT,
            Instruction.TURN_SHARP_RIGHT,
            Instruction.TURN_SLIGHT_LEFT,
            Instruction.TURN_SLIGHT_RIGHT,
            Instruction.U_TURN_LEFT,
            Instruction.U_TURN_RIGHT,
            Instruction.U_TURN_UNKNOWN,
            Instruction.LEAVE_ROUNDABOUT
    );


    private static double evaluate(GHResponse leg) {
        var instructions = leg.getBest().getInstructions();

        var turns = streamFromIterable(instructions).filter(it -> turnInstructions.contains(it.getSign())).count();
        var turnDensity = 1000.0 * turns / leg.getBest().getDistance();   // turns per K

        double result = 1.0;
        if (turnDensity > 8.0) {
            result = 0.0;
        } else if (turnDensity > 4.0) {
            result = 0.25;
        } //- (turns.toDouble() / num.toDouble())

        return result;
    }

    /**
     * scores each numbered control based on the complexity of the route to that control.
     * i.e. control 2 is in a bad place as the route from 1 to 2 was too direct
     */
    @NotNull
    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
        return StreamEx.of(routedLegs.stream().map(LegComplexityScorer::evaluate)).toList();
    }

}

