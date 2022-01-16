package org.streeto.genetic;

import io.jenetics.Alterer;
import io.jenetics.AnyGene;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.ISeq;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;
import org.streeto.StreetOPreferences;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ScatterFinderRunner {
    private final Function<List<ControlSite>, List<Double>> legScorer;
    private final ControlSiteFinder csf;
    private final List<Sniffer> callbacks;
    private final StreetOPreferences preferences;
    private final Alterer<AnyGene<ISeq<ControlSite>>, Double> myAlterer;

    public ScatterFinderRunner(Function<List<ControlSite>, List<Double>> legScorer, ControlSiteFinder csf, List<Sniffer> callbacks, StreetOPreferences preferences) {
        this.legScorer = legScorer;
        this.csf = csf;
        this.callbacks = callbacks;
        this.preferences = preferences;

        myAlterer = Alterer.of(
                new ControlSiteSwapper(preferences.getSwapProbability()),
                new CourseMutator(this.csf, preferences.getMutateProbability(), preferences.getMutationRadius())
        );
    }

    public Optional<List<ControlSite>> run(double requestedDistance, int requestedNumControls, List<ControlSite> initialControls) {

        final Engine<AnyGene<ISeq<ControlSite>>, Double> engine = Engine
                .builder(new ScatterFinderProblem(legScorer, csf, requestedDistance, requestedNumControls, initialControls, preferences))
                .alterers(myAlterer)
                .offspringFraction(preferences.getOffspringFraction())
                .populationSize(preferences.getPopulationSize())
                .maximalPhenotypeAge(preferences.getMaxPhenotypeAge())
                .build();
        EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();
        var population = engine.stream()
                .limit(Limits.byFitnessThreshold(preferences.getStoppingFitness()))
                .limit(Limits.byExecutionTime(Duration.ofSeconds(preferences.getMaxExecutionTime())))
                .limit(Limits.byFixedGeneration(preferences.getMaxGenerations()))
                .limit(StopOnFlagLimit.instance())
                .peek(statistics)
                .peek(this::callEveryoneBack)
                .collect(EvolutionResult.toBestPhenotype());
        callbacks.forEach(it -> it.acceptStatistics(statistics));
        if (population.isValid()) {
            return Optional.of(population.genotype().gene().allele().asList());
        } else {
            return Optional.empty();
        }
    }


    private void callEveryoneBack(EvolutionResult<AnyGene<ISeq<ControlSite>>, Double> evolutionResult) {
        callbacks.forEach(it -> it.accept(evolutionResult));
    }
}