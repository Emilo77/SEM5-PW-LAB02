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

        public AddingHelper(Vector vector, Vector other,
                            Vector result, int starting,
                            int amount) {
            this.vector = vector;
            this.other = other;
            this.starting = starting;
            this.amount = amount;
            this.result = result;
        }

        @Override
        public void run() {

            for (int i = starting; i < starting + amount; i++) {
                result.dimension.set(i,
                        vector.dimension.get(i) + other.dimension.get(i));
            }
        }
    }

    public static class DotHelper implements Runnable {
        Vector vector;
        Vector other;
        ArrayList<Integer> result;
        int starting;
        int amount;

        public DotHelper(Vector vector, Vector other, ArrayList<Integer> result,
                         int starting, int amount) {
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
            return new Vector(new ArrayList<Integer>());
        }
        ArrayList<Integer> arr = new ArrayList<Integer>(Collections.nCopies(dimension.size(), 0));
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

        if (result.dimension.size() == 0) {
            result.dimension.add(0);
        }
        return result;
    }

    int dot(Vector other) {
        if (this.dimension.size() != other.dimension.size()) {
            System.out.println("Wektory są różnej długości");
            return 0;
        }
        Integer result = 0;
        ArrayList<Integer> arr = new ArrayList<Integer>(Collections.nCopies(dimension.size(), 0));

        int threadNumber = Math.ceilDiv(dimension.size(), 10);
        int actualNumber = dimension.size();

        Thread[] threads = new Thread[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            if (actualNumber > 10) {
                threads[i] = new Thread(new DotHelper(this, other, arr,
                        i * 10, 10));
            } else {
                threads[i] = new Thread(new DotHelper(this, other,
                        arr, i * 10, actualNumber));
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
        System.out.print("[");
        for(int i = 0; i < this.dimension.size() - 1; i++) {
            System.out.print(this.dimension.get(i) + ", ");
        }
        System.out.print(this.dimension.get(this.dimension.size()-1) + "]");
        System.out.println();
    }
    static void randomVector(Vector vec, int DimensionSize) {
        for(int i = 0; i < DimensionSize; i++) {
            Random random = new Random();
            vec.dimension.add(random.nextInt(1000));
        }
    }

    public static void main(String args[]) {

        ArrayList<Integer> arr1 = new ArrayList<>();
        ArrayList<Integer> arr2 = new ArrayList<>();

        Vector vector1 = new Vector(arr1);
        Vector vector2 = new Vector(arr2);
        Random random = new Random();
        int size = random.nextInt(100);

        randomVector(vector1, size);
        randomVector(vector2, size);

        System.out.println("Wektor będący sumą, policzony sekwencyjnie: ");
        Vector vectorRes = vector1.sumSeq(vector2);
        vectorRes.vectorPrint();

        System.out.println();
        System.out.println("Wektor będący sumą, policzony współbieżnie: ");
        Vector vectorRes2 = vector1.sum(vector2);
        vectorRes2.vectorPrint();

        System.out.println();
        System.out.println("Iloczyn skalarny policzony sekwencyjnie: ");
        System.out.println(vector1.dotSeq(vector2));
        System.out.println();
        System.out.println("Iloczyn skalarny policzony współbieżnie: ");
        System.out.println(vector1.dot(vector2));
    }
}