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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

class CourseFinderProblem implements Problem<ISeq<ControlSite>, AnyGene<ISeq<ControlSite>>, Double> {

    private final Function<List<ControlSite>, List<Double>> legScorer;
    private final ControlSiteFinder csf;
    private final Course initialCourse;
//    private final CourseScorer courseScorer;
    private final CourseSeeder seeder;
    private final List<CourseConstraint> constraints;
    private final Map<Integer, Boolean> validatedSet = new HashMap<>();

    CourseFinderProblem(Function<List<ControlSite>,List<Double>> legScorer, ControlSiteFinder csf, Course initialCourse) {
        this.legScorer = legScorer;
        this.csf = csf;
        this.initialCourse = initialCourse;
 //       this.courseScorer = new CourseScorer(featureScorers, csf::findRoutes);
        this.seeder = new CourseSeeder(this.csf);

        this.constraints = List.of(
                new IsRouteableConstraint(),
                new CourseLengthConstraint(initialCourse.distance()),
                new PrintableOnMapConstraint(),
                new LastControlNearTheFinishConstraint(),
                new DidntMoveConstraint(),
                new OnlyGoToTheFinishAtTheEndConstraint()
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

    @Override
    public Optional<Constraint<AnyGene<ISeq<ControlSite>>, Double>> constraint() {
        return Optional.of(RetryConstraint.of(this::courseValidator));
    }

    private boolean courseValidator(Phenotype<AnyGene<ISeq<ControlSite>>, Double> pt) {
        var controls = pt.genotype().gene().allele();
        var hashCode = controls.hashCode();
        if (!validatedSet.containsKey(hashCode)) {
            var route = csf.routeRequest(controls.asList());
            var ok = constraints.stream().allMatch(it -> it.valid(route));
            validatedSet.put(hashCode, ok);
        }
        return validatedSet.get(hashCode);
    }

    private ISeq<ControlSite> nextRandomCourse() {
        return ISeq.of(seeder.chooseInitialPoints(initialCourse.getControls(), initialCourse.getRequestedNumControls(), initialCourse.getRequestedDistance()));
    }

    private Double courseFitness(ISeq<ControlSite> controls) {
        var legScores = legScorer.apply(controls.asList());
        var average = legScores.stream().mapToDouble(x -> x).average().orElseThrow();
        return 1.0 - average;
    }


}

