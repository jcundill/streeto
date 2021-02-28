package org.streeto;

import org.streeto.genetic.Sniffer;

import java.util.List;

public class StreetOSniffer extends Sniffer {

    @Override
    public void accept(long generation, double fitness, List<ControlSite> controls) {
        var stats = String.format("Generation: %d, Best: %f",
                generation, fitness);
        System.out.println(stats);

    }

    @Override
    public void acceptStatistics(String details) {
        System.out.println(details);
    }
}
