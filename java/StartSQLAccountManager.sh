#!/bin/sh
java -Djava.util.logging.config.file=config/console.cfg -cp c3p0-0.9.1.2.jar:l2pserver.jar:mysql-connector-java-5.1.6-bin.jar l2p.accountmanager.SQLAccountManager
