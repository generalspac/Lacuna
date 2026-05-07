#!/bin/bash
rm -rf bin
mkdir bin
javac -d bin src/global/*.java src/model/*.java src/view/*.java src/controller/*.java src/main/*.java