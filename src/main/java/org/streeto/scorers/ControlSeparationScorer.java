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

package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint3D;
import org.jetbrains.annotations.NotNull;
import org.streeto.ControlSite;
import org.streeto.StreetOPreferences;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.pow;
import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;
import static org.streeto.utils.DistUtils.getDistanceFromLine;

public class ControlSeparationScorer extends ControlSetScorer {

    public ControlSeparationScorer(StreetOPreferences preferences) {
        super(preferences);
    }

    /**
     * works out if controls are too close to each other
     * and scores the relevant controls badly if they are
     */
     public double score(List<ControlSite> controls) {
         var tooCloseInstances =  controls.stream().map(c -> controls.stream()
                 .anyMatch(c2 -> (dist(c2.getLocation(), c.getLocation()) < preferences.getMinControlSeparation() && c2 != c)) ? 0.0 : 1.0).mapToDouble(Double::doubleValue).sum();
         var allInstances = pow(controls.size(), 2);
         return tooCloseInstances / allInstances;
     }
}