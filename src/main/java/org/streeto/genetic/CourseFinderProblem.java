package org.streeto.genetic;

import io.jenetics.AnyChromosome;
import io.jenetics.AnyGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;
import org.streeto.CourseSeeder;
import org.streeto.StreetOPreferences;
import org.streeto.constraints.*;

import java.util.List;
import java.util.function.Function;

import static org.streeto.CourseScorer.getOverallScore;

class CourseFinderProblem implements Problem<ISeq<ControlSite>, AnyGene<ISeq<ControlSite>>, Double> {

    private final Function<List<ControlSite>, List<Double>> legScorer;
    private final ControlSiteFinder csf;
    private final double requestedDistance;
    private final int requestedNumControls;
    private final List<ControlSite> initialControls;
    private final CourseSeeder seeder;
    private final List<CourseConstraint> constraints;

    CourseFinderProblem(Function<List<ControlSite>, List<Double>> legScorer,
                        ControlSiteFinder csf,
                        double requestedDistance,
                        int requestedNumControls,
                        List<ControlSite> initialControls,
                        StreetOPreferences preferences) {
        this.legScorer = legScorer;
        this.csf = csf;
        this.requestedDistance = requestedDistance;
        this.requestedNumControls = requestedNumControls;
        this.initialControls = initialControls;

        this.seeder = new CourseSeeder(this.csf, preferences.getPaperSize(), preferences.getMaxMapScale());

        this.constraints = List.of(
                new IsRouteableConstraint(),
                new CourseLengthConstraint(requestedDistance, preferences),
                new OnlyGoToTheFinishAtTheEndConstraint(preferences),
                new PrintableOnMapConstraint(preferences)
        );
    }

    @Override
    public Function<ISeq<ControlSite>, Double> fitness() {
        return this::courseFitness;
    }

    @Override
    public Codec<ISeq<ControlSite>, AnyGene<ISeq<ControlSite>>> codec() {
        return Codec.of(
                Genotype.of(AnyChromosome.of(this::nextRandomCourse)),
                gt -> gt.gene().allele()
        );
    }

    private ISeq<ControlSite> nextRandomCourse() {
        ISeq<ControlSite> course = null;
        boolean ok = false;
        while (!ok) {
            course = ISeq.of(seeder.chooseInitialPoints(initialControls, requestedNumControls, requestedDistance));
            var route = csf.routeRequest(course.asList());
            ok = constraints.stream().allMatch(it -> it.test(route));
        }
        return course;
    }

    private Double courseFitness(ISeq<ControlSite> controls) {
        var route = csf.routeRequest(controls.asList());
        if (!constraints.stream().allMatch(it -> it.test(route))) {
            return 0.0;
        }
        var legScores = legScorer.apply(controls.asList());
        return getOverallScore(legScores);
    }


}

