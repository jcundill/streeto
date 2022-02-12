package org.streeto.genetic;

import io.jenetics.AnyChromosome;
import io.jenetics.AnyGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;
import org.streeto.*;
import org.streeto.constraints.CourseConstraint;
import org.streeto.constraints.PrintableOnMapConstraint;
import org.streeto.scorers.*;
import org.streeto.tsp.OrienteeringProblemSolver;

import java.util.List;
import java.util.function.Function;

import static java.lang.Math.abs;
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
    private final CourseScorer courseScorer;
    private final ControlSpreadScorer spreadScorer;

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
        this.spreadScorer = new ControlSpreadScorer(preferences);

        this.seeder = new CourseSeeder(this.csf, preferences.getPaperSize(), preferences.getMaxMapScale());

        this.constraints = List.of(
                new PrintableOnMapConstraint(preferences)
        );

        courseScorer = new CourseScorer(preferences, csf);

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
        var result = new OrienteeringProblemSolver(csf).solve(controls.asList(), requestedDistance, iterations);
        var distance = result.distance();
        var maxScore = controls.stream().mapToInt(ControlSite::getValue).sum();
        var scoreRatio = (result.score() * 1.0) / (maxScore * 1.0);
        var tspScore = (1.0 - abs(distance - requestedDistance) / requestedDistance) * scoreRatio;

        var path = result.path();
        if (path.size() < requestedNumControls) {
            return 0.0;
        }
        var route = csf.routeRequest(path);
        if (!constraints.stream().allMatch(it -> it.test(route))) {
            return 0.0;
        }
        var separationScore = separationScorer.score(controls.asList());
        //var startNearTheCentreScore = startNearTheCentreScorer.score(controls.asList());
        var legScores = courseScorer.score(path).getOverallScore();
        var spreadScore = spreadScorer.score(controls.asList());
        return (pow(tspScore, 2) + pow(separationScore, 2) + pow(spreadScore, 2) + pow(legScores, 2)) / 4.0;
    }


}

