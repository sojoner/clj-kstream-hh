#!/bin/bash
mv ../target/clj-kstream-hh.jar .
docker build --tag "sojoner/clj-kstream-hh:0.1.0" .
#docker tag <HASH> sojoner/clj-kstream-hh:0.1.0
#docker login
#docker push sojoner/clj-kstream-hh