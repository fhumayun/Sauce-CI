/**
 * @author Faisal
 */

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
import com.saucelabs.testng.SauceOnDemandTestListener;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

import static org.testng.Assert.assertEquals;


/**
 * Simple TestNG test which demonstrates being instantiated via a DataProvider in order to supply multiple browser combinations.
 *
 */
@Listeners({SauceOnDemandTestListener.class})

public class NicerMultiWebLauncher implements SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider {

	/**
	 * Constructs a {@link com.saucelabs.common.SauceOnDemandAuthentication} instance using the supplied user name/access key.  To use the authentication
	 * supplied by environment variables or from an external file, use the no-arg {@link com.saucelabs.common.SauceOnDemandAuthentication} constructor.
	 */
	public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication("userName", "accessKey");
	private ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();
	private ThreadLocal<String> sessionId = new ThreadLocal<String>();
	private static final Logger logger = Logger.getLogger(NicerMultiWebLauncher.class.getName());

	/**
	 * Simple hard-coded DataProvider that explicitly sets the browser combinations to be used.
	 *
	 * @param testMethod
	 * @return
	 */
	@DataProvider(name = "sauceBrowserSet", parallel = true)
	public static Object[][] sauceBrowserDataProvider(Method testMethod) {
		return new Object[][]{
				new Object[]{"SELENIUM_BROWSER1", "SELENIUM_VERSION1", "SELENIUM_PLATFORM1","","","Browser1"},
				new Object[]{"SELENIUM_BROWSER2", "SELENIUM_VERSION2", "SELENIUM_PLATFORM2","","","Browser2"},
				//new Object[]{"iehta", "8", "Windows 7","","","Browser3"},
				//new Object[]{"iehta", "9", "Windows 7","","","Browser4"},
				//new Object[]{"iehta", "10", "Windows 7","","","Browser5"},
				//new Object[]{"iehta", "11", "Windows 7","","","Browser6"},
				//new Object[]{"opera", "11", "Windows 7","","","Browser7"},
				//new Object[]{"safari", "5", "MAC","","","Browser8"},
				//new Object[]{"iPad", "7.0", "OS X 10.9","","portrait","Browser9"},
				//new Object[]{"Android", "4.4", "Linux","","portrait","Browser10"},
		};
	}

	/**
	 * Creates a new {@link RemoteWebDriver} instance which is configured
	 * @param browser
	 * @param version
	 * @param os
	 * @return
	 * @throws MalformedURLException
	 */
	private WebDriver createDriver(String browser, String Device, String DeviceOrientation, String version, String os, String name) throws MalformedURLException {

		// Start with Jenkins
		DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
		desiredCapabilities.setBrowserName(System.getenv("SELENIUM_BROWSER"));
		desiredCapabilities.setVersion(System.getenv("SELENIUM_VERSION"));
		desiredCapabilities.setCapability(CapabilityType.PLATFORM, System.getenv("SELENIUM_PLATFORM"));
		desiredCapabilities.setCapability("record-video", true);
		desiredCapabilities.setCapability("name", this.getClass().getName() + "." + testName.getMethodName());

		this.webDriver = new RemoteWebDriver(
				new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() + "@ondemand.saucelabs.com:80/wd/hub"),
				desiredCapabilities);				
		this.sessionId = ((RemoteWebDriver)webDriver).getSessionId().toString();
		return webDriver.get();
	}

	@Test(dataProvider = "sauceBrowserSet")
	public void webDriver(String browser,  String Device, String DeviceOrientation,String version, String os, String name) throws Exception {
		WebDriver driver = createDriver(browser, Device, DeviceOrientation, version, os, name);
		// Console level logging
		logger.log(Level.INFO, "Running test using " + browser + " " + version + " " + os + " as job:" + name );
		// Test QA Link below:
        String QA_Link = Utils.readPropertyOrEnv("QA_Link", "");
        webDriver.get(QA_Link);
		webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		webDriver.quit();
	}

	public WebDriver getWebDriver() {
		// Console log of webdriver functions
		System.out.println("WebDriver" + webDriver.get());
		return webDriver.get();
	}

	public String getSessionId() {
		return sessionId.get();
	}

	@Override
	public SauceOnDemandAuthentication getAuthentication() {
		return authentication;
	}
}