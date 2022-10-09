package przyklady02;

import java.util.concurrent.ThreadLocalRandom;

public class TwoWritersPeterson {

    private static final int MAX_SLEEPING_TIME = 10;
    private static final int MULTIPLIER = 10;

    private static final int LINES_COUNT = 100;
    private static final int LINE_LENGTH = 50;

    private static volatile boolean lettersIsInterested = false;
    private static volatile boolean digitsIsInterested = false;

    private enum WriterType {
        LETTERS, DIGITS
    }

    private static volatile WriterType waiting = WriterType.LETTERS;

    private static void busyWork() throws InterruptedException {
        Thread.sleep(MULTIPLIER * ThreadLocalRandom.current().nextInt(MAX_SLEEPING_TIME));
    }

    private static char write(char first, char current, char last) {
        char c = current;
        for (int j = 0; j < LINE_LENGTH; ++j) {
            System.out.print(c);
            ++c;
            if (c > last) {
                c = first;
            }
        }
        System.out.println();
        return c;
    }

    private static class Letters implements Runnable {

        private final char FIRST_CHAR = 'a';
        private final char LAST_CHAR = 'z';

        @Override
        public void run() {
            try {
                char c = FIRST_CHAR;
                for (int i = 0; i < LINES_COUNT; ++i) {
                    busyWork();
                    lettersIsInterested = true;
                    waiting = WriterType.LETTERS;
                    while (digitsIsInterested && (waiting == WriterType.LETTERS)) {
                        Thread.yield();
                    }
                    c = write(FIRST_CHAR, c, LAST_CHAR);
                    lettersIsInterested = false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Letters interrupted");
            }
        }

    }

    private static class Digits implements Runnable {

        private final char FIRST_CHAR = '0';
        private final char LAST_CHAR = '9';

        @Override
        public void run() {
            try {
                char c = FIRST_CHAR;
                for (int i = 0; i < LINES_COUNT; ++i) {
                    busyWork();
                    digitsIsInterested = true;
                    waiting = WriterType.DIGITS;
                    while (lettersIsInterested && (waiting == WriterType.DIGITS)) {
                        Thread.yield();
                    }
                    c = write(FIRST_CHAR, c, LAST_CHAR);
                    digitsIsInterested = false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Digits interrupted");
            }
        }

    }

    public static void main(String[] args) {
        Thread letters = new Thread(new Letters());
        Thread digits = new Thread(new Digits());
        System.out.println("Start");
        letters.start();
        digits.start();
        try {
            letters.join();
            digits.join();
            System.out.println("Done");
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted");
            Thread.currentThread().interrupt();
            letters.interrupt();
            digits.interrupt();
        }
    }

}
