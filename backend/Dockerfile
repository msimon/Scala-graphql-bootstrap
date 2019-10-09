FROM ubuntu:18.10

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl gnupg2 ca-certificates build-essential netcat && \
    update-ca-certificates

RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add

RUN apt-get update && apt-get install -y --no-install-recommends default-jdk sbt

RUN mkdir /src
RUN mkdir /sbt
WORKDIR /src

COPY . /src/
