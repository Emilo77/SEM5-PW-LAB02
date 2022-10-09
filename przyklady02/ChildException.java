package przyklady02;

public class ChildException {

    private static class MessException extends RuntimeException {

        public MessException() {
            super("mess");
        }
    }

    public static void main(String[] args) {
        Thread badChild = new Thread(() -> {
            System.out.println(Thread.currentThread() + " is going to make a mess or die trying");
            throw new MessException();
        }, "Bad child");

        boolean noticed = false;

        try {
            Thread.sleep(1000);
            badChild.start();
            badChild.join();
        } catch (MessException e) {
            noticed = true;
        } catch (InterruptedException e) {
            System.out.println("Who dares to interrupt the main thread!");
        }

        System.out.println("Has " + badChild.getName() + " died? " + !badChild.isAlive());
        System.out.println("Has anyone noticed? " + noticed);
    }
}
