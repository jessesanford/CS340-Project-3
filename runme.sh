#!/bin/bash

export A_PORT="8787"


export A_DBG="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=$A_PORT,server=y,suspend=y"

java $A_DBG -jar /Users/jessesanford/NetBeansProjects/homework2/dist/homework2.jar
