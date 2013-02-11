package org.jenkinsci.plugins.snsnotify;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import hudson.Launcher;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class AmazonSNSNotifier extends Notifier {

    private final String projectTopicArn;

    @DataBoundConstructor
    public AmazonSNSNotifier(String projectTopicArn) {
        this.projectTopicArn = projectTopicArn;
    }

    public String getProjectTopicArn() {
        return projectTopicArn;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        String awsAccessKey = getDescriptor().getAwsAccessKey();
        String awsSecretKey = getDescriptor().getAwsSecretKey();
        String publishTopic = isEmpty(projectTopicArn) ? 
            getDescriptor().getDefaultTopicArn() : projectTopicArn;

        if (isEmpty(publishTopic)) {
            listener.error(
                    "No global or project topic ARN sent; cannot send SNS notification");
            return true;
        }

        if (isEmpty(awsAccessKey) || isEmpty(awsSecretKey)) {
            listener.error(
                    "AWS credentials not configured; cannot send SNS notification");
            return true;
        }

        String snsApiEndpoint = getSNSApiEndpoint(publishTopic);
        if (isEmpty(snsApiEndpoint)) {
            listener.error(
                    "Could not determine SNS API Endpoint from topic ARN: " + publishTopic);
            return true;
        }

        String subject = truncate(
                String.format("Build %s: %s", 
                    build.getResult().toString(), build.getFullDisplayName()), 100);

        String message = Hudson.getInstance().getRootUrl() == null ?
            Util.encode("(Global build server url not set)/" + build.getUrl()) :
            Util.encode(Hudson.getInstance().getRootUrl() + build.getUrl());

        AmazonSNSClient snsClient = new AmazonSNSClient(
                new BasicAWSCredentials(awsAccessKey, awsSecretKey));
        snsClient.setEndpoint(snsApiEndpoint);

        try {
            PublishRequest pubReq = new PublishRequest(publishTopic, message, subject);
            snsClient.publish(pubReq);
        } catch (Exception e) {
            listener.error(
                    "Failed to send SNS notification: " + e.getMessage());
        } finally {
            snsClient.shutdown();
        }

        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    private String truncate(String s, int toLength) {
        if (s.length() > toLength) {
            return s.substring(0, toLength);
        }
        return s;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    /**
     * Determine the SNS API endpoint to make API calls to, based on the region
     * included in the topic ARN. 
     */
    private String getSNSApiEndpoint(String topicArn) {

        // This is probably not a recommended way to pick the API endpoint, but
        // it seems to be a reasonably safe assumption, and avoids extra 
        // configuration variables.

        if (!topicArn.startsWith("arn:aws:sns:")) {
            return null;
        }

        String[] arnParts = topicArn.split(":");
        if (arnParts.length < 5) {
            return null;
        }

        String region = arnParts[3];
        return "sns." + region + ".amazonaws.com";
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String awsAccessKey;
        private String awsSecretKey;
        private String defaultTopicArn;

        public DescriptorImpl() {
            super(AmazonSNSNotifier.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "Amazon SNS Notifier";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            awsAccessKey = formData.getString("awsAccessKey");
            awsSecretKey = formData.getString("awsSecretKey");
            defaultTopicArn = formData.getString("defaultTopicArn");

            save();
            return super.configure(req,formData);
        }

        public String getAwsAccessKey() {
            return awsAccessKey;
        }

        public String getAwsSecretKey() {
            return awsSecretKey;
        }

        public String getDefaultTopicArn() {
            return defaultTopicArn;
        }

        public void setAwsAccessKey(String awsAccessKey) {
            this.awsAccessKey = awsAccessKey;
        }

        public void setAwsSecretKey(String awsSecretKey) {
            this.awsSecretKey = awsSecretKey;
        }

        public void setDefaultTopicArn(String defaultTopicArn) {
            this.defaultTopicArn = defaultTopicArn;
        }
    }
}
