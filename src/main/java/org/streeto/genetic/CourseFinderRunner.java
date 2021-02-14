package org.streeto.genetic;

import io.jenetics.Alterer;
import io.jenetics.AnyGene;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.util.ISeq;
import io.jenetics.util.IntRange;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;
import org.streeto.Course;
import org.streeto.utils.CollectionHelpers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.streeto.utils.CollectionHelpers.*;

public class CourseFinderRunner {

    private final ControlSiteFinder csf;
    private final Sniffer callback;
    private final Alterer<AnyGene<ISeq<ControlSite>>, Double> myAlterer;

    public CourseFinderRunner(ControlSiteFinder csf, Sniffer callback) {
        this.csf = csf;
        this.callback = callback;

        myAlterer = Alterer.of(
                new ControlSiteSwapper(this.csf, Alterer.DEFAULT_ALTER_PROBABILITY),
                new CourseMutator(this.csf, Alterer.DEFAULT_ALTER_PROBABILITY)
        );
    }


    public Course run(Course initialCourse) {
        final Engine<AnyGene<ISeq<ControlSite>>, Double> engine = Engine
                .builder(new CourseFinderProblem(csf, initialCourse))
                .alterers(myAlterer)
                .build();
        Consumer<? super EvolutionResult<AnyGene<ISeq<ControlSite>>, Double>> statistics = EvolutionStatistics.ofNumber();
        var population = engine.stream()
                .limit(Limits.byFitnessThreshold(0.99))
                .limit(Limits.byExecutionTime(Duration.ofSeconds(120)))
                .limit(Limits.byFixedGeneration(100))
                .peek(statistics)
                .peek(callback)
                .collect(EvolutionResult.toBestPhenotype());

        System.out.println(statistics);

        if (population.isValid()) {
            var best = population.genotype().gene().allele().asList();
            // number the controls
            first(best).setNumber("S1");
            last(best).setNumber("F1");
            IntStream.range(1, best.size() - 1).forEach(i -> best.get(i).setNumber(String.format("%2d", i)));

            return new Course(initialCourse.getRequestedDistance(), initialCourse.getRequestedNumControls(), best);
        } else {
            return initialCourse;
        }
    }

}