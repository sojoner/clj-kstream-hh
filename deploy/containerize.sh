#!/bin/bash
mv ../target/clj-kstream-hh.jar .
docker build --tag "sojoner/clj-kstream-hh:0.1.0" .
