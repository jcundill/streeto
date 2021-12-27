[back](./index.md)

## Loading and Saving Courses

StreetO stores generated courses as either KML or GPX files.

These are standard files and can be loaded and viewed in any GPS application or Google Earth.

The KML files generated are formatted to MapRun's guidelines https://maprunners.weebly.com/course-setting.html

GPX files store the Controls as Waypoints and the generated best route around that course as the track.

File -> Open shows a FileChooser dialog to select a KML or GPX file.

The file the course was loaded from is remembered and changes to the course can be saved to the same location.

File -> Save As saves a copy of the current course to a new location as either a KML or GPX file.

### Loaded Courses and the Desired Course Length

Information about the requested course distance chosen when the course was initially
seeded is not stored in the GPX or KML files.

Instead, the requested distance is set to the current distance of the best route 
around the course.

Repeatedly loading a course, improving the existing controls on it and saving it again tends
to result in the course getting slowly longer and longer - courses slightly longer than
the requested distance are the ones that generally get selected by the algorithm.

If this proves problematic, then seed the course with its existing controls 
and choose a slightly shorter distance.

[back](./index.md)