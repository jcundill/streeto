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

import com.vividsolutions.jts.geom.Envelope;
import io.jenetics.util.RandomRegistry;
import org.jetbrains.annotations.NotNull;
import org.streeto.mapping.MapFitter;
import org.streeto.seeders.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CourseSeeder {

    private final ControlSiteFinder csf;
    private final List<SeedingStrategy> seeders;

    public CourseSeeder(ControlSiteFinder csf) {
        this.csf = csf;
        seeders = List.of(
                new RectangleSeeder(this.csf),
                new TriangleSeeder(this.csf),
                new HourglassSeeder(this.csf),
                new FatHourglassSeeder(this.csf),
                new CentredFatHourglassSeeder(this.csf),
                new CentredHourglassSeeder(this.csf));
    }

    private SeedingStrategy chooseSeeder() {
        var roll = rnd().nextInt(6);
        return seeders.get(roll);
  
    }

    private Random rnd() {
        return RandomRegistry.random();
    }

    public List<ControlSite>  chooseInitialPoints(List<ControlSite> initialPoints, int requestedNumControls, double requestedCourseLength){

        var env = new Envelope();

        // check that everything we have been given is mappable
        var startPoint = csf.findNearestControlSiteTo(initialPoints.get(0).getPosition());
        var finishPoint = csf.findNearestControlSiteTo(initialPoints.get(initialPoints.size() - 1).getPosition());
        if(startPoint.isEmpty()) throw new RuntimeException("Cannot map the start");
        if(finishPoint.isEmpty()) throw new RuntimeException("Cannot map the finish");

        var start = startPoint.get();
        var finish = finishPoint.get();

        List<ControlSite> chosenControls = List.of();
        if( initialPoints.size() > 2) {
            chosenControls = initialPoints.subList(1, initialPoints.size() - 1).stream().map(it ->
                    csf.findNearestControlSiteTo(it.getPosition()).get()
            ).collect(Collectors.toList());
        }


        env.expandToInclude(start.getPosition().getLon(), start.getPosition().getLat());
        env.expandToInclude(finish.getPosition().getLon(), finish.getPosition().getLat());
        chosenControls.forEach ( it->
            env.expandToInclude(it.getPosition().getLon(), it.getPosition().getLat())
        );


        if (!canBeMapped(env)) {
            throw new RuntimeException("initial course cannot be mapped");
        }

        var initialControls = getControlSites(start, finish, chosenControls);

        var controls =  chooseSeeder().seed(initialControls, requestedNumControls, requestedCourseLength);
        var ctrls = (rnd().nextDouble() < 0.5) ? controls : reverse(controls.stream()).collect(Collectors.toList());
        return getControlSites(start, finish, ctrls);
    }

    @NotNull
    private List<ControlSite> getControlSites(ControlSite startPoint, ControlSite finishPoint, List<ControlSite> chosenControls) {
        return Stream.concat(Stream.concat(Stream.of(startPoint), chosenControls.stream()), Stream.of(finishPoint)).collect(Collectors.toList());
    }

    private static Stream<ControlSite> reverse( Stream<ControlSite> input) {
            ControlSite[] temp = input.toArray(ControlSite[]::new);
            return IntStream.range(0, temp.length)
                    .mapToObj(i -> temp[temp.length - i - 1]);
        }



    private boolean canBeMapped(Envelope env) {
        return MapFitter.getForEnvelope(env).isPresent();
    }

}