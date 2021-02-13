package org.streeto.constraints;

import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.stream.Stream;

public class CollectionHelpers {
    public static Stream<List<GHPoint3D>> windowed(PointList pl, int size) {
        return  StreamEx.ofSubLists(StreamEx.of(pl.iterator()).toList(), size, 1);

    }
}
