import org.apache.tools.ant.DirectoryScanner

// Ant 1.10.17 adds this entry after Gradle 9.6.1 snapshots its legacy default-exclude set.
DirectoryScanner.removeDefaultExclude("**/.gitignore")

rootProject.name = "m3fx"

include("demo")
include("catalog")
