Analog Probe Data Collector
===========================

This is an experimental embedded system for reading analog data from a sensor by an Arduino board.
The data is read by a reader service running in a local Raspberry Pi (or a Linux PC).
The data is sent to a web service that displays it graphically.

Features
========
 - A data collector servlet to receive data over HTTP
 - A Vaadin UI for visualizing the data
 - Written in Scala

Complete System
===============

The complete data collection system consists of:
 - An Arduino board for collecting data from probes
 - A Raspberry Pi for forwarding the data to a collector
 - A collector web service
 - A Vaadin UI for visualizing the data

This project does not currently include the Arduino or Pi code,
it will be added in future.

Development
===========
 - Install Scala IDE
 - Install Vaadin Plugin for Eclipse
 - Correct scala-library.jar must be installed in WebContent/WEB-INF/lib
 - Import as a Scala project - I haven't tried how this works

Copyright 2014 Marko Grönroos (magi@iki.fi)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

3rd Party Licenses
==================

This application uses Vaadin Charts library, which is a commercial product
licensed under a separate commercial license.

  https://vaadin.com/add-ons/charts

License for Vaadin Charts is required for using this software.

