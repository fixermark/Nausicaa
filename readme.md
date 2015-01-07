Nausicaa
========

A Google Glass client for the Telemachus plugin to Kerbal Space Program.

<img src="http://fixermark.com/images/nausicaa/status.png"><img src="http://fixermark.com/images/nausicaa/docking.png">

Features
========
For a full feature overview, see the intro video:

<iframe width="560" height="315" src="//www.youtube.com/embed/mEFK3JnkHJE" frameborder="0" allowfullscreen></iframe>

  * Display critical information on your ship's status (altitude, velocity, orbit, electric charge, fuel supply)
  * Docking view ("OK glass... docking view"). Shows RCS fuel remaining and the relative alignment of your craft to the docking target
  * Voice status updates and warnings for attaining circular orbit, bringing periapsis too close to a planet's atmosphere, and
    running low on electricity
    * Electricity warning includes automatic override to switch off time scaling (disable via "Ok glass... toggle alert guard"). This
      guards against my most common mistake... Failing to deploy solar panels after achieving orbit. ;)

How To Use
==========

  <ol>
  <li>Install Kerbal Space Program</li>
  <li>Install the Telemachus Plugin ([github project](https://github.com/richardbunt/Telemachus),
     [releases](https://github.com/richardbunt/Telemachus/releases))</li>
  <li>Compile and side-load Nausicaa into your Google Glass ([compile guide](https://developers.google.com/glass/develop/gdk/quick-start),
     [side-load tips](http://glassappz.com/ten-easy-steps-to-side-load-an-app-to-google-glass/))</li>
  <li>Run Kerbal Space Program, build a craft that includes the Telemachus components, and start the mission.</li>
  <li>Launch Nausicaa ("OK Glass, start a flight")</li>
  <li>Set up Nausicaa to point to your Telemachus instance:</li>
    <ol>
    <li>Set the telemetry source ("OK Glass, set telemetry source")</li>
    <li>Choose IP host ("OK Glass, Change Host.... one nine two dot one six eight dot one dot two")</li>
    <li>Choose IP port ("OK Glass, Change Port.... eight zero eight zero")</li>
    <li>Swipe down to return to the main view</li>
    </ol>
  <li> You should now see critical information about your ship in the main Telemachus view!</li>
  </ol>


P.A.Q. (Preemptively-asked questions)
=====================================

Compatibility
-------------
**What version of Telemachus / Kerbal Space Program will this work with?**

Tested with KSP 0.24 and Telemachus v.1.4.25.0. I don't anticipate compatibility issues with future versions of Telemachus / KSP,
but your mileage may vary.

Platform
--------

**I don't have Glass. Can I make this work on another device?**

I'm definitely receptive to patches to enable another platform. Anecdotally, I've tried launching this on a regular Android phone
and it's almost completely working (just some work needed on the menus to bind them in appropriately without Glass's voice support).
You may also be interested in [kerbal-watch](https://github.com/TronLaser/kerbal-watch), a Telemachus client for the Pebble watch.

**I don't really play Kerbal Space Program, but this UI is nifty. Can I make this work with another program?**

The software isn't architected to be modular in terms of adapting to another app, but I'm definitely not against it. Pass me a patch
or make a fork, and we'll see what we can do.

Licensing
---------

**I want to use parts of this, but my project isn't compatible with the Apache license.**

I'm extremely open to building forks that could be licensed on a case-by-case basis; contact me at iam+nausicaa@fixermark.com for more
information. Note that I don't own the copyright to the web-sockets library, so you will need to either independently contact that
library's owner or replace that library with your own solution for web-socket access.

Bugs
----

**It didn't work!**

If you're having issues, definitely submit a bug report on the
[issue tracker](https://github.com/fixermark/Nausicaa/issues), and I'll look
into it. Alternatively, you can reach me by email at iam+nausicaa@fixermark.com

**These colors are weird**

What, you don't like the traditional green and purple of aviation instruments? ;)

The green-purple color scheme is actually working around a weird bug where the multithreading into Websockets appears to be interacting
strangely with Glass's rendering; at a rate synchronized with how often Nausicaa gets data from Telemachus, the blue and red color channels
flip, leading to UI elements with blue and red in them blinking annoyingly. When I find the source of that bug, I'll switch over to a
red-yellow-green color scheme.

On the other hand, it's nice to be colorblindness-compliant!


Special Thanks
==============

Nausicaa would not have been possible without the following contributions:

 * [Richard Bunt](https://github.com/richardbunt/Telemachus), for authoring the Telemachus interface
   and being *incredibly* responsive to bugfixes and patches!
 * Amanda Leight, first tester besides myself.
