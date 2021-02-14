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

package org.streeto;

import com.graphhopper.util.shapes.GHPoint;

public class ControlSite {
    private final GHPoint position;
    private final String description;

    public ControlSite(GHPoint p, String desc) {
        position = p;
        description = desc;
    }

    public ControlSite(GHPoint p) {
        position = p;
        description = "";
    }

    public GHPoint getPosition() {
        return position;
    }

    public String getDescription() {
        return description;
    }

    public ControlSite(double lat, double lon, String desc) {
        position = new GHPoint(lat, lon);
        description = desc;
    }

    public ControlSite(double lat, double lon) {
        position = new GHPoint(lat, lon);
        description = "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ControlSite that = (ControlSite) o;

        if (!position.equals(that.position)) return false;
        return description.equals(that.description);
    }

    @Override
    public int hashCode() {
        int result = position.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ControlSite{" +
               "position=" + position +
               ", description='" + description + '\'' +
               '}';
    }
}