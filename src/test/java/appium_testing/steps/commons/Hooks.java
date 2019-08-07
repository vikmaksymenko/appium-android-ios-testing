package appium_testing.steps.commons;

import appium_testing.CukesRunner;

import io.appium.java_client.screenrecording.CanRecordScreen;
import io.cucumber.core.api.Scenario;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.qameta.allure.Attachment;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import java.util.Base64;

import static org.testng.Assert.fail;

public class Hooks {
    @Before
    public static void setup(Scenario scenario) {
        if(scenario.getSourceTagNames().contains("skip")
                && System.getProperty("skip.tests").equals("true")) {
            fail("The test is marked with @skip tag and will not be run");
        }

        WebDriver driver = new CukesRunner().getDriver();
        if (System.getProperty("device.platform.name").equals("android")) {
            driver.manage().logs().get("logcat");     // reset device log buffer
            if(System.getProperty("device.type").contains("device")
                    || System.getProperty("device.platform.version").contains("8")
                    || System.getProperty("device.platform.version").contains("9")) {
                ((CanRecordScreen) driver).startRecordingScreen();
            }
        } else if (System.getProperty("device.platform.name").equals("ios")) {
            driver.manage().logs().get("syslog");     // reset device log buffer
            ((CanRecordScreen) driver).startRecordingScreen();
        }
    }

    @After
    public void teardown(Scenario scenario) {
        WebDriver driver = new CukesRunner().getDriver();

        if (scenario.isFailed()) {
            if (System.getProperty("device.platform.name").equals("android")) {
                attachDeviceLog(driver.manage().logs().get("logcat"));
            } else if (System.getProperty("device.platform.name").equals("ios")) {
                attachDeviceLog(driver.manage().logs().get("syslog"));
            }
            attachScreencast();
//            AllureLifecycle.takeScreenshot("failed");
        }
    }

    @Attachment(value = "test_recording.mp4", type = "video/mp4")
    private byte[] attachScreencast(){

        // TODO: validate the method

        WebDriver driver = new CukesRunner().getDriver();
        if (System.getProperty("device.platform.name").equals("ios")
                || System.getProperty("device.type").contains("device")
                || System.getProperty("device.platform.version").contains("8")
                || System.getProperty("device.platform.version").contains("9")) {
            String video = ((CanRecordScreen) driver).stopRecordingScreen();
            return Base64.getMimeDecoder().decode(video);
        } else {
            return null;
        }
    }

    @Attachment(value = "device.log", type = "text/plain")
    private String attachDeviceLog(LogEntries entries){
        StringBuilder builder = new StringBuilder();
        for (LogEntry entry: entries) {
            builder.append(entry.toString()).append("\n");
        }
        return builder.toString();
    }
}
