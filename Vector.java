import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


/*Pozwoliłem dodać od siebie funkcję wypisującą wektor i losującą współrzędne
wektora. Dzięki temu, prawie za każdym razem losują się inne wektory do
dodania i obliczenia iloczynu skalarnego.
*/

public class Vector {

    public volatile ArrayList<Integer> dimension;

    public Vector(ArrayList<Integer> dimension) {
        this.dimension = dimension;
    }

    public static class AddingHelper implements Runnable {
        Vector vector;
        Vector other;
        Vector result;
        int starting;
        int amount;

        public AddingHelper(Vector vector, Vector other, Vector result, int starting, int amount) {
            this.vector = vector;
            this.other = other;
            this.starting = starting;
            this.amount = amount;
            this.result = result;
        }

        @Override
        public void run() {

            for (int i = starting; i < starting + amount; i++) {
                result.dimension.set(i, vector.dimension.get(i) + other.dimension.get(i));
            }
        }
    }

    public static class DotHelper implements Runnable {
        Vector vector;
        Vector other;
        ArrayList<Integer> result;
        int starting;
        int amount;

        public DotHelper(Vector vector, Vector other, ArrayList<Integer> result, int starting, int amount) {
            this.vector = vector;
            this.other = other;
            this.result = result;
            this.starting = starting;
            this.amount = amount;
        }

        @Override
        public void run() {
            for (int i = starting; i < starting + amount; i++) {
                result.set(i, vector.dimension.get(i) * other.dimension.get(i));
            }
        }
    }

    Vector sum(Vector other) {
        if (this.dimension.size() != other.dimension.size()) {
            System.out.println("Wektory są różnej długości");
            return new Vector(new ArrayList<>());
        }
        ArrayList<Integer> arr = new ArrayList<>(Collections.nCopies(dimension.size(), 0));
        Vector result = new Vector(arr);

        int threadNumber = Math.ceilDiv(dimension.size(), 10);
        int actualNumber = dimension.size();

        Thread[] threads = new Thread[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            if (actualNumber > 10) {
                threads[i] = new Thread(new AddingHelper(this, other, result, i * 10, 10));
            } else {
                threads[i] = new Thread(new AddingHelper(this, other, result, i * 10, actualNumber));
            }
            actualNumber -= 10;
            threads[i].start();
        }
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.out.println("Proces zatrzymany!");
        }
        return result;
    }

    int dot(Vector other) {
        if (this.dimension.size() != other.dimension.size()) {
            System.out.println("Wektory są różnej długości");
            return 0;
        }
        Integer result = 0;
        ArrayList<Integer> arr = new ArrayList<>(Collections.nCopies(dimension.size(), 0));

        int threadNumber = Math.ceilDiv(dimension.size(), 10);
        int actualNumber = dimension.size();

        Thread[] threads = new Thread[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            if (actualNumber > 10) {
                threads[i] = new Thread(new DotHelper(this, other, arr, i * 10, 10));
            } else {
                threads[i] = new Thread(new DotHelper(this, other, arr, i * 10, actualNumber));
            }
            actualNumber -= 10;
            threads[i].start();
        }
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.out.println("Proces zatrzymany!");
        }
        for (Integer element : arr) {
            result += element;
        }
        return result;
    }

    Vector sumSeq(Vector other) {
        if (this.dimension.size() == other.dimension.size()) {
            ArrayList<Integer> newDimension = new ArrayList<>();
            for (int i = 0; i < this.dimension.size(); i++) {
                newDimension.add(this.dimension.get(i) + other.dimension.get(i));
            }
            return new Vector(newDimension);
        } else {
            System.out.println("Wektory mają różne długości");
            return new Vector(new ArrayList<>());
        }
    }

    int dotSeq(Vector other) {
        if (this.dimension.size() == other.dimension.size()) {
            int res = 0;
            for (int i = 0; i < this.dimension.size(); i++) {
                res += (this.dimension.get(i) * other.dimension.get(i));
            }
            return res;
        } else {
            System.out.println("Wektory mają różne długości");
            return 0;
        }
    }

    void vectorPrint() {
        if (this.dimension.size() > 0) {
            System.out.print("[");
            for (int i = 0; i < this.dimension.size() - 1; i++) {
                System.out.print(this.dimension.get(i) + ", ");
            }
            System.out.print(this.dimension.get(this.dimension.size() - 1) + "]");
        } else {
            System.out.print("[]");
        }
        System.out.println();
    }

    static Vector randomVector(int dimensionSize) {
        Vector result = new Vector(new ArrayList<>());
        for (int i = 0; i < dimensionSize; i++) {
            Random random = new Random();
            result.dimension.add(random.nextInt(1000));
        }
        return result;
    }

    static void printTest(Vector sumSeqRes, Vector sumRes, int dotSeqRes, int dotRes) {
        System.out.println("Wektor będący sumą, policzony sekwencyjnie: ");
        sumSeqRes.vectorPrint();
        System.out.println();
        System.out.println("Wektor będący sumą, policzony współbieżnie: ");
        sumRes.vectorPrint();

        System.out.println();
        System.out.println("Iloczyn skalarny policzony sekwencyjnie: ");
        System.out.println(dotSeqRes);
        System.out.println();
        System.out.println("Iloczyn skalarny policzony współbieżnie: ");
        System.out.println(dotRes);
        System.out.println();
    }

    static void overallTest(int n, boolean print) {

        System.out.println("Starting overallTest...");

        Random random = new Random();

        for (int i = 0; i < n; i++) {
            int size = random.nextInt(100);

            Vector vector1 = randomVector(size);
            Vector vector2 = randomVector(size);

            if (!vector1.sumSeq(vector2).dimension.equals(vector1.sum(vector2).dimension)
                    || vector1.dotSeq(vector2) != vector1.dot(vector2)) {
                System.out.println("Test " + i + " failed!");
                printTest(vector1.sumSeq(vector2), vector1.sum(vector2), vector1.dotSeq(vector2), vector1.dot(vector2));
                return;
            } else if (print) {
                printTest(vector1.sumSeq(vector2), vector1.sum(vector2), vector1.dotSeq(vector2), vector1.dot(vector2));
            }
        }
        System.out.println("All tests passed!");
    }

    /* aby zobaczyć dokładnie dane wektorów,
    można zmienić wartość print na true */
    public static void main(String[] args) {
        overallTest(10000, true);

    }
}