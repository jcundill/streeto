package org.streeto.genetic;

import io.jenetics.AnyGene;
import io.jenetics.Mutator;
import io.jenetics.util.ISeq;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;

import java.util.Random;

public class CourseMutator extends Mutator<AnyGene<ISeq<ControlSite>>, Double> {
    private final ControlSiteFinder csf;

    public CourseMutator(ControlSiteFinder csf, Double probability) {
        super(probability);
        this.csf = csf;
    }

    @Override
    public AnyGene<ISeq<ControlSite>> mutate(AnyGene<ISeq<ControlSite>> gene, Random random) {
        if (gene != null) {
            var course = gene.allele();
            var newCourse = mutateCourse(course, random);
            return gene.newInstance(newCourse);
        } else {
            return super.mutate(gene, random);
        }
    }

    private ISeq<ControlSite> randomMutate(ISeq<ControlSite> controls, Random random) {
        return controls.map(ctrl -> {
            if (random.nextDouble() < probability()) return csf.findAlternativeControlSiteFor(ctrl, 500.0);
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

