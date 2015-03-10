# xml2csv

This is a command line utility and library to convert XML files to CSV files, according to a configuration provided by the user.

This software is maintained on Github at http://www.github.com/andybrodie/xml2csv.  See the [Github project wiki](https://github.com/andybrodie/xml2csv/wiki) to get started, specifically the [Tutorial](https://github.com/andybrodie/xml2csv/wiki/Tutorial).

## Key Features:
* Using a user-supplied configuration, xml2csv reads input XML files and creates output CSV files.
* Processes multiple XML files that match user-specified filters.
* Create many CSV files from a single set of XML files based on 1..n relationships between XML nodes and their descendants.
* Built as a standalone Jar file with no dependencies other than the [Oracle Java 1.6 JRE or later](http://www.java.com), for easy deployment.
* Inline, multi-value mappings, allowing for a variable number of values for a single record.
* Pivot Mapping allow CSV structure to be determined by the contents of XML files.
* Paid support available from [Locima Ltd](http://locima.com).
* Scalable to as many XML input documents as you need.  Only one XML document needs to be held in memory at one time.
* Use via a command line interface or integrated in to your own applications.
* Source code licensed under the Microsoft Public License ([MS-PL](http://opensource.org/licenses/MS-PL)), so you can exploit with confidence!
* Fully commented source code so you can understand and extend easily.
* Based on open source software ([Apache Commons](http://commons.apache.org), [Apache Xerces](http://xerces.apache.org/), [QOS slf4j](http://slf4j.org/), [Saxonica Saxon HE](http://sourceforge.net/projects/saxon/files/Saxon-HE/), [Eclipse](http://www.eclipse.org) Jar-In-Jar Loader).
* Build with [Apache Ant](http://ant.apache.org) and [Ivy](http://ant.apache.org/ivy/) using a single command (`ant build-jar`).

## Third Party Dependencies:

xml2csv relies on the following third party software, each with it's own separate license:

Apache Commons CLI (http://commons.apache.org/proper/commons-cli/)
        Licensed under the Apache License Version 2.0 (http://www.apache.org/licenses/)

Apache Xerces 11.2 (http://xerces.apache.org/)
        Licensed under Apache License Version 2.0 (http://xerces.apache.org/xml-commons/licenses.html)

Jar-in-Jar-Loader (http://www.eclipse.org)
        Licensed under the Eclipse Public License 1.0 (http://www.eclipse.org/org/documents/epl-v10.php)

Junit 4.0 (http://junit.org/)
        Licensed under Eclipse Public License Version 1.0 (http://junit.org/license.html)

Qos Logback (http://logback.qos.ch/)
        Licensed under the Eclipse Public License (http://logback.qos.ch/license.html)

Qos SLF4J (http://slf4j.org/)
        Licensed under the MIT licence (http://slf4j.org/license.html)

Saxonica Saxon 9 HE (http://saxon.sourceforge.net/)
        Licensed under Mozilla Public License 1.0 (https://www.mozilla.org/MPL/1.0/)

Copyright 2014-2015 Locima Ltd. (e-mail: `enquiries (at) locima.com` or website: www.locima.com)
Licensed under the Microsoft Public License (http://www.microsoft.com/en-us/openness/licenses.aspx)
