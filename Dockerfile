# Docker image for springboot file run
# VERSION 1.0.0
# Author: linrol
# 基础镜像使用java
FROM java:8

ADD mirai.tar.gz /web/app/mirai/

CMD sleep 200000