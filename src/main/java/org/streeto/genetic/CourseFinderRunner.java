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

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CourseFinderRunner {
    private final Function<List<ControlSite>, List<Double>> legScorer;
    private final ControlSiteFinder csf;
    private final List<Sniffer> callbacks;
    private final Alterer<AnyGene<ISeq<ControlSite>>, Double> myAlterer;

    public CourseFinderRunner(Function<List<ControlSite>, List<Double>> legScorer, ControlSiteFinder csf, List<Sniffer> callbacks) {
        this.legScorer = legScorer;
        this.csf = csf;
        this.callbacks = callbacks;

        myAlterer = Alterer.of(
                new ControlSiteSwapper(Alterer.DEFAULT_ALTER_PROBABILITY),
                new CourseMutator(this.csf, Alterer.DEFAULT_ALTER_PROBABILITY)
        );
    }

    public Optional<List<ControlSite>> run(double requestedDistance, int requestedNumControls, List<ControlSite> initialControls) {
        final Engine<AnyGene<ISeq<ControlSite>>, Double> engine = Engine
                .builder(new CourseFinderProblem(legScorer, csf, requestedDistance, requestedNumControls,initialControls))
                .alterers(myAlterer)
                .build();
        EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();
        var population = engine.stream()
                .limit(Limits.byFitnessThreshold(0.99))
                .limit(Limits.byExecutionTime(Duration.ofSeconds(120)))
                .limit(Limits.byFixedGeneration(100))
                .peek(statistics)
                .peek(this::callEveryoneBack)
                .collect(EvolutionResult.toBestPhenotype());
        callbacks.forEach( it -> it.acceptStatistics(statistics));
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