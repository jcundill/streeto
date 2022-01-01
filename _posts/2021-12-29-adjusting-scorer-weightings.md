---
layout: post
title:  "Adjusting Scorer Weightings"
date:   2021-12-29 11:20:17 +0000
categories: streeto scorers
---

Wrote this up for the [User Guide](/UserGuide.html). Thought it might make sense to give a few 
examples here.

StreetO works by using a Genetic Algorithm to evolve a population of candidate orienteering courses such that the
legs on those courses would make for an interesting orienteering challenge as much as possible.

It does this by scoring each candidate course against a set of heuristics. It then retains the best scoring candidates
and feeds them into the next generation of the algorithm


You assign weightings to each of these heuristics in the
[Settings And Preferences](/SettingsAndPreferences.html#course-scoring-preferences) dialog to tell the algorithm how
much importance you want to give to each of these factors.

There is no right or wrong way to weight the heuristics. To some extent it depends on the map data at the location
you're generating a course for, and to some extent it depends on your personal preference.

Route Choice is probably the most important heuristic in an urban orienteering course. But you can't just switch
everything else off, or you'll end up with a course that that just oscillates between different locations
that have great route choice options between them, but basically just runs the same legs over and over again as you said you
didn't care about route repetition, or exposing future controls.

An example of this is shown below, here everything apart from Route Choice is given a weighting of 0.

![Image](/doc/routechoiceonly.png)

It mostly looks ok, and there are good route choice options on nearly all the legs, the options for the leg from the first 
to the second control is overlaid as an example. But there are definite route repetition and similar problems around
controls 3, 4 and 5, and especially around 8, 9 and 10.

Zooming in to that latter area we can see that the best route in this area makes you run past control 10 on you way to 9
and then you just retrace your steps

![Image](/doc/routechoiceonlyzoom.png)


This is not surprising as we told the system that we didn't care about route repetition or running past future controls.

The other thing that's apparent on 8, 9, 10 block is that there is no navigational challenge at all in those areas. It is
literally just turn the corner, and you are done.

If we take the other extreme, and set everything to 0 apart from Leg Complexity, we end up with something that looks
like this.

![Image](/doc/complexityonly.png)

As can be seen by the wiggly blue line, the legs involve lots of navigational decisions, but the course itself is a mess.
Control 1 is placed in exactly the same location as control 3,  the leg to control 1 is probably a pretty interesting run,
but from there it goes downhill rapidly. Again, this is expected as we told the system that we didn't care about these other
route factors.

So you do need to give some importance to the structural factors affecting the course, things like repetition of legs, running
past future controls, leg lengths, etc.

As an example setting everything to 0 apart from Leg Length, Avoid Future Controls, Avoid Dog Legs and Avoid Repetition, we end up with something
that looks like this.

![Image](/doc/structuralonly.png)

The system thinks that this is an almost perfect course, and it certainly meets the structural requirements, but it's not
really a very interesting run. the leg from 1 to 2 is a good example, this leg is 700 m long, for the majority of it you
 are just running along the same road. 7 to 8, run a couple of hundred metres along the road, and you are there, etc.

So we need to get the system to balance the structural factors with the interest factors to generate a decent course.

The issue here is that if you weight the structural elements too highly, they tend to swamp the interest factors, complexity
and route choice, on the legs.

The default settings set everything to 1, both structural and interest, and you end up with things that look like this.

![Image](/doc/balanced.png)

Not too bad, as the wiggly blue line shows, there is plenty of navigational complexity in the course, and the course itself is
has pretty decent route choice options on most of the legs, the choice for 6 to 7 is overlaid as an example.

But do I really care about banning dog legs completely and having a bit of repetition if this is at the expense of squeezing
as much as possible out of the interest factors on the course?

Score is based on the mean of the factors, the squares of the weighted scores for each factor of the leg are added up, and the 
mean is used as the leg score. So with everything on 1, route choice and complexity account for 14.3% of that legs score each.

In the examples used above, they are generated from a location that offers plenty of complexity and route choice options,
(52.942, -1.431).

What I find works well for me in my local area is something like this:

![Image](/doc/coursescorerprefs.png)

I don't mind dog legs so much, as long as the dog leg route is just one of the potential route choices, or it's not so long.

In this location I seem to be getting better results with something like this:

![Image](/doc/oakwoodsettings.png)

For example 

![Image](/doc/oakwoodfinal.png)

Although, I don't know, I think I might prefer the 'everything set to one'  option here.

Re-scoring the course with the 'tweaked' setting on purely its route choice scores gives me

![Image](/doc/finalscores.png)

So, there are a lot of good route choice legs on this one. I haven't run it. But it looks like it would be fun.



Note, what we haven't talked about here is the Control Site Placement Weighting, this is another Interest Factor in a way.
It makes for a more interesting run if you are looking for something like a post box on this leg than it does if you know you just need
to get to that road junction up ahead. There is more of the 'ah, there is it' type of experience with the former than the latter.

Weighting too heavily in favour of Control Site Placement, does impact the route choice and complexity factors, if we
end up just scoring a leg badly based on whether the control at the end is furniture or not, we end up ignoring a lot of
otherwise very good legs.

