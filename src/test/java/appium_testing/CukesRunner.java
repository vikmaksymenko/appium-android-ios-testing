package appium_testing;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServerHasNotBeenStartedLocallyException;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import io.qameta.allure.selenide.AllureSelenide;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;

@CucumberOptions(plugin = {"rerun:target/rerun.txt" })
public class CukesRunner extends AbstractTestNGCucumberTests {
    private static ThreadLocal<AppiumDriverLocalService> service = new ThreadLocal<>();
    private static ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();

    public AppiumDriver getDriver() {
        return driver.get();
    }

    public AppiumDriverLocalService getService() {
        return service.get();
    }

    private DesiredCapabilities promoteDeviceCapabilities() {
        DesiredCapabilities caps = new DesiredCapabilities();


        if(System.getProperty("device.platform.name").equals("android")) {
            caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
            caps.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, true);
            caps.setCapability(AndroidMobileCapabilityType.ANDROID_INSTALL_TIMEOUT, 120000);
            caps.setCapability("adbExecTimeout", 90000);
            caps.setCapability("allowTestPackages", true);
        } else if(System.getProperty("device.platform.name").equals("ios")) {
            caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
            caps.setCapability("useNewWDA", "true");
        }

        caps.setCapability(MobileCapabilityType.PLATFORM_NAME, System.getProperty("device.platform.name"));
        caps.setCapability(MobileCapabilityType.PLATFORM_VERSION, System.getProperty("device.platform.version"));
        caps.setCapability(MobileCapabilityType.DEVICE_NAME, System.getProperty("device.name"));
        caps.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 300);

