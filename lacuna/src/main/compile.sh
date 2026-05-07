#!/bin/bash
rm -rf bin
mkdir bin
javac -d bin java/global/*.java java/model/*.java java/view/*.java java/controller/*.java java/main/*.java