package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import drivers.DriverContext;

public class ScreenshotUtil {

	public static String takeScreenshot() {
		File screenshot = ((TakesScreenshot) DriverContext.getDriver()).getScreenshotAs(OutputType.FILE);
		// Use unique names or timestamps to prevent tests from overwriting each other's
		// screenshots
		String screenPath = "C:/Ixigo/ixigo-flightBooking/target/screenshot_" + System.currentTimeMillis() + ".png";

		try {
			// Added REPLACE_EXISTING to avoid file conflicts
			Files.copy(screenshot.toPath(), Paths.get(screenPath), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.err.println("Failed to save screenshot: " + e.getMessage());
			return null;
		}

		// Return the string path so Extent Reports can consume it
		return screenPath;
	}
}
