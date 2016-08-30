FROM      java:8u45
MAINTAINER Corbel Team @ bq <corbel-dev@bq.com>

EXPOSE ${port}
EXPOSE ${port.admin}

ADD run.sh /
ADD ${project.name}-${project.version}-bin.tar.gz /
ADD logback.groovy /${project.name}/etc/logback.groovy
ADD default.properties /${project.name}/etc/default.properties

# Install ImageMagick

RUN apt-get update -y && apt-get install -y imagemagick

ENTRYPOINT ["/bin/sh", "/run.sh"]

CMD ["server"]
