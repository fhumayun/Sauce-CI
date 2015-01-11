import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.SauceOnDemandTestWatcher;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class WebDriverWithHelperParametersTest implements SauceOnDemandSessionIdProvider {

	private WebDriver webDriver;
	private static DesiredCapabilities capabilities;
	private static Platform ANDROID, LINUX, MAC, UNIX, VISTA, WINDOWS, XP, platformValue;
	private String browser, browserVersion, platform, sessionId = "";



	// Create an array of available platforms from the "private static Platform" declaration above
	Platform[] platformValues = Platform.values();


	String[] browserArray = { "android", "chrome", "firefox", "htmlUnit", "internet explorer",
			"ipad", "iphone", "opera", "safari" };


	public Platform setPlatformCapabilities(String platformParam) {

		String platformVal = platformParam;

		for (int p=0; p<platformValues.length; p++) {
			platformValue = platformValues[p++];
			if (platformValue.toString() == platformVal) break;
		}

		return platformValue;

	}


	/**
	 * Constructs a {@link SauceOnDemandAuthentication} instance using the supplied Sauce
	 * user name and access key. To use the authentication supplied by environment variables or
	 * from an external file, use the no-arg {@link SauceOnDemandAuthentication} constructor.
	 */
	public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication("userName", "accessKey");

	/**
	 * JUnit Rule that marks Sauce Jobs as passed/failed when the test succeeds or fails.
	 */
	public @Rule
	SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);


	/**
	 * JUnit Rule that records the test name of the current test. When this is referenced
	 * during the creation of {@link DesiredCapabilities}, the test method name is assigned
	 * to the Sauce Job name and recorded in Jenkins Console Output and in the Sauce Jobs
	 * Report in the Jenkins project's home page.
	 */
	public @Rule TestName testName = new TestName();


	/**
	 * JUnit annotation that runs each test once for each item in a Collection.
	 *
	 * Feel free to add as many additional parameters as you like to the capabilitiesParams array.
	 *
	 * Note: If you add parameters for the MAC platform, make sure that you have Mac minutes in
	 * your <a href="https://saucelabs.com/login">Sauce account</a> or the test will fail.
	 */
	@Parameters
	public static Collection<Object[]> data() {

		Object[][] capabilitiesParams = {
				{ "SELENIUM_BROWSER1", "SELENIUM_VERSION1", "SELENIUM_PLATFORM1" },
				{ "SELENIUM_BROWSER2", "SELENIUM_VERSION2", "SELENIUM_PLATFORM2" },
				{ "SELENIUM_BROWSER3", "SELENIUM_VERSION3", "SELENIUM_PLATFORM3" },
				{ "SELENIUM_BROWSER4", "SELENIUM_VERSION4", "SELENIUM_PLATFORM4" },
		};

		return Arrays.asList(capabilitiesParams);

	}


	public WebDriverWithHelperParametersTest(String s1, String s2, String s3) {
		browser = s1;
		browserVersion = s2;
		platform = s3;
	}


	/**
	 * Creates a new {@link RemoteWebDriver} instance that is used to run WebDriver tests
	 * using Sauce.
	 *
	 * @throws Exception thrown if an error occurs constructing the WebDriver
	 */
	@Test
	@Ignore
	public void validateTitle() throws Exception {
		capabilities = new DesiredCapabilities(browser, browserVersion, setPlatformCapabilities(platform));
		capabilities.setCapability("name", this.getClass().getName() + "." + testName.getMethodName());
		this.webDriver = new RemoteWebDriver(
				new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() + "@ondemand.saucelabs.com:80/wd/hub"),
				capabilities);
		this.sessionId = ((RemoteWebDriver)webDriver).getSessionId().toString();

		if (browserVersion == "") browserVersion = "unspecified";
		String browserName = String.format("%-19s", browser).replaceAll(" ", ".").replaceFirst("[.]", " ");
		String browserVer = String.format("%-19s", browserVersion).replaceAll(" ", ".");
		System.out.println("@Test validateTitle() testing browser/version: " + browserName + browserVer + "platform: " + platform);

		String QA_Link = Utils.readPropertyOrEnv("QA_Link","");
		webDriver.get(QA_Link);

		webDriver.quit();
	}


	@Override
	public String getSessionId() {
		return sessionId;
	}

}
