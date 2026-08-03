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
            case FAILED -> captureFailure(stepText, event.getResult().getError());
            case SKIPPED -> {
                // Do nothing for skipped steps
            }
            default -> ExtentReportManager.getTest().info("ℹ️ Step Status: " + event.getResult().getStatus());
        }
    }

    private void captureFailure(String stepText, Throwable error) {
        ExtentReportManager.getTest().fail("❌ Step Failed: " + stepText);
        // Carries the assertion details into the report, which would otherwise show
        // only which step failed, not why.
        if (error != null) {
            String reason = error.getMessage();
            ExtentReportManager.getTest().fail("Reason: " + error.getClass().getSimpleName()
                    + (reason == null || reason.isBlank() ? "" : " - " + reason.replace("\n", "<br/>")));
        }
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