//        caps.setCapability(MobileCapabilityType.NO_RESET, false);
//        caps.setCapability(MobileCapabilityType.FULL_RESET, true);

        caps.setCapability(MobileCapabilityType.NO_RESET, !Boolean.getBoolean(System.getProperty("reset.app")));
        caps.setCapability(MobileCapabilityType.FULL_RESET, Boolean.getBoolean(System.getProperty("reset.device")));

        return caps;
    }

    private AppiumDriver initLocal() {
        AppiumServiceBuilder appiumServiceBuilder = new AppiumServiceBuilder();
        service.set(appiumServiceBuilder.withIPAddress("127.0.0.1").build());
        service.get().start();

        if (service == null || !service.get().isRunning()) {
            throw new AppiumServerHasNotBeenStartedLocallyException("An appium server node is not started!");
        }

        DesiredCapabilities capabilities = promoteDeviceCapabilities();

        String appPath = System.getProperty("app.path");
        if (appPath.endsWith(".apk") || appPath.endsWith(".ipa")) {
            capabilities.setCapability(MobileCapabilityType.APP, appPath);
        } else {
            appPath = appPath.isEmpty() ? "src/test/resources/appium_testing/apps"  : appPath;
            if(System.getProperty("device.platform.name").equals("android")) {
                capabilities.setCapability(MobileCapabilityType.APP,
                        new File(appPath, "/app-debug.apk").getAbsolutePath());
            }

            if(System.getProperty("device.platform.name").equals("ios")){
                if(System.getProperty("device.type").equals("device")) {
                    capabilities.setCapability(MobileCapabilityType.APP,
                            new File(appPath, "/AppiumTestApp.app").getAbsolutePath());
                } else if(System.getProperty("device.type").equals("simulator")) {
                    capabilities.setCapability(MobileCapabilityType.APP,
                            new File(appPath, "/AppiumTestApp.app").getAbsolutePath());
                }

            }
        }

        if(System.getProperty("device.platform.name").equals("android")) {
//            capabilities.setCapability("unlockType", "pin");
//            capabilities.setCapability("unlockKey", "1111");
            return new AndroidDriver(service.get().getUrl(), capabilities);
        }

        if(System.getProperty("device.platform.name").equals("ios")){
            if(System.getenv("XCODE_ORG_ID") != null) {
                capabilities.setCapability(IOSMobileCapabilityType.XCODE_ORG_ID, System.getenv("XCODE_ORG_ID"));
                capabilities.setCapability(IOSMobileCapabilityType.XCODE_SIGNING_ID, "iPhone Developer");
            } else {
                File xcconfig = new File(System.getProperty("xcconfig.file"));
                if(!xcconfig.exists()) {
                    throw new Error("Set XCODE_ORG_ID or xcconfig.file maven parameter with path to .xcconfig");
                }
                capabilities.setCapability(IOSMobileCapabilityType.XCODE_CONFIG_FILE, xcconfig.getAbsolutePath());
            }
            capabilities.setCapability(IOSMobileCapabilityType.SHOW_XCODE_LOG, true);
            if(System.getProperty("device.udid") != null) {
                capabilities.setCapability("udid", System.getProperty("device.udid"));
            }
            return new IOSDriver(service.get().getUrl(), capabilities);
        }

        return null;
    }

    private AppiumDriver initSauceLabs() throws MalformedURLException {
        /* SauceLabs Config */
        final String USERNAME = System.getenv("USERNAME");
        final String ACCESS_KEY = System.getenv("ACCESS_KEY");
        final String URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@ondemand.eu-central-1.saucelabs.com:443/wd/hub";

        DesiredCapabilities capabilities = promoteDeviceCapabilities();
        capabilities.setCapability("app", "https://73228fec.ngrok.io/app-admob12-debug.apk");
        capabilities.setCapability("browserName", "");

        if(System.getProperty("device.platform.name").equals("android")) {
            return new AndroidDriver(new URL(URL), capabilities);
        }

        if(System.getProperty("device.platform.name").equals("ios")){
            return new IOSDriver(new URL(URL), capabilities);
        }

        return null;
    }

    private AppiumDriver initBrowserstack() throws MalformedURLException {
        final String USERNAME = System.getenv("USERNAME");
        final String ACCESS_KEY = System.getenv("ACCESS_KEY");
        final String URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

        DesiredCapabilities capabilities = promoteDeviceCapabilities();

        capabilities.setCapability("app", System.getenv("APP"));
        capabilities.setCapability("device", System.getProperty("device.name"));
        capabilities.setCapability("os_version", System.getProperty("device.platform.version"));

        if(System.getProperty("device.platform.name").equals("android")) {
            return new AndroidDriver(new URL(URL), capabilities);
        }

        if(System.getProperty("device.platform.name").equals("ios")){
            return new IOSDriver(new URL(URL), capabilities);
        }

        return null;
    }

    private AppiumDriver initPerfecto() throws MalformedURLException {
        DesiredCapabilities capabilities = promoteDeviceCapabilities();
        capabilities.setCapability("deviceOrientation", "portrait");
        capabilities.setCapability("captureScreenshots", true);

        capabilities.setCapability("user", System.getenv("USERNAME"));
        capabilities.setCapability("password", System.getenv("PASSWORD"));
        capabilities.setCapability("deviceName", System.getProperty("device.name"));
        capabilities.setCapability("autoLaunch",true);
        capabilities.setCapability("fullReset",true);
        capabilities.setCapability("app", System.getenv("APP"));

        String host = "mobilecloud.perfectomobile.com";
        String url = "https://" + host + "/nexperience/perfectomobile/wd/hub";

        if(System.getProperty("device.platform.name").equals("android")) {
            return new AndroidDriver(new URL(url), capabilities);
        }

        if(System.getProperty("device.platform.name").equals("ios")){
            return new IOSDriver(new URL(url), capabilities);
        }

        return null;

    }

    private AppiumDriver initKobiton() throws MalformedURLException {
        DesiredCapabilities capabilities = promoteDeviceCapabilities();

        final String USERNAME = System.getenv("USERNAME");
        final String ACCESS_KEY = System.getenv("ACCESS_KEY");
        String kobitonServerUrl = "https://" + USERNAME + ":" + ACCESS_KEY + "@api.kobiton.com/wd/hub";

        // The generated session will be visible to you only.
        capabilities.setCapability("sessionName", "Automation test session");
        capabilities.setCapability("sessionDescription", "");
        capabilities.setCapability("deviceOrientation", "portrait");
        capabilities.setCapability("captureScreenshots", true);

        capabilities.setCapability("app", System.getenv("APP"));

        if(System.getProperty("device.platform.name").equals("android")) {
            return new AndroidDriver(new URL(kobitonServerUrl), capabilities);
        }

        if(System.getProperty("device.platform.name").equals("ios")){
            return new IOSDriver(new URL(kobitonServerUrl), capabilities);
        }

        return null;
    }

    @BeforeTest
    public void startServer() throws MalformedURLException {
        AppiumDriver aDriver = null ;
        switch (System.getProperty("provider.name")) {
            case "local":
                aDriver = initLocal();
                break;
            case "saucelabs":
                aDriver = initSauceLabs();
                break;
            case "browserstack":
                aDriver = initBrowserstack();
                break;
            case "kobiton":
                aDriver = initKobiton();
                break;
            case "perfecto":
                aDriver = initPerfecto();
                break;
        }

        driver.set(aDriver);
        WebDriverRunner.setWebDriver(aDriver);
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
    }

    @AfterTest(alwaysRun = true)
    public void stopServer() {
        if (driver.get() != null) {
            driver.get().quit();
        }
        if (service.get() != null) {
            service.get().stop();
        }
    }

    @AfterTest(alwaysRun = true)
    public void addAllureEnvDetails() {
        BufferedWriter writer = null;
        try {
            File envPropsFile = new File("target/allure-results/environment.properties");
            writer = new BufferedWriter(new FileWriter(envPropsFile));
            String env = "Provider=" + System.getProperty("provider.name") + "\n" +
                    "Platform.name=" + System.getProperty("device.platform.name") + "\n" +
                    "Platform.version=" + System.getProperty("device.platform.version") + "\n" +
                    "Device.type=" + System.getProperty("device.type") + "\n" +
                    "Device.name=" + System.getProperty("device.name") + "\n" +
                    "App.branch=" + System.getProperty("app.branch") + "\n";
            writer.write(env);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
