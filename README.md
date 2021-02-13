
## Osmosys - OpenStreetMap Orienteering system
Use goal based searching to generate passable Urban Orienteering Courses.

Uses routing algorithms on top of OSM data to find challenging course legs. The start location can be anywhere in the world as long as an OSM protobuf file covering that location has been loaded into the system. So start your run just outside your front door if you like.

Helper functions are provided to:
+  Generate a printable PDF A4 open orienteering map <https://oomap.co.uk/gb> of the generated course overlaid with the controls as a line course.
+  Generate the KML and KMZ files needed for uploading to MapRunF <https://play.google.com/store/apps/details?id=au.com.fne.maprunf&hl=en_GB> 

This allows you to immediately run the generated course yourself by printing out the map and importing the maprun files into CheckSites <http://www.p.fne.com.au/#/checksitesupload>



## How it works

Osmosys uses the Graphhopper routing engine <https://github.com/graphhopper/graphhopper>  to calculate runnable routes between arbitrary locations. 
Where those potential routes are limited to those that you could legally and safely run along. So no motorways, private service roads etc.

System is given a starting location, an approximate length to make the
generated course and the number of controls to place on the course.

The algorithm currently seems to perform best with the number of controls set to around 1.5 times the requested distance in K - so 8 or 9 on a 6K route, maybe 14 to 16 on a 10K route. 
However, the control density that would allow the generation of a reasonable route is largely a function of the complexity of the OSM data in the area around the start location.
But, unlike properly planned courses, don't expect to let the system manage 30 controls on a 10K run.
Trying to increase the density of the controls whilst still maintaining a decent overall course route is one of the things I'd like to improve in the algorithm.

Control site locations are selected from:
 +  Node type street furniture available on OSM - post boxes, bus stops, bollards, trees, etc
 +  Ends of linear features available on OSM - hedges, steps, bridges
 +  Path junctions and bends
 
Preference is given to the first two types - if available near to the selected control location. 

Then an initial course is seeded from these inputs, and the algorithm iteratively tries
to improve the placement of the controls on the course to make the legs between them
more interesting as an orienteering course.

This is done 1000 times, which takes around 20secs on my Macbook, then the following artifacts are created:
+  gpx file of the course and optimal path around it
+  Open Orienteering Map showing the course
+  KMZ and KML files of the course for uploading to MapRun

As we are routing legs along the street, road, footpath and track data in OSM, the quality of the generated course is largely 
constrained by the number, structure and layout of these features in the area surrounding the start location. So you tend to get a better result in areas like
town centres and housing estates than you do out in more rural locations with limited available route choices.

### Course Improving
This is the core rationale for this project. Trying to automate finding a set of locations on a streetmap that would make for a reasonable urban orienteering challenge.

Osmosys uses a Simulated Annealing approach <https://en.wikipedia.org/wiki/Simulated_annealing> to improving the control locations.

At each iteration, the legs between the controls are scored using the following set of factors:
+  Leg Route Choice - did the routing engine suggest alternative routes for this leg, and if so how dissimilar are they
+  Leg Complexity - how many decisions about turn left, turn right, etc are there on this leg
+  Leg Length - is this leg just too short or too long
+  Dog Leg - is this just coming back the same way we just went
+  Been This Way Before - how much of the leg to this numbered control has been travelled along already
+  Coming Back Here Later - do we run close to a future control on this leg
+  Only Go To The Finish At The End - primarily for MapRun, don't accidentally trigger the finish part way through the course
+  Last Control Is Near The Finish - don't make the run in annoyingly long
+  Didn't Move - this leg is so short the controls are basically in the same place

The system then selects a number of the worst scoring legs and randomly picks an alternative location
for the control at the end of that leg. For the last leg, the last control rather than the finish location is replaced.

These replacements are fed into the Annealing solver which re-scores the resulting course and either accepts
or rejects this new course depending on both the overall scores for both the old and the
new courses and the amount of energy still remaining in the Solver.

If rejected new alternatives for the worst scoring legs are re-evaluated.
If accepted the Solver moves on to look at the worst scoring legs in this new course.

Annealing continues for 1000 iterations by default.

Each time the course is improved we defensively run a Travelling Salesman solver over it to makes sure control ordering remains fairly sensible - we don't just keep running from one side of the map to the other but try and generate a course showing some kind of 'round-trip' behaviour.

