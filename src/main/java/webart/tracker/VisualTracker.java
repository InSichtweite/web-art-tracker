package webart.tracker;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.openqa.selenium.JavascriptExecutor;
import java.util.Map;

public class VisualTracker {
    private static BufferedImage lastScreenshot = null;
    private static String logFilePath;
    private static long lastKeyCount = 0;
    private static long lastMoveCount = 0;
    private static long lastClickCount = 0;
    private static long lastWheelCount = 0;

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "C:/Users/Daniela/Programming/Java/Analyse_tool/WebArtTracker/chromedriver.exe"); // Pfad ggf. anpassen
        final WebDriver driver = new ChromeDriver();
        driver.get("https://brandon.guggenheim.org/");
        
        // Create session directory with timestamp
        String sessionTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File baseDir = new File("screenshots");
        if (!baseDir.exists()) baseDir.mkdir();

        File sessionDir = new File(baseDir, "session_" + sessionTime);
        if (!sessionDir.exists()) sessionDir.mkdir();
        
     // Make it accessible inside TimerTask
        final File finalSessionDir = sessionDir;
        logFilePath = new File(finalSessionDir, "event_log.csv").getAbsolutePath();
        
     // Load and inject the JavaScript event tracker
        String script = "";
        try {
            script = new String(Files.readAllBytes(Paths.get("src/main/resources/js/eventTracker.js")));
            ((JavascriptExecutor) driver).executeScript(script);
        } catch (IOException e) {
            System.err.println("Error loading JS script: " + e.getMessage());
        }
        
        // Write CSV header
        try (FileWriter fw = new FileWriter(logFilePath, false)) {
            fw.write("timestamp; event; filename; detected_input\n");
        }
        catch (IOException e) {
            System.err.println("Error initializing log file: " + e.getMessage());
        }
        
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                	TakesScreenshot screenshotTaker = (TakesScreenshot) driver;
                	File screenshotFile = screenshotTaker.getScreenshotAs(OutputType.FILE);
                	BufferedImage currentScreenshot = ImageIO.read(screenshotFile);
                	String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

                	// Prepare JavaScript tracker
                	JavascriptExecutor js = (JavascriptExecutor) driver;

                	// Reinject JS tracker if missing
                	Object trackerExists = js.executeScript("return typeof window.__eventTracker !== 'undefined';");
                	if (!(Boolean) trackerExists) {
                	    try {
                	        String script = new String(Files.readAllBytes(Paths.get("src/main/resources/js/eventTracker.js")));
                	        js.executeScript(script);
                	        System.out.println("Reinjected JS tracker.");
                	    } catch (IOException e) {
                	        System.err.println("Error reloading JS tracker: " + e.getMessage());
                	    }
                	}

                	// Read event counts safely
                	Object result = js.executeScript("return window.__eventTracker;");
                	String reason = "dynamic";

                	if (result instanceof Map) {
                	    Map<String, Object> eventCounts = (Map<String, Object>) result;

                	    long move = ((Number) eventCounts.get("mousemove")).longValue();
                	    long down = ((Number) eventCounts.get("mousedown")).longValue();
                	    long click = ((Number) eventCounts.get("click")).longValue();
                	    long dbl = ((Number) eventCounts.get("dblclick")).longValue();
                	    long wheel = ((Number) eventCounts.get("wheel")).longValue();
                	    long key = ((Number) eventCounts.get("keyboard")).longValue();

                	    if (click > lastClickCount || down > lastClickCount) {
                	        reason = "mouse_click";
                	    } else if (wheel > lastWheelCount) {
                	        reason = "mouse_scroll";
                	    } else if (move > lastMoveCount) {
                	        reason = "mouse_move";
                	    } else if (key > lastKeyCount) {
                	        reason = "keyboard";
                	    }

                	    // Update all counters
                	    lastMoveCount = move;
                	    lastClickCount = click;
                	    lastWheelCount = wheel;
                	    lastKeyCount = key;

                	} else {
                	    System.err.println("Warning: Tracker result not valid – using reason = unknown");
                	}

                    if (lastScreenshot == null || !imagesAreEqual(lastScreenshot, currentScreenshot)) {
                        lastScreenshot = currentScreenshot;
                        
                        File screenshotDir = new File("screenshots");
                        if (!screenshotDir.exists()) {
                            screenshotDir.mkdir();
                        }
                        File output = new File(finalSessionDir, "screenshot_" + timestamp + ".png");
                        ImageIO.write(currentScreenshot, "png", output);
                        logEvent(timestamp, "Change detected;Change detected – screenshot saved:;" + output.getName()+ ";detetced:" + reason);
                        System.out.println("Change detected – screenshot saved: ," + output.getName() + ",detected:" + reason);
                    } else {
                    	System.out.println("No change detected.");
                    }
                } catch (IOException e) {
                    System.err.println("Error while taking screenshot: " + e.getMessage());
                }
            }
        }, 0, 1000); // every second

        System.out.println("Press [Enter] to stop tracking...");
        try {
            System.in.read();
        } catch (IOException e) {
            System.err.println("Error while waiting for input: " + e.getMessage());
        }
        driver.quit();
        System.exit(0);
    }

    private static boolean imagesAreEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
            return false;

        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y))
                    return false;
            }
        }
        return true;
    }
    
    private static void logEvent(String timestamp, String event) {
        try (FileWriter fw = new FileWriter(logFilePath, true)) {
            fw.write(timestamp + "," + event.replace(",", ";") + "\n");
        } catch (IOException e) {
            System.err.println("Error while writing to log file: " + e.getMessage());
        }
    }
}
