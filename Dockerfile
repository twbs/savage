# Written against Docker v1.5.0
FROM java:8
MAINTAINER Chris Rebert <code@chrisrebert.com>

WORKDIR /

RUN ["apt-get", "install", "git"]
RUN ["apt-get", "install", "openssh-client"]
RUN ["useradd", "savage"]

ADD target/scala-2.11/savage-assembly-1.0.jar /app/server.jar
ADD git-repo /app/git-repo

ADD ssh/id_rsa.pub /home/savage/.ssh/id_rsa.pub
ADD ssh/id_rsa /home/savage/.ssh/id_rsa

RUN ssh-keyscan -t rsa github.com > /home/savage/.ssh/known_hosts

RUN ["chown", "-R", "savage:savage", "/home/savage/.ssh"]
RUN ["chown", "-R", "savage:savage", "/app/git-repo"]
# chmod must happen AFTER chown, due to https://github.com/docker/docker/issues/6047
RUN ["chmod", "-R", "go-rwx", "/home/savage/.ssh"]

WORKDIR /app/git-repo
USER savage
CMD ["java", "-jar", "/app/server.jar", "6060"]
EXPOSE 6060