In addition to the improvers, there are a number of hard constraints that the course must always satisfy:
+ Course Length - must be reasonably similar to the requested course length
+ Is Routeable - there must exist at least one safely runnable route around the whole course
+ Printable On Map - Is it possible to fit the course onto either a Landscape or Portrait A4 map at 5000, 7500, 10000, 12500 or 15000 scale


### Installing and Getting Started

Osmosys is a java based piece of Software, primarily because the Graphhopper routing engine that does a lot of the heavy lifting is written in Java.

The codebase itself is Kotlin - https://kotlinlang.org/ - sorry about that I mostly
wrote this code as an excuse to learn Kotlin properly - still could be worse I started off writing it in Scala.

Maven build system, but please note the GPX library used in not in mvn

```
mvn install:install-file -Dfile=lib/gpxparser-20130603.jar -DgroupId=jon.gpx -DartifactId=jon.gpx.gpxparser -Dversion=1.0 -Dpackaging=jar
```
To get mvn to recognise it locally, then

```
mvn clean verify
```
To get everything to compile.


### Importing Open Street Map data

Graphhopper works against a local database built from OSM protobuf files. You will need to download an appropriate openstreetmap protocol buffer file for the area you want to make courses for. Great-britain-latest.osm.pbf or similar for nationwide coverage - or choose a smaller one for your local area.

For details of pbf file downloads see <https://download.geofabrik.de/europe/great-britain.html>

The downloaded pbf file needs to go into a folder called extracts in order to initialise the Graphhopper database - see initGH in `GhWrapper.kt` for details of how this is done.

```kotlin
    fun initGH(name: String): ControlSiteFinder {

        val gh = GraphHopperOSM().apply {
            forServer()
            osmFile = "extracts/$name.osm.pbf"
            graphHopperLocation = "osm_data/grph_$name"
            isCHEnabled = false
            setElevation(true)
            elevationProvider = SRTMProvider()
            encodingManager = EncodingManager.create(oFlagEncoder)
            setEnableCalcPoints(true)
        }

        gh.importOrLoad()
        return ControlSiteFinder(gh)
    }
```

### Running the Algorithm
Please note that currently there are just a bunch of Integration Tests that give examples of how the Solver can be used.

I've started some basic work to wrap this as a webapp with a REST api, but to date I've mostly been more interested in playing around with the algorithm itself than providing REST access to it.

Examples of how to run the Solver are in `MainIT.kt`.



#### Running the Solver using a property file

```kotlin
   @Test
    fun main() {

        val props =  "./streeto.properties"

        val initialCourse = Course.buildFromProperties(props)
        val problem = osmosys.makeProblem(initialCourse)
        val solution = osmosys.findCourse(problem, 1000)

        if (solution != null) {
           val timestamp = Date().time
            gpxWriter.writeToFile(solution, "Map-$timestamp.gpx")
            printMap(solution, timestamp)
            printStats(solution)
        }
    }

```

Where the property file contains:
+  distance - roughly how long the route should be in metres
+  numControls - how many controls in the course
+  controls - pipe separated list of lat lon locations

Example:
```
distance = 6000
numControls = 9
controls = 51.223650, -1.361135|51.231753, -1.336556

```

This will try and generate a roughly 6K course of 9 controls with start at the first location and the finish at the last

Most of the time, for private use, you just want to start and finish in the same place 

```
distance = 6000
numControls = 9
controls = 51.223650, -1.361135

```
 
 With more than two pipe separated locations in the controls list Osmosys will use those to seed control locations in the initally generated course. This allows you to hint, somewhat which direction you'd like the course to go in.


 In the example test shown above - if the Solver generated a runnable course after 1000 iterations then write out the map elements - kmz, kml and pdf and display some stats as to how the Solver scored the generated course - see the relevant functions in `MainIT.kt` for details of these.

#### Running the Solver against an existing KML file

Maybe one initially created in Google Earth

```kotlin
   @Test
    fun courseFromKml() {
        val initialCourse = Course.buildFromKml("my-course.kml")
        val course = osmosys.score(initialCourse)
        printStats(course)
        val problem = osmosys.makeProblem(course)
        val newCourse = osmosys.findCourse(problem, 1000)
        if (newCourse != null )
            printStats(newCourse)

    }
```

Here the course is imported into the system and each leg is initially just scored against the critera mentioned above. Then that imported course is used to seed the Solver and the scores of a potentially improved set of controls are output once the Solver has completed.





