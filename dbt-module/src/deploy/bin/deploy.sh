#!/bin/bash

BRANCH=${branch}
CLI_HOME=${cli.home}
MYCORE_HOME=${mycore.home}
TOMCAT_HOME=${tomcat.home}
SOLR_HOME=${solr.home}

echo "Install DBT Module..."
rm -rf $MYCORE_HOME/$BRANCH/lib/dbt-module-*.jar
mv dbt-module-*.jar $MYCORE_HOME/$BRANCH/lib/

echo "Install SOLR Schema..."
mv solr/schema.xml $SOLR_HOME/$BRANCH/conf/schema.xml

echo "Install MIR WAR..."
rm -rf $TOMCAT_HOME/webapps/$BRANCH
mv mir-*.war $TOMCAT_HOME/webapps/$BRANCH.war

echo "Install MIR CLI..."
mkdir -p $CLI_HOME/$BRANCH/
rm -rf $CLI_HOME/$BRANCH/mir-cli-*
tar xf mir-cli-*.tar.gz -C $CLI_HOME/$BRANCH/
sed -i "s/mir/$BRANCH/g" $CLI_HOME/$BRANCH/mir-cli-*/bin/mir.sh
$CLI_HOME/$BRANCH/mir-cli-*/bin/mir.sh process setup-commands.txt
