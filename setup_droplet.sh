#!/bin/bash

# Step 0: You need to have copied the assembly JAR to savage/target/scala-2.10/savage-assembly-1.0.jar
# Step 0.1: You need to have the git repo checked out in ./git-repo
# Step 0.2: The user's SSH public-private keys must be at ./ssh/id_rsa and ./ssh/id_rsa.pub

# set to Pacific Time (for @cvrebert)
# ln -sf /usr/share/zoneinfo/America/Los_Angeles /etc/localtime

# remove useless crap
aptitude remove wpasupplicant wireless-tools
aptitude remove pppconfig pppoeconf ppp

# setup firewall
ufw default allow outgoing
ufw default deny incoming
ufw allow ssh
ufw allow www
ufw enable
ufw status verbose

# setup Docker; written against Docker v1.2.0
docker build . 2>&1 | tee docker.build.log
IMAGE_ID="$(tail -n 1 docker.build.log | cut -d ' ' -f 3)"
docker run -d -p 80:6060 --name savage $IMAGE_ID
