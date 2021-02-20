package org.streeto.genetic;

import io.jenetics.AnyGene;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.ISeq;
import org.streeto.ControlSite;

import java.util.function.Consumer;

public class Sniffer implements Consumer<EvolutionResult<AnyGene<ISeq<ControlSite>>, Double>> {
    @Override
    public void accept(EvolutionResult<AnyGene<ISeq<ControlSite>>, Double> t) {
        var stats = String.format("Generation: %d, Best: %f, Altered: %d, Invalid: %d",
                t.generation(), t.bestFitness(), t.alterCount(), t.invalidCount());
        System.out.println(stats);
    }

    public void acceptStatistics(EvolutionStatistics<Double, DoubleMomentStatistics> statistics) {
        System.out.println(statistics.toString());
    }
}
