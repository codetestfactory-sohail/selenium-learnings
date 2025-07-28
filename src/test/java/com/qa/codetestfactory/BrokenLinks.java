package com.qa.codetestfactory;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class BrokenLinks {
    public static void main(String[] args) throws InterruptedException, NullPointerException, Exception {
                
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://ecommerce-playground.lambdatest.io/");

        Thread.sleep(2000);

        List<WebElement> links = driver.findElements(By.tagName("a"));
        System.out.println("Total Number of Identified Hyperlinks: " + links.size());

        AtomicInteger brokenLinksCount = new AtomicInteger(0);
        AtomicInteger invalidLinksCount = new AtomicInteger(0);
        AtomicInteger linkIndex = new AtomicInteger(1);

        long startTime = System.currentTimeMillis();

        links.parallelStream().forEach(link -> {
            int idx = linkIndex.getAndIncrement();
            String url = link.getAttribute("href");
            if (isValidLink(url)) {
                if (isBrokenLink(url, idx)) {
                    brokenLinksCount.incrementAndGet();
                }
            } else {
                System.out.println(idx + ". Invalid Link: " + url);
                invalidLinksCount.incrementAndGet();
            }
        });

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000; // Convert to seconds

        System.out.println("#################################");
        System.out.println("Total Links: " + links.size() + 
        "\nValid Links: " + (links.size() - brokenLinksCount.get() - invalidLinksCount.get()) + 
        "\nInvalid Links: " + invalidLinksCount.get() + 
        "\nBroken Links: " + brokenLinksCount.get());
        System.out.printf("\nTotal time taken to check all links: %d seconds\n", duration);
        System.out.println("#################################");

        driver.quit();
    }

    public static boolean isValidLink(String url) {
        if (url == null || url.isEmpty()) return false;
        if (url.startsWith("javascript:") || url.startsWith("#")) return false;
        return true;
    }

    public static boolean isBrokenLink(String url, int idx) {
        try {
            URL urlObj = new URI(url).toURL();
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("HEAD");
            conn.connect();
            int respCode = conn.getResponseCode();
            if (respCode >= 400) {
                System.out.println(idx + ". Broken Link: " + url + " - Response Code: " + respCode);
                return true; // Link is broken
            }
            else if (respCode == 200) {
                System.out.println(idx + ". Working Link: " + url + " - Response Code: " + respCode);
                return false; // Link is valid
            }
        } catch (Exception e) {
            System.out.println(idx + ". Error checking link: " + url + " - " + e.getMessage());
            return true; // If there's an error, consider it a broken link
        }
        return false; // Link is valid
    }

}
