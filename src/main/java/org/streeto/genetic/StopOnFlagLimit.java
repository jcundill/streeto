package org.streeto.genetic;

import io.jenetics.AnyGene;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.ISeq;
import org.streeto.ControlSite;

import java.util.function.Predicate;

public class StopOnFlagLimit implements Predicate<EvolutionResult<AnyGene<ISeq<ControlSite>>, Double>> {
    private static final StopOnFlagLimit instance = new StopOnFlagLimit();
    private boolean stopNow;

    public static StopOnFlagLimit instance() {
        return instance;
    }

    public Boolean isStopNow(EvolutionResult<AnyGene<ISeq<ControlSite>>, Double> unused) {
        return stopNow;
    }

    public void setStopNow(boolean b) {
        stopNow = b;
    }

    @Override
    public boolean test(EvolutionResult<AnyGene<ISeq<ControlSite>>, Double> result) {
        if (result.generation() > 1) {
            return !stopNow;
        } else {
            return true;
        }
    }
}
