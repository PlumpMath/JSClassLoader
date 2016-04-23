JSClassLoader

Allows adding jars on the fly and loads, recurively all the jar files in the
path specified in the JSLIBDIR environment variable.

I use this to launch java with my custom classloader like this:

JSLIBDIR=~/workspace/lib/ java -Djava.system.class.loader=com.javasteam.tools.classloader.JSClassLoader com.some.class

