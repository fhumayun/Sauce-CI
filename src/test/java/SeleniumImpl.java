package com.saucelabs.sauce_ondemand.driver;

import com.saucelabs.saucerest.SauceREST;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
class SeleniumImpl extends DefaultSelenium implements SauceOnDemandSelenium, Selenium {
    /**
     * {@link DefaultSelenium} throw away the session ID as soon as the {@link #stop()}
     * is called, so we'll  store it aside.
     */
    private String lastSessionId;

    private String jobName;

    private final Credential credential;

    SeleniumImpl(String serverHost, int serverPort, String browserStartCommand, String browserURL, Credential credential, String jobName) {
        super(serverHost, serverPort, browserStartCommand, browserURL);
        this.credential = credential;
        this.jobName = jobName;
    }

    @Override
    public void start() {
        super.start();
        dumpSessionId();
    }

    @Override
    public void start(String optionsString) {
        super.start(optionsString);
        dumpSessionId();
    }

    @Override
    public void start(Object optionsObject) {
        super.start(optionsObject);
        dumpSessionId();
    }

    /**
     * Dump the session ID, so that it can be captured by the CI server.
     */
    private void dumpSessionId() {
        lastSessionId = getSessionId();
        System.out.println("SauceOnDemandSessionID=" + lastSessionId + " job-name=" + jobName);
    }

    public String getSessionId() {
        try {
            Field f = commandProcessor.getClass().getDeclaredField("sessionId");
            f.setAccessible(true);
            Object id = f.get(commandProcessor);
            if (id != null) return id.toString();
            return lastSessionId;
        } catch (NoSuchFieldException e) {
            // failed to retrieve the session ID
        } catch (IllegalAccessException e) {
            // failed to retrieve the session ID
        }
        return null;
    }

    public String getSessionIdValue() {
        return getSessionId();
    }

    public Credential getCredential() {
        return credential;
    }

    public URL getSeleniumServerLogFile() throws IOException {
        return getFileURL("selenium-server.log");
    }

    public URL getVideo() throws IOException {
        return getFileURL("video.flv");
    }

    private URL getFileURL(String fileName) throws MalformedURLException {
        // userinfo in URL doesn't result in the BASIC auth, so in this method we won't set the credential.
        return new URL(MessageFormat.format("https://saucelabs.com/rest/{0}/jobs/{1}/results/{2}",
                credential.getUsername(), lastSessionId, fileName));
    }

    public InputStream getSeleniumServerLogFileInputStream() throws IOException {
        return openWithAuth(getSeleniumServerLogFile());
    }

    public InputStream getVideoInputStream() throws IOException {
        return openWithAuth(getVideo());
    }

    private InputStream openWithAuth(URL url) throws IOException {
        URLConnection con = url.openConnection();
        //Handle long strings encoded using BASE64Encoder - see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6947917
        BASE64Encoder encoder = new BASE64Encoder() {
            @Override
            protected int bytesPerLine() {
                return 9999;
            }
        };
        String encodedAuthorization = encoder.encode(
                (credential.getUsername() + ":" + credential.getKey()).getBytes());
        con.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
        return con.getInputStream();
    }

    public void jobPassed() throws IOException {
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("passed", true);
        updateJobInfo(updates);
    }

    private void updateJobInfo(Map<String, Object> updates) throws IOException {
        SauceREST sauceREST = new SauceREST(credential.getUsername(), credential.getKey());
        sauceREST.updateJobInfo(lastSessionId, updates);

    }

    public void jobFailed() throws IOException {
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("passed", false);
        updateJobInfo(updates);
    }

    public void setBuildNumber(String buildNumber) throws IOException {
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("build", buildNumber);
        updateJobInfo(updates);
    }

    public void setJobName(String jobName) throws IOException {
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("name", jobName);
        updateJobInfo(updates);
    }
}