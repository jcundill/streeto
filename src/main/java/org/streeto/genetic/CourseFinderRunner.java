package org.streeto.genetic;

import io.jenetics.Alterer;
import io.jenetics.AnyGene;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.util.ISeq;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;
import org.streeto.Course;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import static org.streeto.utils.CollectionHelpers.*;

public class CourseFinderRunner {
    private final Function<List<ControlSite>, List<Double>> legScorer;
    private final ControlSiteFinder csf;
    private final Sniffer callback;
    private final Alterer<AnyGene<ISeq<ControlSite>>, Double> myAlterer;

    public CourseFinderRunner(Function<List<ControlSite>, List<Double>> legScorer, ControlSiteFinder csf, Sniffer callback) {
        this.legScorer = legScorer;
        this.csf = csf;
        this.callback = callback;

        myAlterer = Alterer.of(
                new ControlSiteSwapper(Alterer.DEFAULT_ALTER_PROBABILITY),
                new CourseMutator(this.csf, Alterer.DEFAULT_ALTER_PROBABILITY)
        );
    }

    public Course run(Course initialCourse) {
        final Engine<AnyGene<ISeq<ControlSite>>, Double> engine = Engine
                .builder(new CourseFinderProblem(legScorer, csf, initialCourse))
                .alterers(myAlterer)
                .build();
        var population = engine.stream()
                .limit(Limits.byFitnessThreshold(0.99))
                .limit(Limits.byExecutionTime(Duration.ofSeconds(120)))
                .limit(Limits.byFixedGeneration(100))
                .peek(EvolutionStatistics.ofNumber())
                .peek(callback)
                .collect(EvolutionResult.toBestPhenotype());
        if (population.isValid()) {
            var best = population.genotype().gene().allele().asList();
            // number the controls
            formatNumber(first(best), "S1");
            forEachIndexed(dropFirstAndLast(best, 1), (i, ctrl) -> formatNumber(ctrl, String.format("%d", i+1)));
            formatNumber(last(best), "F1");

            return new Course(initialCourse.getRequestedDistance(), initialCourse.getRequestedNumControls(), best);
        } else {
            return initialCourse;
        }
    }

    private void formatNumber(ControlSite controlSite, String format) {
        controlSite.setNumber(format);
    }
}