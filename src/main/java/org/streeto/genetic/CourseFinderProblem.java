package org.streeto.genetic;

import io.jenetics.AnyChromosome;
import io.jenetics.AnyGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Constraint;
import io.jenetics.engine.Problem;
import io.jenetics.engine.RetryConstraint;
import io.jenetics.util.ISeq;
import org.streeto.*;
import org.streeto.constraints.*;
import org.streeto.scorers.*;
import java.util.*;
import java.util.function.Function;

class CourseFinderProblem implements Problem<ISeq<ControlSite>, AnyGene<ISeq<ControlSite>>, Double> {

    private final ControlSiteFinder csf;
    private final Course initialCourse;
    private final CourseScorer courseScorerJava;
    private final CourseSeeder seeder;
    private final List<CourseConstraint> constraints;
    private final Map<Integer, Boolean> validatedSet = new HashMap<>();

    CourseFinderProblem(ControlSiteFinder csf, Course initialCourse) {
        this.csf = csf;
        this.initialCourse = initialCourse;
        this.constraints = List.of(
            new IsRouteableConstraint(),
            new CourseLengthConstraint(initialCourse.distance()),
            new PrintableOnMapConstraint(),
            new LastControlNearTheFinishConstraint(),
            new DidntMoveConstraint(),
            new OnlyGoToTheFinishAtTheEndConstraint()
        );
        List<LegScorer> featureScorersJava = List.of(
                new LegLengthScorer(),
                new LegComplexityScorer(),
                new LegRouteChoiceScorer(),
                new BeenThisWayBeforeScorer(),
                new ComingBackHereLaterScorer(),
                new DogLegScorer()
        );
        this.courseScorerJava = new CourseScorer(featureScorersJava, csf::findRoutes);
        this.seeder = new CourseSeeder(this.csf);
    }

    @Override
    public Codec<ISeq<ControlSite>, AnyGene<ISeq<ControlSite>>> codec() {
        return Codec.of(
                Genotype.of(AnyChromosome.of(this::nextRandomCourse)),
                gt -> gt.gene().allele()
        );
    }

    @Override
    public Function<ISeq<ControlSite>, Double> fitness()  {
        return this::courseFitness;
    }

    @Override
    public Optional<Constraint<AnyGene<ISeq<ControlSite>>, Double>> constraint() {
        return Optional.of(RetryConstraint.of(this::courseValidator));
    }

    private boolean courseValidator(Phenotype<AnyGene<ISeq<ControlSite>>, Double> pt) {
        var controls = pt.genotype().gene().allele();
        var hashCode = controls.hashCode();
        if( !validatedSet.containsKey(hashCode)) {
            var route = csf.routeRequest(controls.asList());
            var ok = constraints.stream().allMatch(it -> it.valid(route) );
            validatedSet.put(hashCode, ok);
        }
        return validatedSet.get(hashCode);
     }

    private ISeq<ControlSite> nextRandomCourse() {
        return ISeq.of(seeder.chooseInitialPoints(initialCourse.getControls(), initialCourse.getRequestedNumControls(), initialCourse.getRequestedDistance()));
     }

    private Double courseFitness(ISeq<ControlSite> controls) {
        var legScores = courseScorerJava.scoreLegs(controls.asList());
        var average = legScores.stream().mapToDouble(x -> x).average().orElseThrow();
        return 1.0 - average;
    }


}

