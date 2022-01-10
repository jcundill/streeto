package org.streeto.utils;

import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.streeto.utils.DistUtils.dist;

public class DistUtilsTest {
    @Test
    public void testClosestPoint() {
        GHPoint pt1 = new GHPoint(0, 0);
        GHPoint pt2 = new GHPoint(1, 1);
        GHPoint p = new GHPoint(0.75, 0.25);

        GHPoint closest = DistUtils.getClosestPoint(pt1, pt2, p);
        assertEquals(0.5, closest.getLat(), 0.001);
        assertEquals(0.5, closest.getLon(), 0.001);
    }

    @Test
    public void testClosestPointPastEnd() {
        GHPoint pt1 = new GHPoint(0, 0);
        GHPoint pt2 = new GHPoint(1, 1);
        GHPoint p = new GHPoint(0.75, 1.25);

        GHPoint closest = DistUtils.getClosestPoint(pt1, pt2, p);
        assertEquals(1.0, closest.getLat(), 0.001);
        assertEquals(1.0, closest.getLon(), 0.001);
    }

    @Test
    public void testClosestPointBeforeStart() {
        GHPoint pt1 = new GHPoint(10, 10);
        GHPoint pt2 = new GHPoint(20, 20);
        GHPoint p = new GHPoint(5, 15);

        GHPoint closest = DistUtils.getClosestPoint(pt1, pt2, p);
        assertEquals(10, closest.getLat(), 0.001);
        assertEquals(10, closest.getLon(), 0.001);
    }

    @Test
    public void testClosestPointNotMiddle() {
        GHPoint pt1 = new GHPoint(10, 10);
        GHPoint pt2 = new GHPoint(20, 20);
        GHPoint p = new GHPoint(20, 18);

        GHPoint closest = DistUtils.getClosestPoint(pt1, pt2, p);
        assertEquals(19, closest.getLat(), 0.001);
        assertEquals(19, closest.getLon(), 0.001);
    }

    @Test
    void getDistanceFromLine() {
        GHPoint pt1 = new GHPoint(0, 0);
        GHPoint pt2 = new GHPoint(1, 1);
        GHPoint p = new GHPoint(0.75, 0.25);

        double dist = DistUtils.getDistanceFromLine(pt1, pt2, p);
        assertEquals(dist(new GHPoint(0.5, 0.5), p), dist, 0.001);
    }

    @Test
    void hasNormal() {
        GHPoint pt1 = new GHPoint(0, 0);
        GHPoint pt2 = new GHPoint(1, 1);
        GHPoint p = new GHPoint(0.75, 0.25);
        assertTrue(DistUtils.hasNormal(pt1, pt2, p));
    }

    @Test
    void hasNormalAfter() {
        GHPoint pt1 = new GHPoint(0, 0);
        GHPoint pt2 = new GHPoint(1, 1);
        GHPoint p = new GHPoint(0.75, 1.25);
        assertFalse(DistUtils.hasNormal(pt1, pt2, p));
    }

    @Test
    public void hasNormalBeforeStart() {
        GHPoint pt1 = new GHPoint(10, 10);
        GHPoint pt2 = new GHPoint(20, 20);
        GHPoint p = new GHPoint(5, 15);
        assertFalse(DistUtils.hasNormal(pt1, pt2, p));
    }
}
