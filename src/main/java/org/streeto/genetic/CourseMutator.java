package org.streeto.genetic;

import io.jenetics.AnyGene;
import io.jenetics.Mutator;
import io.jenetics.util.ISeq;
import org.jetbrains.annotations.NotNull;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;

import java.util.Random;

public class CourseMutator extends Mutator<AnyGene<ISeq<ControlSite>>, Double> {
    private final ControlSiteFinder csf;
    private final double mutateRadius;

    public CourseMutator(ControlSiteFinder csf, double mutateProbability, double mutateRadius) {
        super(mutateProbability);
        this.csf = csf;
        this.mutateRadius = mutateRadius;
    }

    @Override
    public AnyGene<ISeq<ControlSite>> mutate(@NotNull AnyGene<ISeq<ControlSite>> gene, Random random) {
        var controls = gene.allele();
        var newControls = mutateCourse(controls, random);
        return gene.newInstance(newControls);
    }

    private ISeq<ControlSite> randomMutate(ISeq<ControlSite> controls, Random random) {
        return controls.map(ctrl -> {
            if (random.nextDouble() < probability()) return csf.findAlternativeControlSiteFor(ctrl, mutateRadius);
            else return ctrl;
        });
    }

    private <T> ISeq<T> removeStartAndFinish(ISeq<T> points) {
        var last = points.size() - 1;
        if (last < 1) return ISeq.of();
        return points.subSeq(1, last);
    }


    private ISeq<ControlSite> mutateCourse(ISeq<ControlSite> controls, Random random) {
        return ISeq.of(controls.get(0))
                .append(randomMutate(removeStartAndFinish(controls), random))
                .append(controls.get(controls.size() - 1));

    }
}

