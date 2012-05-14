import AndroidKeys._

libraryDependencies += "local.nodens" %% "linkmodel" % "1.0"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4"

resolvers += "Virtual-Void repository" at "http://mvn.virtual-void.net"

addCompilerPlugin("net.virtualvoid" %% "scala-enhanced-strings" % "0.5.2")
