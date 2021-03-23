#!/usr/bin/env bash

curl -O https://www.antlr.org/download/antlr-4.7.1-complete.jar
java -cp antlr-4.7.1-complete.jar org.antlr.v4.Tool -package org.hamidelmaazouz.graaljulia.parser -no-listener language/src/main/java/com/oracle/truffle/sl/parser/SimpleLanguage.g4
