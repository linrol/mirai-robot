#!/bin/bash
app='spring-coolq.jar'
args='-Xms1024M -Xmx2048m'
cmd=$1
pid=`ps -ef|grep java|grep $app|awk '{print $2}'`

startup(){
  nohup /usr/local/java/jdk1.8.0_201/bin/java -jar $args $app >> /root/web/app/coolq/logs/spring-coolq-`date +%Y%m%d`.log 2>&1 &
}

if [ ! $cmd ]; then
  echo "Please specify args 'start|restart|stop'"
  exit
fi

if [ $cmd == 'start' ]; then
  if [ ! $pid ]; then
    startup
  else
    echo "$app is running! pid=$pid"
  fi
fi

if [ $cmd == 'restart' ]; then
  if [ $pid ]
    then
      echo "$pid will be killed after 3 seconds!"
      sleep 3
      kill -9 $pid
  fi
  startup
fi

if [ $cmd == 'stop' ]; then
  if [ $pid ]; then
    echo "$pid will be killed after 3 seconds!"
    sleep 3
    kill -9 $pid
  fi
  echo "$app is stopped"
fi