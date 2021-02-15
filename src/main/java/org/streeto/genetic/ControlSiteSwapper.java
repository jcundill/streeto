package org.streeto.genetic;

import io.jenetics.AnyGene;
import io.jenetics.UniformCrossover;
import io.jenetics.util.ISeq;
import io.jenetics.util.MSeq;
import io.jenetics.util.RandomRegistry;
import org.streeto.ControlSite;


class ControlSiteSwapper extends UniformCrossover<AnyGene<ISeq<ControlSite>>, Double> {

    private final double crossoverProbability;

    ControlSiteSwapper(@SuppressWarnings("SameParameterValue") double crossoverProbability) {
        super(crossoverProbability);
        this.crossoverProbability = crossoverProbability;
    }

    @Override
    public int crossover(MSeq<AnyGene<ISeq<ControlSite>>> that, MSeq<AnyGene<ISeq<ControlSite>>> other) {
        var thatCourse = that.get(0).allele();
        var otherCourse = other.get(0).allele();

        var swapped = randomCrossover(thatCourse, otherCourse);
        that.set(0, that.get(0).newInstance(swapped.get(0)));
        other.set(0, that.get(0).newInstance(swapped.get(1)));
        return 2;
    }

    private ISeq<ISeq<ControlSite>> randomCrossover(ISeq<ControlSite> first, ISeq<ControlSite> second) {
        var rnd = RandomRegistry.random();
        var newFirsts = first.toArray(new ControlSite[]{});
        var newSeconds = second.toArray(new ControlSite[]{});
        for (int index = 1; index < newFirsts.length - 1; index++) {
            if (rnd.nextDouble() < crossoverProbability) {
                var a = newFirsts[index];
                var b = newFirsts[index + 1];
                newFirsts[index] = newSeconds[index];
                newFirsts[index + 1] = newSeconds[index + 1];
                newSeconds[index] = a;
                newSeconds[index + 1] = b;
            }
        }

        return ISeq.of(ISeq.of(newFirsts), ISeq.of(newSeconds));
    }
}
