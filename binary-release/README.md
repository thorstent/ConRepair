HTTP Download
=============

An HTTP download is not possible from github. You need to clone the repository.

Prerequisites
=============
The binary has been tested on Ubuntu x64, but it should also run on any other Linux.

The only software needed is **Java 7**, everything else is bundled. For testing we used Oracel Java 7, which can be installed on Ubuntu with these commands:

	sudo add-apt-repository ppa:webupd8team/java
	sudo apt-get update
	sudo apt-get install oracle-java7-installer

Running ConRepair
=================

The jar file can be invoked with

	java -jar ConRepair-1.0-jar-with-dependencies.jar input.c

For further commandline switch use

	java -jar ConRepair-1.0-jar-with-dependencies.jar --help

Output
------
The output that is generated is the same as the ouput that is checked in at the test-cases directory of this GIT. See the readme in that folder.

Updates
=======
This binary was created as part of the CAV2014 submission and will not be updated. For the latest version be sure to compile from source.