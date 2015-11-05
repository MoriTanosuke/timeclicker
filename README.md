Simple time tracking application hosted on Google AppEngine
-----------------------------------------------------------

[![Build Status](https://travis-ci.org/MoriTanosuke/timeclicker.svg)](https://travis-ci.org/MoriTanosuke/timeclicker)

This application fits my needs of a simple 1-button application to track my time spent in the office. It's just that: one button to start/stop tracking and a simple webpage displaying the overall sum of time that I spent in the office.

I'm working on a simple Android app as well, but the tracking will be useable via a regular browser from any device.

What I am working on
--------------------

* Better webpage to start/stop
* Android app
* Better webpage to display daily/weekly/monthly sums

Using the Dockerfile
--------------------

You can use the included *Dockerfile* to compile this application:

````
docker run -it --rm --name my-maven-project -v "$PWD":/usr/src/mymaven -w /usr/src/mymaven maven:3-jdk-8-onbuild mvn clean install
````