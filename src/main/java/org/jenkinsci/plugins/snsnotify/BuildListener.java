package org.jenkinsci.plugins.snsnotify;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

@Extension
public class BuildListener extends RunListener<AbstractBuild> {

    @Override
    public void onStarted(AbstractBuild abstractBuild, TaskListener listener) {
        AmazonSNSNotifier trigger = AmazonSNSNotifier.getNotifier(abstractBuild.getProject());

        if (trigger == null) {
            return;
        }

        trigger.onStarted(abstractBuild, listener);
    }

    @Override
    public void onCompleted(AbstractBuild abstractBuild, TaskListener listener) {
        AmazonSNSNotifier trigger = AmazonSNSNotifier.getNotifier(abstractBuild.getProject());

        if (trigger == null) {
            return;
        }

        trigger.onCompleted(abstractBuild, listener);
    }

}
