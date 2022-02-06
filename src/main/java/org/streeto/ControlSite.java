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

import java.util.Objects;

public class ControlSite {
    private final GHPoint location;
    private final String description;
    private String number;

    private ControlType type;

    public ControlSite(GHPoint p, String description) {
        this(p, description, ControlType.UNCHECKED);
    }

    public ControlSite(GHPoint p, String description, ControlType type) {
        this.location = p;
        this.description = description;
        this.type = type;
    }


    public String getDescription() {
        return description;
    }

    public GHPoint getLocation() {
        return location;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public ControlType getType() {
        return type;
    }

    public void setType(ControlType type) {
        this.type = type;
    }


    @Override
    public int hashCode() {
        int result = location.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ControlSite that = (ControlSite) o;

        if (location.lon != that.location.lon) return false;
        if (location.lat != that.location.lat) return false;
        if (!Objects.equals(description, that.description)) return false;
        return Objects.equals(number, that.number);
    }

    @Override
    public String toString() {
        return "ControlSite{" +
               "location=" + location +
               ", description='" + description + '\'' +
               ", number='" + number + '\'' +
               '}';
    }
}