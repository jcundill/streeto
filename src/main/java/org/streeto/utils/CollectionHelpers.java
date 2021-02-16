package org.streeto.utils;

import one.util.streamex.StreamEx;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Math.min;

public class CollectionHelpers {

    public static <T> Stream<List<T>> windowed(Iterable<T> pl, int size) {
        return StreamEx.ofSubLists(StreamEx.of(pl.iterator()).toList(), size, 1);
    }

    public static <T> Stream<List<T>> beforeAndAfterLegs(List<T> routes) {
        return StreamEx.ofSubLists(routes, 2, 1);
    }

    public static <T> Stream<T> iterableAsStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T> T last(Iterable<T> points) {
        return last(iterableAsStream(points).collect(Collectors.toList()));
    }

    public static <T> T first(Iterable<T> points) {
        return first(iterableAsStream(points).collect(Collectors.toList()));
    }

    public static <T> Stream<List<T>> windowed(List<T> pl, int size) {
        return StreamEx.ofSubLists(pl, size, 1);
    }

    public static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> T first(List<T> list) {
        return list.get(0);
    }

    public static <T> List<T> drop(Iterable<T> list, int i) {
        var ret = iterableAsStream(list)
                .collect(Collectors.toList());
        return ret.subList(i, ret.size());
    }

    public static <T> List<T> dropLast(Iterable<T> list, int i) {
        var ret = iterableAsStream(list).collect(Collectors.toList());
        return ret.subList(0, ret.size() - i);
    }

    public static <T> List<T> dropFirstAndLast(List<T> list, int i) {
        if (list.size() < 2) return List.of();
        else return list.subList(i, list.size() - 1);
    }


    @SuppressWarnings("unchecked")
    public static <T> Stream<T> reverse(Stream<T> input) {
        Object[] temp = input.toArray();
        return (Stream<T>) IntStream.range(0, temp.length)
                .mapToObj(i -> temp[temp.length - i - 1]);
    }


    public static <T> void forEachIndexed(List<T> list, BiConsumer<Integer, T> consumer) {
        IntStream.range(0, list.size()).forEach(idx -> consumer.accept(idx, list.get(idx)));
    }

    public static <T, R> Stream<R> mapIndexed(List<T> list, BiFunction<Integer, T, R> function) {
        return IntStream.range(0, list.size()).mapToObj(idx -> function.apply(idx, list.get(idx)));
    }

    public static <T> List<T> intersection(List<T> pointsA, List<T> pointsB) {
        return pointsA.stream()
                .filter(pointsB::contains)
                .collect(Collectors.toList());
    }

    public static <T, U> void forEachZipped(List<T> ts, List<U> us, BiConsumer<T, U> function) {
        IntStream.range(0, min(ts.size(), us.size())).forEach(idx -> function.accept(ts.get(idx), us.get(idx)));
    }

    public static <T, U, R> Stream<R> mapZipped(List<T> ts, List<U> us, BiFunction<T, U, R> function) {
        return IntStream.range(0, min(ts.size(), us.size())).mapToObj(idx -> function.apply(ts.get(idx), us.get(idx)));
    }


}
