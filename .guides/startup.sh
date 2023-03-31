#!/usr/bin/env bash


SAMPLE_DATA=/home/codio/workspace/parkingsystem/resources/Data.sql
DB_NAME="prod"

# wait for mysql startup
RETRIES=50

# wait sql to start
until mysql -e "\q" > /dev/null 2>&1 || [ $RETRIES -eq 0 ]; do
  echo "Waiting for server, $((RETRIES--)) remaining attempts..."
  sleep 1
done

if mysql ${DB_NAME} -e '\q' 2>&1; then # checking is data already exists
   echo "database ${DB_NAME} exists"
else
  # populating the database
  for f in $SAMPLE_DATA
  do
    echo "Processing $f file..."
    mysql < "$f"
  done
fi
