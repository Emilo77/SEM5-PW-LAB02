package przyklady02;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class SpawnableWorkers {

    private static class Worker implements Runnable {

        private final int id;
        private final int secondsOfWork;

        public Worker(int id, int secondsOfWork) {
            this.id = id;
            this.secondsOfWork = secondsOfWork;
        }

        @Override
        public void run() {
            System.out.println("Worker " + id + " started");

            try {
                Thread.sleep(secondsOfWork * 1000L);
            } catch (InterruptedException e) {
                System.out.println("Worker " + id + " interrupted!");
                return;
            }

            System.out.println("Worker " + id + " finished after " + secondsOfWork + " seconds");
        }

    }

    public static void main(String[] args) {
        System.out.println("A positive number n starts a new worker which will work for n seconds");
        System.out.println("A negative number -n interrupts the worker with id n");
        System.out.println("Zero interrupts the main thread");

        int nextId = 1;
        List<Thread> threads = new ArrayList<>();
        threads.add(Thread.currentThread());

        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                // This method blocks the thread until some input is read
                int number = scanner.nextInt();

                if (number > 0) {
                    Thread thread = new Thread(new Worker(nextId, number));
                    threads.add(thread);
                    nextId++;
                    thread.start();
                } else {
                    int id = -number;
                    if (id >= nextId) {
                        System.out.println("No worker with such id");
                    } else if (!threads.get(id).isAlive()) {
                        System.out.println("Worker already finished");
                    } else {
                        threads.get(id).interrupt();
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted!");
        } catch (InputMismatchException e) {
            System.out.println("Incorrect input");
        }

        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

}
