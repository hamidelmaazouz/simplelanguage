# The Graaljulia native image

Truffle language implementations can be AOT compiled using the
GraalVM [native-image](https://www.graalvm.org/docs/reference-manual/aot-compilation/)
tool. Running `mvn package` in the graaljulia folder also builds a `graaljulia-native` executable This executable is the
full Julia language implementation as a native application, and has no need for a JVM to run.
