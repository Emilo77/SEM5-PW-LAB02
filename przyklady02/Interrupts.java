package przyklady02;

public class Interrupts {

    private static void printTime() {
        System.out.println("Current time: " + System.currentTimeMillis());
    }

    private static class Helper implements Runnable {

        private final int sleepTime;
        private final int workTime;

        public Helper(int sleepTime, int workTime) {
            this.sleepTime = sleepTime;
            this.workTime = workTime;
        }

        private static void printFinished() {
            System.out.println("Thread " + Thread.currentThread().getName() + " finished uninterrupted");
        }

        private static void printInterrupted(String when) {
            Thread current = Thread.currentThread();
            System.out.print("Thread " + current.getName());
            System.out.print(" was interrupted during " + when);
            System.out.print(", interrupted flag before: " + current.isInterrupted());
            current.interrupt();
            System.out.println(", interrupted flag after: " + current.isInterrupted());
        }

        @Override
        public void run() {
            try {
                Thread.sleep(sleepTime);
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() < start + workTime) {
                    if (Thread.interrupted()) {
                        printInterrupted("computations");
                        return;
                    }
                    // computations
                }
                printFinished();
            } catch (InterruptedException e) {
                printInterrupted("sleep");
            }
        }

    }

    public static void main(String args[]) {
        try {
            Thread first = new Thread(new Helper(1000, 1000), "First");
            Thread second = new Thread(new Helper(3000, 1000), "Second");
            Thread third = new Thread(new Helper(3000, 2000), "Third");
            first.start();
            second.start();
            third.start();
            printTime();
            first.join();
            printTime();
            second.interrupt();
            Thread.sleep(2000);
            printTime();
            third.interrupt();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main interrupted");
        }
    }

}
