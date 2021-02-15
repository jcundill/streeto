package org.streeto;

import io.jenetics.AnyGene;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.ISeq;
import org.streeto.genetic.CourseFinderRunner;
import org.streeto.genetic.Sniffer;

public class TestJava {

    public static void main(String[] args) {
        var osmosys = new StreetO("derbyshire-latest");
        var initialCourse = Course.buildFromProperties("./streeto.properties");
//        val initialCourse = Course.buildFromProperties("./streeto.properties")
        var lastMondayRunner = new CourseFinderRunner(osmosys.getCsf(), new Sniffer());
        var course = lastMondayRunner.run(initialCourse);
        //val course = courses.first()
        var scoredCourse =
                osmosys.score(new Course(initialCourse.distance(), initialCourse.getRequestedNumControls(), course.getControls()));
        System.out.printf("best score: %f%n", 1.0 - scoredCourse.getEnergy());
        System.out.printf("distance: %f%n", scoredCourse.getRoute().getDistance());

    }

    static class JavaSniffer extends Sniffer {

        @Override
        public void accept(EvolutionResult<AnyGene<ISeq<ControlSite>>, Double> t) {
            // println("Generation: ${t.generation()}, Best: ${t.bestFitness()}, Altered: ${t.alterCount()}, Invalid: ${t.invalidCount()}")
            var stats = String.format("Generation: %d, Best: %f, Altered: %d, Invalid: %d",
                    t.generation(), t.bestFitness(), t.alterCount(), t.invalidCount());
            System.out.println(stats);
        }
    }
}
