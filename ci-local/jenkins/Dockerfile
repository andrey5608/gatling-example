FROM jenkins/jenkins:lts-jdk17

USER root
RUN apt-get update && apt-get install -y jq maven python3-pip docker.io && rm -rf /var/lib/apt/lists/*
RUN curl -LO https://mirror.openshift.com/pub/openshift-v4/clients/ocp/stable/openshift-client-linux.tar.gz \
    && tar -xzf openshift-client-linux.tar.gz -C /usr/local/bin oc kubectl \
    && rm openshift-client-linux.tar.gz
RUN groupadd -f docker && usermod -aG docker jenkins && usermod -aG root jenkins

USER jenkins
COPY casc /var/jenkins_home/casc
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
