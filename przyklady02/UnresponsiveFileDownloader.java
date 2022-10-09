package przyklady02;

import java.io.IOException;
import java.time.Clock;

public class UnresponsiveFileDownloader {

    private static volatile int progress = 0;
    private static final int PROGRESS_MAX = 100;

    private static void startDownloading() throws InterruptedException {
        while (progress < PROGRESS_MAX) {
            Thread.sleep(50);
            progress++;
        }
    }

    public static void main(String[] args) {
        while (true) {
            // This clears the console
            System.out.print("\033[H\033[2J");
            System.out.flush();

            if (progress == 0) {
                System.out.println("Press enter to start downloading");
            } else if (progress == 100) {
                System.out.println("Download complete");
            }
            System.out.println("Time: " + Clock.systemDefaultZone().instant().toString());
            System.out.println("Progress: " + progress + " / " + PROGRESS_MAX);
            try {
                // Check if user pressed enter
                if (System.in.available() > 0 && System.in.read() == '\n') {
                    startDownloading();
                } else {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (IOException e) {
                return;
            }

        }
    }
}

