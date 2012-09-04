goldwrap
========

''goldwrap'' is a wrapper around the Gold user management system (http://www.clusterresources.com/products/gold-allocation-manager.php) in order to make it accessible via a webservice.

# Download

Releases can be found here: http://code.ceres.auckland.ac.nz/nexus/content/repositories/releases/nz/org/nesi/goldwrap/

API documentation for current snapshot version: http://gold.dev.nesi.org.nz:8080/goldwrap/


# Development

## Requirements

 * Java (version >= 6)
 * maven (version >= 3 -- http://maven.apache.org/)

### Building

    cd <wherever>/goldwrap
    mvn clean install
	# or, in order to also build a debian package:
    mvn clean install -P deb 
	
Build artifacts are in target/ directory...

### Run in Jetty

    mvn jetty:run-war
    
to debug from Eclipse:

    export MAVEN_OPTS="-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044"
    
then debug in Eclipse as remote application, setting port 1044

### Configure to access gold via ssh

Config directory in $HOME/.goldwrap, file named "goldwrap.config":

    prefix=ssh gold@gold.dev.nesi.org.nz /opt/gold/bin/
    # printing out commands it sends to gold (including stdout & stderr)
    debug=true


