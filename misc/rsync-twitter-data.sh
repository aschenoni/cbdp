#!/bin/bash

while [ 1 ]; do
	time rsync -av --delete /mnt/multidc-data/twitter mdc-s60:/mnt/multidc-data
	time rsync -av --delete /mnt/multidc-data/twitter mdc-s80:/mnt/multidc-data
	time rsync -av --delete /mnt/multidc-data/twitter mdc-p40:/mnt/multidc-data
	date
	sleep $((2*3600))
done
