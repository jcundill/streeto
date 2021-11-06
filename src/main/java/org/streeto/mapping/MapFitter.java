/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.streeto.mapping;

import org.streeto.utils.Envelope;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * finds, from the available possibilities, the best map scale and orientation that would allow
 * the current course to be printed on a sheet of paper
 */
public class MapFitter {

    public static final MapBox landscape10000 = new MapBox(
            0.04187945854565189 * 0.9,
            0.016405336634527146 * 0.9,
            10000,
            true);

    public static final MapBox portrait10000 = new MapBox(
            0.02955457284753238 * 0.9,
            0.024208663288803223 * 0.9,
            10000,
            false);

    public static final MapBox landscape10000A3 = new MapBox(
            portrait10000.getMaxWidth() * 2.0,
            portrait10000.getMaxHeight(),
            10000,
            true,
            PaperSize.A3);
    public static final MapBox landscape5000A3 = new MapBox(
            landscape10000A3.getMaxWidth() * 0.5,
            landscape10000A3.getMaxHeight() * 0.5,
            5000,
            true,
            PaperSize.A3);
    public static final MapBox landscape7500A3 = new MapBox(
            landscape10000A3.getMaxWidth() * 0.75,
            landscape10000A3.getMaxHeight() * 0.75,
            7500,
            true,
            PaperSize.A3);
    public static final MapBox landscape12500A3 = new MapBox(
            landscape10000A3.getMaxWidth() * 1.25,
            landscape10000A3.getMaxHeight() * 1.25,
            12500,
            true,
            PaperSize.A3);
    public static final MapBox landscape15000A3 = new MapBox(
            landscape10000A3.getMaxWidth() * 1.5,
            landscape10000A3.getMaxHeight() * 1.5,
            15000,
            true,
            PaperSize.A3);
    public static final MapBox landscape20000A3 = new MapBox(
            landscape10000A3.getMaxWidth() * 2.0,
            landscape10000A3.getMaxHeight() * 2.0,
            20000,
            true,
            PaperSize.A3);
    public static final MapBox portrait10000A3 = new MapBox(
            landscape10000.getMaxWidth(),
            landscape10000.getMaxHeight() * 2.0,
            10000,
            false,
            PaperSize.A3);
    public static final MapBox portrait5000A3 = new MapBox(
            portrait10000A3.getMaxWidth() * 0.5,
            portrait10000A3.getMaxHeight() * 0.5,
            5000,
            false,
            PaperSize.A3);
    public static final MapBox portrait7500A3 = new MapBox(
            portrait10000A3.getMaxWidth() * 0.75,
            portrait10000A3.getMaxHeight() * 0.75,
            7500,
            false,
            PaperSize.A3);
    public static final MapBox portrait12500A3 = new MapBox(
            portrait10000A3.getMaxWidth() * 1.25,
            portrait10000A3.getMaxHeight() * 1.25,
            12500,
            false,
            PaperSize.A3);
    public static final MapBox portrait15000A3 = new MapBox(
            portrait10000A3.getMaxWidth() * 1.5,
            portrait10000A3.getMaxHeight() * 1.5,
            15000,
            false,
            PaperSize.A3);
    public static final MapBox portrait20000A3 = new MapBox(
            portrait10000A3.getMaxWidth() * 2.0,
            portrait10000A3.getMaxHeight() * 2.0,
            20000,
            false,
            PaperSize.A3);
    public static final MapBox landscape5000 = new MapBox(
            landscape10000.getMaxWidth() * 0.5,
            landscape10000.getMaxHeight() * 0.5,
            5000,
            true);
    public static final MapBox portrait5000 = new MapBox(
            portrait10000.getMaxWidth() * 0.5,
            portrait10000.getMaxHeight() * 0.5,
            5000,
            false);
    public static final MapBox landscape7500 = new MapBox(
            landscape10000.getMaxWidth() * 0.75,
            landscape10000.getMaxHeight() * 0.75,
            7500,
            true);
    public static final MapBox portrait7500 = new MapBox(
            portrait10000.getMaxWidth() * 0.75,
            portrait10000.getMaxHeight() * 0.75,
            7500,
            false);
    public static final MapBox landscape12500 = new MapBox(
            landscape10000.getMaxWidth() * 1.25,
            landscape10000.getMaxHeight() * 1.25,
            12500,
            true);
    public static final MapBox portrait12500 = new MapBox(
            portrait10000.getMaxWidth() * 1.25,
            portrait10000.getMaxHeight() * 1.25,
            12500,
            false);
    public static final MapBox landscape15000 = new MapBox(
            landscape10000.getMaxWidth() * 1.5,
            landscape10000.getMaxHeight() * 1.5,
            15000,
            true);
    public static final MapBox portrait15000 = new MapBox(
            portrait10000.getMaxWidth() * 1.5,
            portrait10000.getMaxHeight() * 1.5,
            15000,
            false);
    public static final MapBox landscape20000 = new MapBox(
            landscape10000.getMaxWidth() * 2.0,
            landscape10000.getMaxHeight() * 2.0,
            20000,
            true);
    public static final MapBox portrait20000 = new MapBox(
            portrait10000.getMaxWidth() * 2.0,
            portrait10000.getMaxHeight() * 2.0,
            20000,
            false);
    private static final List<MapBox> possibleBoxes = List.of(
            landscape5000, portrait5000,
            landscape7500, portrait7500,
            landscape10000, portrait10000,
            portrait12500, landscape12500,
            portrait15000, landscape15000,
            portrait20000, landscape20000,
            landscape5000A3, portrait5000A3,
            landscape7500A3, portrait7500A3,
            landscape10000A3, portrait10000A3,
            portrait12500A3, landscape12500A3,
            portrait15000A3, landscape15000A3,
            portrait20000A3, landscape20000A3

    );

    /**
     * return the MapBox that would best enclose the points in the passed in envelope
     * or null if we don't have one
     */
    public static Optional<MapBox> getForEnvelope(Envelope env, PaperSize size, double maxScale) {
        return possibleBoxes.stream().filter(it ->
                it.getScale() <= maxScale &&
                it.getPaperSize() == size &&
                env.getWidth() < it.getMaxWidth() && env.getHeight() < it.getMaxHeight()).min(Comparator.comparingDouble(MapBox::getScale));
    }

    public static boolean canBeMapped(Envelope env, PaperSize size, double maxScale) {
        return getForEnvelope(env, size, maxScale).isPresent();
    }

    public static boolean canFitOnMap(Envelope env, MapBox box) {
        return env.getWidth() < box.getMaxWidth() && env.getHeight() < box.getMaxHeight();
    }

}