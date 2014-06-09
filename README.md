snsnotify-plugin
================

Sends build notifications to an AWS SNS topic which are designed to be consumed by:
https://github.com/jkelabora/raspberry-pipeline

1. `git clone https://github.com/jkelabora/snsnotify-plugin`
2. `cd snsnotify-plugin`
3. Open `pom.xml` and modify the line 8 to reflect the Jenkins version you have installed
4. `mvn clean install`
5. (wait for mvn to download the internet)
6. Manage Jenkins > Plugins > Advanced > Upload ```./target/snsnotify.hpi```
7. Restart Jenkins ([$JENKINS_URL]/restart)

Now, login to AWS and do the following:

1. Create an SNS Topic, subscribe a target SQS queue
2. Right-click on targeted SQS queue to confirm subscription 

Finally, back to Jenkins...

1.  Manage Jenkins > Configure Jenkins to use AWS creds and newly created Topic ARN
