package org.streeto.utils;

import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.streeto.utils.CollectionHelpers.*;

public class CollectionHelpersTest {

    @Test
    public void windowedList() {
        var a = List.of(1,2,3,4,5,6,7);
        var expected = List.of(List.of(1,2), List.of(2,3), List.of(3,4), List.of(4,5), List.of(5,6), List.of(6,7));
        var b = windowed(a,2).collect(Collectors.toList());
        assertEquals(expected, b);
    }

    @Test
    public void transposeTest() {
        var a = List.of(
                List.of(1,2,3,4),
                List.of(1,2,3,4),
                List.of(1,2,3,4)
        );
        var expected = List.of(
                List.of(1,1,1),
                List.of(2,2,2),
                List.of(3,3,3),
                List.of(4,4,4)
        );
        assertEquals(expected, transpose(a));
    }

    @Test
    public void testLast() {
        var a = List.of(1,2,3,4,5,6,7);
        assertEquals(7, last(a));
    }

    @Test
    public void testFirst() {
        var a = List.of(1,2,3,4,5,6,7);
        assertEquals(1, first(a));
    }

    @Test
    public void testDrop() {
        var a = List.of(1,2,3,4,5,6,7);
        assertEquals(List.of(4,5,6,7), drop(a, 3));
    }

    @Test
    public void testDropLast() {
        var a = List.of(1,2,3,4,5,6,7);
        assertEquals(List.of(1,2,3,4), dropLast(a, 3));
    }

    @Test
    public void testDropFirstAndLast() {
        var a = List.of(1,2,3,4,5,6,7);
        assertEquals(List.of(4), dropFirstAndLast(a, 3));
    }

    @Test
    public void testDropFirstAndLastTooFew() {
        var a = List.of(1);
        assertEquals(List.of(), dropFirstAndLast(a, 1));
    }

    @Test
    public void testTake() {
        var a = List.of(1,2,3,4,5,6,7);
        assertEquals(List.of(1,2,3), take(a, 3));
    }

    @Test
    public void testTakeLst() {
        var a = List.of(1,2,3,4,5,6,7);
        assertEquals(List.of(5,6,7), takeLast(a, 3));
    }

    @Test
    public void testReverse() {
        var a = List.of(1,2,3,4,5,6,7);
        assertEquals(List.of(7,6,5,4,3,2,1), reverse(a.stream()).collect(Collectors.toList()));
    }

    @Test
    public void testForEachIndexed() {
        BiConsumer<Integer,Integer> fun = (idx, num) -> assertEquals(idx, num - 1);
        var a = List.of(1,2,3,4,5,6,7);
        forEachIndexed(a, fun);
    }

    @Test
    public void testMapIndexed() {
        BiFunction<Integer,Integer, Integer> fun = (idx, num) -> idx + num + 1;
        var a = List.of(1,2,3,4,5,6,7);
        var b = mapIndexed(a, fun).collect(Collectors.toList());
        assertEquals(List.of(2,4,6,8,10,12,14), b);
    }

    @Test
    public void testIntersection() {
        var a = List.of(1,2,3,4,5,6,7);
        var b = List.of(3,4,5,8,9,10);
        assertEquals(List.of(3,4,5), intersection(a,b));
    }

    @Test
    public void testForEachZipped() {
        BiConsumer<Integer,Integer> fun = (a, b) -> assertEquals(a * 2, b);
        var a = List.of(1,2,3,4,5,6,7);
        var b = List.of(2,4,6,8,10,12,14);
        forEachZipped(a, b, fun);
    }

    @Test
    public void testMapZipped() {
        BiFunction<Integer,Integer, Integer> fun = (a, b) -> b - a;
        var a = List.of(1,2,3,4,5,6,7);
        var b = List.of(2,4,6,8,10,12,14);
        var c = mapZipped(a, b, fun).collect(Collectors.toList());
        assertEquals(a, c);
    }
}