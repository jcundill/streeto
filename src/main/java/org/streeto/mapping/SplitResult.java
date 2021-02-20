package org.streeto.mapping;

import org.streeto.ControlSite;
import org.streeto.utils.Envelope;

import java.util.List;

import static java.lang.Math.abs;

public class SplitResult {
    List<ControlSite> split1;
    Envelope env1;
    List<ControlSite> split2;
    Envelope env2;
    MapBox box;

    public SplitResult(List<ControlSite> remainder, Envelope remainderEnvelope, List<ControlSite> middle, Envelope middleEnvelope, MapBox box) {
        this.split1 = remainder;
        this.env1 = remainderEnvelope;
        this.split2 = middle;
        this.env2 = middleEnvelope;
        this.box = box;
    }

    public List<ControlSite> getSplit1() {
        return split1;
    }

    public Envelope getEnv1() {
        return env1;
    }

    public List<ControlSite> getSplit2() {
        return split2;
    }

    public Envelope getEnv2() {
        return env2;
    }

    public MapBox getBox() {
        return box;
    }

    int lengthDiff() {
        return abs(split2.size() - split1.size());
    }

}
