---
layout: page
title: About
permalink: /about/

---


StreetO is a desktop application that uses goal based search algorithms to automatically generate urban orienteering
courses from OpenStreetMap data.

It uses routing algorithms on top of OSM data to try and find a number of challenging course legs that would make
for an interesting urban orienteering course of a given distance at a specified location.

The start location is specified as a parameter to the algorithm and can be anywhere in the
world as long as an OSM protobuf file covering that location has been loaded into the system.

![Image](./doc/uioverview.png)

StreetO software is open source and available under the [MIT license](https://opensource.org/licenses/MIT).

Windows, macOS and Debian installers are available for download from [GitHub](https://github.com/jcundill/streeto/releases).

Note that currently the installers are not digitally signed, this will be fixed in the future. But for now they will
trigger warning messages.


