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
import org.streeto.constraints.CourseConstraint;
import org.streeto.constraints.PrintableOnMapConstraint;
import org.streeto.scorers.ControlSeparationScorer;
import org.streeto.scorers.ControlSetScorer;
import org.streeto.scorers.ScatterTspScorer;
import org.streeto.scorers.StartNearTheCentreScorer;

import java.util.List;
import java.util.function.Function;

import static java.lang.Math.pow;

class ScatterFinderProblem implements Problem<ISeq<ControlSite>, AnyGene<ISeq<ControlSite>>, Double> {

    private final ControlSiteFinder csf;
    private final double requestedDistance;
    private final int requestedNumControls;
    private final List<ControlSite> initialControls;
    private final CourseSeeder seeder;
    private final List<CourseConstraint> constraints;
    private final ControlSetScorer separationScorer;
    private final int totalControls;
    private final int iterations;
    private final ControlSetScorer startNearTheCentreScorer;
    private final ControlSetScorer scatterTspScorer;

    ScatterFinderProblem(ControlSiteFinder csf,
                         double requestedDistance,
                         int totalControls,
                         int requestedNumControls,
                         int iterations,
                         List<ControlSite> initialControls,
                         StreetOPreferences preferences) {
        this.totalControls = totalControls;
        this.iterations = iterations;
        this.csf = csf;
        this.requestedDistance = requestedDistance;
        this.requestedNumControls = requestedNumControls;
        this.initialControls = initialControls;
        this.separationScorer = new ControlSeparationScorer(preferences);
        this.startNearTheCentreScorer = new StartNearTheCentreScorer(preferences);
        this.scatterTspScorer = new ScatterTspScorer(preferences, csf, requestedNumControls, requestedDistance, iterations);

        this.seeder = new CourseSeeder(this.csf, preferences.getPaperSize(), preferences.getMaxMapScale());

        this.constraints = List.of(
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
            course = ISeq.of(seeder.chooseInitialPoints(initialControls, totalControls, requestedDistance * totalControls / requestedNumControls));
            var route = csf.routeRequest(course.asList());
            ok = constraints.stream().allMatch(it -> it.test(route));
        }
        return course;
    }

    private Double courseFitness(ISeq<ControlSite> controls) {
        if (controls.size() < requestedNumControls) {
            return 0.0;
        }
        var route = csf.routeRequest(controls.asList());
        if (!constraints.stream().allMatch(it -> it.test(route))) {
            return 0.0;
        }
        var separationScore = separationScorer.score(controls.asList());
        var startNearTheCentreScore = startNearTheCentreScorer.score(controls.asList());
        var tspScore = scatterTspScorer.score(controls.asList());
        return (pow(separationScore, 2) + pow(startNearTheCentreScore, 2) + pow(tspScore, 2)) / 3.0;
    }


}

