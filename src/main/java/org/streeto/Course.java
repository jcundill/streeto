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

import java.util.List;


public class Course {

    private final double requestedDistance;
    private final int requestedNumControls;
    private final List<ControlSite> controls;

    public Course(double requestedDistance, int requestedNumControls, List<ControlSite> controls) {
        this.requestedDistance = requestedDistance;
        this.requestedNumControls = requestedNumControls;
        this.controls = controls;
    }

    public double getRequestedDistance() {
        return requestedDistance;
    }

    public int getRequestedNumControls() {
        return requestedNumControls;
    }

    public List<ControlSite> getControls() {
        return controls;
    }

    @Override
    public String toString() {
        return "Course{" +
               "requestedDistance=" + requestedDistance +
               ", requestedNumControls=" + requestedNumControls +
               ", controls=" + controls +
               '}';
    }
}