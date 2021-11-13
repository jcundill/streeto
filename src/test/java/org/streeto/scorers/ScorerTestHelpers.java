package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Combinators;
import net.jqwik.api.Provide;
import net.jqwik.api.arbitraries.ListArbitrary;

public interface ScorerTestHelpers {
    @Provide("routed legs")
    static ListArbitrary<GHResponse> routedLegsProvider() {
        var dist = Arbitraries.doubles().between(0.0, 15000.0);
        var errs = Arbitraries.of(true, false);
        return Combinators.combine(dist, errs).as((d, e) -> {
            var a = new ResponsePath();
            a.setDistance(d);
            a.setImpossible(e);
            var b = new GHResponse();
            b.add(a);
            return b;
        }).list().ofMinSize(2);
    }
}
