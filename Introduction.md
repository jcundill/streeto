[back](./index.md)

## Introduction

With the increasing acceptance of MapRun style courses within the Orienteering Community, the need for a planner to go
out and actually hang, or gripple, controls so that there can be an orienteering event, is starting to become less
ubiquitous.

Informal, adhoc, MapRun style GPS based courses have proven to be very popular especially during the recent COVID
restrictions on Orienteering events.

The problem is there needs to be a planned, and reasonably interesting, course for you to navigate around in the first
place.

Sure, you can go for a run around the block anytime, but that's not really Orienteering.

We like route choice options and challenging legs "is it better to go around, even though it is a bit longer, or follow
the maze of footpaths through that housing estate?"

This is the problem that StreetO is trying to solve. Can we use AI to automatically generate a decent orienteering
challenge based solely on freely available map data?

Whilst it's outputs will never come close to matching Short Green on a World Masters, it is pretty good in the following
kind of scenarios:

I'm bored, a 10K urban around Hampton Court would be a pretty cool thing to do this evening. Only Orienteers have ever
said this.

Let's take the kids and make Daddies phone (or watch) go beep by following the purple lines around a map. Let's do this
at Grannies on Sunday, everyone would love that. Again, it is probably true that only Orienteers have ever said this.

So, tag an easily accesible start and finish location on Open Streetmap - my front door, the pub - and tell the software
how far a reasonable run/walk/meander would be right now and then just let the AI 'scurry' around OpenStreetmap data
extracts of the area in question to try to find a reasonable orienteering challenge that meets these constraints rather
than trying to plan it all out yourself.

### Constraints, Limitations, etc.

The software only recognises what OpenStreetMap tells it is on the map and what it's routing algorithms tell it about
that data from the map.

So, if OpenStreetMap data is tagged such that the slip road onto the M69 at junction 2 has a footpath for pedestrians (
it doesn't) then the AI will quite happily place a control where that slip road meets the M69 because you could quite
safely follow that footpath to get to it.

Similarly, if OpenStreetMap says that the service road leading to the nuclear waste containment facility at Hinckley C
is open for public access (it doesn't), then the AI will quite happily place a control next to the cooling tower at that
containment facility, because as far as it can tell. there is no reason you shouldn't just be able to run along that
service road to get to it.

We exclude most potentially dodgy or dangerous locations as far as we can given the underlying OpenStreetMap data. You
can't go for a run in the slow lane of a motorway or across areas designated with limited access, so the AI doesn't even
consider them.

Caveat Emptor, use your common sense, if you just can't, or really shouldn't, get to control 5 via the most obvious, or
the only, route from here - then don't, just skip it and move onto the next one. It's not as though you'll lose BOF
ranking points for doing this - and as a good citizen, just go and fix OpenStreetMap after you've got home from your run
adding in the appropriate way designations for the bit you couldn't do.

I have this exact issue with a service road through an electrical substation just around the corner from where I live.

It happens infrequently, the AI generally does a good job, but obviously not every possible route can be vetted up
front, I've no idea how accurate the OpenStreetMap data for the area just south of Alice Springs in Australia is, for
example, so use your common sense and have a look over the map before you set off. You can always just generate another
one if this one is no good.

In a similar vein, the routing algorithm used on top of the OpenStreetMap data is completely path/track/road driven. So
for an urban park or playing field with mapped paths around the perimeter it is blind to the fact that you can just run
straight across the middle of this area as it has no routing information for this option.

Unlike on most traditional TD3/4/5 Orienteering Courses, where this is a valid, and planned for option, StreetO's AI is
convinced that you can never leave the path and navigate on a bearing to your next control.

This makes it largely unsuited to many forest, or other open access land, based courses.

Where it excels is in Urban Orienteering course discovery.

Bung a virtual start kite next to the Barbican Centre in London, or next to the Tesco garage in a dismal housing estate
on the southern edge of Nuneaton, tell it to find something about 8 or 9K long and to place around 20 Controls, and
you'll probably have a decent orienteering challenge generated for you.

[back](./index.md)