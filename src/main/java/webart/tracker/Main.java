package webart.tracker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        String url = "https://example.com";
        try {
            Document doc = Jsoup.connect(url).get();
            System.out.println("[" + LocalDateTime.now() + "] Title: " + doc.title());
        } catch (IOException e) {
            System.err.println("Error fetching URL: " + e.getMessage());
        }
    }
}
