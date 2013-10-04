#!/bin/bash

while [ 1 ]; do
	echo "mdc-s60"
	time rsync -av --delete /mnt/multidc-data/twitter mdc-s60:/mnt/multidc-data
	echo
	echo "mdc-s80"
	time rsync -av --delete /mnt/multidc-data/twitter mdc-s80:/mnt/multidc-data
	echo
	echo "mdc-p40"
	time rsync -av --delete /mnt/multidc-data/twitter mdc-p40:/mnt/multidc-data
	echo
	SLEEP_SEC=3600
	echo `date`" Sleeping for "$SLEEP_SEC" seconds ..."
	sleep $SLEEP_SEC
	echo
done
