#! /bin/bash
rm -rf bin/*.class
javac -cp ".;java/lib/postgresql-42.1.4.jar;" java/src/DBproject.java -d bin/
