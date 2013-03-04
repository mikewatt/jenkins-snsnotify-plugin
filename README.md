snsnotify-plugin
================

Sends build notifications to an AWS SNS topic which are designed to be consumed by:
https://github.com/jkelabora/raspberry-pipeline

- ```git clone https://github.com/jkelabora/snsnotify-plugin && cd snsnotify-plugin && mvn clean install```
- (wait for mvn to download the internet)
- manage jenkins / plugins -> advanced -> upload ```./target/snsnotify.hpi```
- restart jenkins ([$JENKINS_URL]/restart)
- create a SNS Topic, subscribe a target SQS queue
- right-click on targeted SQS queue to confirm subscription 
- configure jenkins to use AWS creds and newly created Topic ARN
