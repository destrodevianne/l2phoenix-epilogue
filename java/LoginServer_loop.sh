#!/bin/bash

DBHOST=localhost
USER=root
PASS=
DBNAME=l2pdb

while :;
do
	#mysqlcheck -h $DBHOST -u $USER --password=$PASS -s -r $DBNAME>>"log/`date +%Y-%m-%d_%H:%M:%S`-sql_check.log"
	#mysqldump -h $DBHOST -u $USER --password=$PASS $DBNAME | gzip > "backup/`date +%Y-%m-%d_%H:%M:%S`-"$DBNAME"_loginserver.gz"
	mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	nice -n -2 java -server -Xms32m -Xmx32m -cp javolution.jar:c3p0-0.9.1.2.jar:mysql-connector-java-5.1.6-bin.jar:jacksum.jar:l2pserver.jar l2p.loginserver.L2LoginServer > log/stdout.log 2>&1
	[ $? -ne 2 ] && break
	sleep 10;
done
