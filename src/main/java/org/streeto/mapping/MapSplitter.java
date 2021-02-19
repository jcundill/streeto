package org.streeto.mapping;

import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.streeto.utils.CollectionHelpers.*;

public class MapSplitter {

    private final ControlSiteFinder csf;

    public MapSplitter(ControlSiteFinder csf) {
        this.csf = csf;
    }

    public SplitResult makeDoubleSidedIfPossible(List<ControlSite> controls, MapBox box) {
        // find subset in the middle where
        // subset can be mapped on a larger scale
        // and others including head and tail of subset can also be mapped on the same larger scale
        var splitLocations = IntStream.range(1, controls.size())
                .mapToObj( startOffset -> findPartitionsFromOffset(startOffset, controls, box))
                .flatMap(x -> x)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (splitLocations.isEmpty()) {
            return null;
        } else {
            var sorted = splitLocations.stream()
                    .sorted(Comparator.comparingDouble(a -> a.box.getScale()))
                    .collect(Collectors.toList());
            var mostEqual = sorted.stream()
                    .takeWhile( it -> it.box.getScale() == first(sorted).box.getScale() )
                    .min(Comparator.comparingInt(SplitResult::lengthDiff));
            return mostEqual.orElse(null);
        }
    }

    private Stream<SplitResult> findPartitionsFromOffset(int startOffset, List<ControlSite> controls, MapBox box) {
        return IntStream.range(startOffset + 1, controls.size() - 2)
                .mapToObj( endOffset -> getSplitForOffset(controls, startOffset, endOffset, box)
        );
    }

    private SplitResult getSplitForOffset(List<ControlSite> controls, int startOffset, int endOffset, MapBox box){
        var middle = drop( take(controls, endOffset), startOffset);
        var remainder = Stream.concat(take(controls, startOffset + 1).stream(), drop(controls, endOffset - 1).stream())
                .collect(Collectors.toList());//include start and end of middle in remainder

        if(middle.size() < 2 || remainder.size() < 2) return null;
        var envMiddle = csf.getEnvelopeForProbableRoutes(middle);
        var envRemainder = csf.getEnvelopeForProbableRoutes(remainder);
        var middleBox = MapFitter.getForEnvelope(envMiddle).orElse(null);
        var remainderBox = MapFitter.getForEnvelope(envRemainder).orElse(null);
        if (middleBox != null && remainderBox != null) {
            var canPossiblySplit = middleBox.getScale() < box.getScale() && remainderBox.getScale() < box.getScale();
            if( !canPossiblySplit ) return null;
            else if( middleBox.getScale() == remainderBox.getScale() && middleBox.isLandscape() == remainderBox.isLandscape() ||
                     middleBox.getScale() > remainderBox.getScale() && MapFitter.canFitOnMap(envRemainder, middleBox)) {
                return new SplitResult(remainder, envRemainder, middle, envMiddle, middleBox);
            } else if( remainderBox.getScale() > middleBox.getScale() && MapFitter.canFitOnMap(envMiddle, remainderBox)) {
                return new SplitResult(remainder,envRemainder, middle, envMiddle, remainderBox);
            } else {
                return null;
            }
        } else
           return null;
    }
}