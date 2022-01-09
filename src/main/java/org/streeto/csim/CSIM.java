package org.streeto.csim;

import com.graphhopper.ResponsePath;

import java.util.List;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.dropFirstAndLast;
import static org.streeto.utils.CollectionHelpers.iterableAsStream;


class CSIM {

    private final Route router;

    public CSIM(int cellLength) {
        this.router = new Route(cellLength);
    }

    private List<Cell> fromResponsePath(ResponsePath path) {
        var points = dropFirstAndLast(iterableAsStream(path.getPoints()).map(Point::fromGraphHopperPoint).toList(), 1);
        return router.getCells(points);
    }

    double calculateFor(ResponsePath a, ResponsePath b) {
        List<Cell> cellsA = fromResponsePath(a);
        List<Cell> cellsB = fromResponsePath(b);

        var ca = cellsA.stream().filter(c -> c.frequency > 0).collect(Collectors.toSet());
        var cb = cellsB.stream().filter(c -> c.frequency > 0).collect(Collectors.toSet());
        var caShadow = cellsA.stream().filter(c -> c.frequency == 0).collect(Collectors.toSet());
        var cbShadow = cellsB.stream().filter(c -> c.frequency == 0).collect(Collectors.toSet());

        var numInAAndB = cb.stream().filter(ca::contains).count();
        var numInAShadowAndB = caShadow.stream().filter(cb::contains).count();
        var numInAAndBShadow = cbShadow.stream().filter(ca::contains).count();

        return 1.0 * (numInAAndB + numInAShadowAndB + numInAAndBShadow) / (ca.size() + cb.size() - numInAAndB);
    }
}
