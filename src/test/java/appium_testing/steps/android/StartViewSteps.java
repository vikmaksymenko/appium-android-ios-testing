package appium_testing.steps.android;

import appium_testing.utils.AllureLogHelper;
import io.cucumber.java.en.Given;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static org.testng.Assert.assertEquals;

public class StartViewSteps {
    @Given("Message {string} should be displayed")
    public void messageShouldBeDisplayed(String message) {
        $(By.xpath("//android.widget.TextView[@text='" + message + "']")).waitUntil(exist, 20000).should(be(visible));
        AllureLogHelper.takeScreenshot("App loaded");
    }

    @Given("{int} plus {int} should be {int}")
    public void aPlusBShouldBeC(int a, int b, int res) {
        assertEquals(res, a + b);
    }
}
