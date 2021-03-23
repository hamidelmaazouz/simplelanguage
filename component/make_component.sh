#!/usr/bin/env bash

declare -r JAVA_VERSION="${1:?First argument must be java version.}"
declare -r GRAALVM_VERSION="${2:?Second argument must be GraalVM version.}"
if [[ $JAVA_VERSION == 1.8* ]]; then
    JRE="jre/"
elif [[ $JAVA_VERSION == 11* ]]; then
    JRE=""
else
    echo "Unkown java version: $JAVA_VERSION"
    exit 1
fi
readonly COMPONENT_DIR="component_temp_dir"
readonly LANGUAGE_PATH="$COMPONENT_DIR/$JRE/languages/sl"
if [[ -f ../native/graaljulia-native ]]; then
    INCLUDE_GRAALJULIA-NATIVE="TRUE"
fi

rm -rf COMPONENT_DIR

mkdir -p "$LANGUAGE_PATH"
cp ../language/target/simplelanguage.jar "$LANGUAGE_PATH"

mkdir -p "$LANGUAGE_PATH/launcher"
cp ../launcher/target/sl-launcher.jar "$LANGUAGE_PATH/launcher/"

mkdir -p "$LANGUAGE_PATH/bin"
cp ../sl $LANGUAGE_PATH/bin/
if [[ $GRAALJULIA-NATIVE = "TRUE" ]]; then
    cp ../native/slnative $LANGUAGE_PATH/bin/
fi

touch "$LANGUAGE_PATH/native-image.properties"

mkdir -p "$COMPONENT_DIR/META-INF"
{
    echo "Bundle-Name: Simple Language";
    echo "Bundle-Symbolic-Name: org.hamidelmaazouz.graaljulia";
    echo "Bundle-Version: $GRAALVM_VERSION";
    echo "Bundle-RequireCapability: org.graalvm; filter:=\"(&(graalvm_version=$GRAALVM_VERSION)(os_arch=amd64))\"";
    echo "x-GraalVM-Polyglot-Part: True"
} > "$COMPONENT_DIR/META-INF/MANIFEST.MF"

(
cd $COMPONENT_DIR || exit 1
jar cfm ../graaljulia-component.jar META-INF/MANIFEST.MF .

echo "bin/sl = ../$JRE/languages/sl/bin/sl" > META-INF/symlinks
if [[ $GRAALJULIA-NATIVE = "TRUE" ]]; then
    echo "bin/graaljulia-native = ../$JRE/languages/sl/bin/graaljulia-native" >> META-INF/symlinks
fi
jar uf ../graaljulia-component.jar META-INF/symlinks

{
    echo "$JRE"'languages/sl/bin/sl = rwxrwxr-x'
    echo "$JRE"'languages/sl/bin/graaljulia-native = rwxrwxr-x'
} > META-INF/permissions
jar uf ../graaljulia-component.jar META-INF/permissions
)
rm -rf $COMPONENT_DIR
