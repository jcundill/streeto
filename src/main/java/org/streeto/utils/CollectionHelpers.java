package org.streeto.utils;

import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CollectionHelpers {

    public static Stream<List<GHPoint3D>> windowed(PointList pl, int size) {
        return  StreamEx.ofSubLists(StreamEx.of(pl.iterator()).toList(), size, 1);

    }

    public static <T> Stream<T> streamFromIterable(List<T> iterable) {
        return StreamEx.of(iterable.iterator()) ;
    }

    public static Stream<GHPoint> streamFromPointList(PointList it) {
        return StreamEx.of(it.iterator());
    }

    public static Stream<List<PointList>> beforeAndAfterLegs(List<PointList> routes) {
        return StreamEx.ofSubLists(routes, 2, 1);
    }

    public static GHPoint last(PointList points) {
        return last(streamFromPointList(points).collect(Collectors.toList()));
    }
    public static GHPoint first(PointList points) {
        return first(streamFromPointList(points).collect(Collectors.toList()));
    }

    public static <T> T last(List<T> list) {
        var num = list.size() - 1;
        return list.get(num);
    }
    public static <T> T first(List<T> list) {
        return list.get(0);
    }

    public static List<GHPoint3D> drop(PointList lists, int i) {
       return StreamEx.of(lists.iterator()).toList().subList(i, lists.size());
   }

    public static List<GHPoint3D> dropLast(PointList lists, int i) {
      return StreamEx.of(lists.iterator()).toList().subList(0, lists.size() - i);
  }
    @SuppressWarnings("unchecked")
    public static <T> Stream<T> reverse(Stream<T> input) {
            Object[] temp = input.toArray();
            return (Stream<T>) IntStream.range(0, temp.length)
                    .mapToObj(i -> temp[temp.length - i - 1]);
        }
}
