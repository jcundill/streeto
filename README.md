
## StreetO - OpenStreetMap based Urban Orienteering Course Generation

Use goal based searching algorithms to automatically generate passable Urban Orienteering Courses.

Uses routing algorithms on top of OSM data to find a number of challenging course legs to make an urban orienteering course of a given distance. The start location is specified as a parameter to the algorithm and can be anywhere in the world as long as an OSM protobuf file covering that location has been loaded into the system. So start your run just outside your front door if you like.

Helper functions are provided to:
+  Generate a printable PDF A4 open orienteering map <https://oomap.co.uk/gb> of the generated course overlaid with the controls as a line course.
+  Generate the KML and KMZ files needed for uploading to MapRunF <https://play.google.com/store/apps/details?id=au.com.fne.maprunf&hl=en_GB> 

This allows you to immediately run the generated course yourself by printing out the map and importing the maprun files into CheckSites <http://www.p.fne.com.au/#/checksitesupload>



## How it works

StreetO uses the Graphhopper routing engine <https://github.com/graphhopper/graphhopper>  to calculate runnable routes between arbitrary locations. 
Where those potential routes are limited to those that you could legally and safely run along. So no motorways, private service roads etc.

System is given a starting location, an approximate length to make the
generated course and the number of controls to place on the course.

Control site locations are selected from:
 +  Node type street furniture available on OSM - post boxes, bus stops, bollards, trees, etc
 +  Ends of linear features available on OSM - hedges, steps, bridges
 +  Path junctions and bends
 
Preference is given to the first two types - if available near to the selected control location. 

Then an initial course is seeded from these inputs, and the algorithm iteratively tries
to improve the placement of the controls on the course to make the legs between them
more interesting as an orienteering course.

This is done a number of times, by default we give it 2 minutes of processing time, then the following artifacts are created:
+  gpx file of the course and optimal path around it
+  Open Orienteering Map showing the course
+  KMZ and KML files of the course for uploading to MapRun

As we are routing legs along the street, road, footpath and track data in OSM, the quality of the generated course is largely 
constrained by the number, structure and layout of these features in the area surrounding the start location. So you tend to get a better result in areas like
town centres and housing estates than you do out in more rural locations with limited available route choices.

### Course Improving
This is the core rationale for this project. Trying to automate finding a set of locations on a streetmap that would make for a reasonable urban orienteering challenge.

StreetO uses a Genetic Algorithm approach to improving the control locations and makes use of the Jenetics project to implement this functionality, see https://jenetics.io/ for details.

We construct a population of candidate courses, then try to improve the population.

At each iteration, the legs between the controls on each of the candidates are scored using the following set of factors:
+  Leg Route Choice - did the routing engine suggest alternative routes for this leg, and if so how dissimilar are they
+  Leg Complexity - how many decisions about turn left, turn right, etc are there on this leg
+  Leg Length - is this leg just too short or too long
+  Dog Leg - is this just coming back the same way we just went
+  Been This Way Before - how much of the leg to this numbered control has been travelled along already
+  Coming Back Here Later - do we run close to a future control on this leg
+  Only Go To The Finish At The End - primarily for MapRun, don't accidentally trigger the finish part way through the course
+  Last Control Is Near The Finish - don't make the run in annoyingly long
+  Didn't Move - this leg is so short the controls are basically in the same place

The system then mutates the controls on the candidates courses.

These replacements are fed back into the algorithm which re-scores the resulting courses and then repeats the process. 

In addition to the improvers, there are a number of hard constraints that the course must always satisfy:
+ Course Length - must be reasonably similar to the requested course length
+ Is Routeable - there must exist at least one safely runnable route around the whole course
+ Printable On Map - Is it possible to fit the course onto either a Landscape or Portrait A4 map at 5000, 7500, 10000, 12500 or 15000 scale


### Installing and Getting Started

StreetO is a java based piece of Software, primarily because the Graphhopper routing engine and the jenetics library that do a lot of the heavy lifting are written in Java.

Maven build system
```
mvn clean verify
```
To get everything to compile.


### Importing Open Street Map data

Graphhopper works against a local database built from OSM protobuf files. You will need to download an appropriate openstreetmap protocol buffer file for the area you want to make courses for. Great-britain-latest.osm.pbf or similar for nationwide coverage - or choose a smaller one for your local area.

For details of pbf file downloads see <https://download.geofabrik.de/europe/great-britain.html>

The downloaded pbf file needs to go into a folder called extracts in order to initialise the Graphhopper database - see initGH in `GhWrapper.kt` for details of how this is done.



