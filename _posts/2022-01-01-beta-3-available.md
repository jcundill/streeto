---
layout: post
title:  "Released the 3rd Beta"
date:   2022-01-01 08:39:09 +0000
categories: streeto releases
---
The 3rd beta release of the StreetO installation packages is now available.

Windows, macOS and Debian installers are available for download from [GitHub](https://github.com/jcundill/streeto/releases).

This release finally renders the course overlay on the map correctly. So the lines terminate
at the edges of the control circles, and the start and finish controls now render using
the regular orienteering map symbols used for them.

Also, the Control Site finding logic now properly looks for street furniture at the current location first.
It was 'snapping' to the nearest bend or junction first and then looking for street furniture.

Also, the map splitting options now work as expected when selected in the Preferences Panel.

Getting a reasonably scaled, and therefore readable, map is a bit of a challenge. Most
folks only have access to an A4 printer at home, and printing a reasonably long course on
paper that size quite often results in small scale maps.

Splitting that map into two sheets helps enormously here.

There are two methods of splitting the map available in the Mapping Preferences:

##### Print A3 Maps on A4 Paper

Essentially, this method allows you to specify an A3 paper size but the actually
print the map on two A4 sheets.

This is quite a naive approach, it just splits  the original map down the middle.

Note: It is literally split the map down the middle, there are no 'map exchange' style controls here. The purple lines just
go off the edge of the paper.

This is always guaranteed to improve the map readability greatly, and give you a better map scale, but it can be a bit annoying when navigating legs
that start on one sheet and finish on the other.

##### Split Map for Better Scale

This option emulates the traditional double-sided orienteering map approach, in that it prints the 
map over two pieces of paper, and places 'map exchange' style controls, where that control is shown on both map sheets
and there are no purple lines going off the edge of the map.

It uses an algorithm that considers all possible pairs of controls on the course, and splits the map
at the control pair that results in the best scale for both of the resulting parts.

For example, if the course needs a 1:10000 scale map to fit it all onto, then it may be possible to split the course up
into two pieces, and print each of those pieces on a 1:7500 scale map.

Note: unlike most double-sided orienteering maps, what you end up with here is a map exchange control that
takes you onto the second map sheet, and a further map exchange control that takes you back to the first map sheet again.

This isn't guaranteed to work, as it depends on the shape of the original course, but it is a good option for getting a
better map scale.

If you specify A3 paper with this option then the resulting map will be printed on two A3 sheets.

---

On the roadmap is adding an address lookup feature to the menu rather than having to manually zoom out pan around 
and zoom back in to get to the desired location.

After that, I think we're probably ready to merge this code back onto the master branch and take the 
pre-release tag off the installers.