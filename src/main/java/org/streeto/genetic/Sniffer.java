package org.streeto.genetic;

import io.jenetics.AnyGene;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.ISeq;
import org.streeto.ControlSite;
import org.streeto.StreetOSniffer;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Sniffer implements Consumer<EvolutionResult<AnyGene<ISeq<ControlSite>>, Double>> {
    @Override
    public void accept(EvolutionResult<AnyGene<ISeq<ControlSite>>, Double> t) {
        accept(t.generation(), t.bestFitness(), t.bestPhenotype().genotype().gene().allele().asList());
        acceptPopulation(t.generation(), t.genotypes().stream().map(geno -> geno.gene().allele().asList()).collect(Collectors.toList()));
    }

    public void acceptPopulation(long generation, List<List<ControlSite>> population) {
        // by default do nothing
    }

    abstract public void accept(long generation, double fitness, List<ControlSite> controls);

    public void acceptStatistics(EvolutionStatistics<Double, DoubleMomentStatistics> statistics) {
        acceptStatistics(statistics.toString());
    }

    abstract public void acceptStatistics(String details);
}
