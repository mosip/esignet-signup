package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import base.BaseTest;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestStepFinished;

public class StepListener implements ConcurrentEventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        // Only listen to step finished events
        publisher.registerHandlerFor(TestStepFinished.class, this::handleStepFinished);
    }

    private void handleStepFinished(TestStepFinished event) {
        if (!(event.getTestStep() instanceof PickleStepTestStep step)) {
            return;
        }

        String stepText = step.getStep().getText();

        switch (event.getResult().getStatus()) {
            case PASSED -> ExtentReportManager.logStep("ℹ️ Step completed successfully: " + stepText);
            case FAILED -> captureFailure(stepText);
            case SKIPPED -> {
                // Do nothing for skipped steps
            }
            default -> ExtentReportManager.getTest().info("ℹ️ Step Status: " + event.getResult().getStatus());
        }
    }

    private void captureFailure(String stepText) {
        ExtentReportManager.getTest().fail("❌ Step Failed: " + stepText);
        WebDriver driver = BaseTest.getDriver();
        if (driver != null) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                ExtentReportManager.getTest().addScreenCaptureFromBase64String(
                        java.util.Base64.getEncoder().encodeToString(screenshot), "Failure Screenshot");
            } catch (Exception e) {
                System.err.println("Failed to capture screenshot: " + e.getMessage());
            }
        }
    }
}
