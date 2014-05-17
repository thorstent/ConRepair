Building ConRepair
==================

You need Java 7 and Maven on Linux. Oracel Java 7 can be installed on Ubuntu as follows:

	sudo add-apt-repository ppa:webupd8team/java
	sudo apt-get update
	sudo apt-get install oracle-java7-installer

We now install maven

	sudo apt-get install maven


Compile:
The first command ensures that the maven-repo gets cloned first.

	git submodule update --init --recursive
	cd ConRepair
	mvn compile assembly:single

The warning "Multiple versions of scala libraries detected!" can be safely ignored.

Running ConRepair
=================

The jar file can be invoked with 

	java -jar ConRepair/target/ConRepair-1.0-jar-with-dependencies.jar input.c

The --help switch reveals more command line parameters.

Output
------
The output that is generated is the same as the ouput that is checked in at the test-cases directory of this GIT. See the readme in that folder.

