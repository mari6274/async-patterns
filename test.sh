#!/bin/zsh

ab -n 10000 -c 100 -T "application/json" -p /tmp/post http://localhost:8080/applications/submit
