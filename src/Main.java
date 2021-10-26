import java.util.Random;
import java.util.Scanner;

public class Main {

    static private PseudoCond pseudoCond;

    static class Worker implements Runnable {

        private final String name;
        private final String role;
        private final Buffer buffer;

        Worker(String name, String role, Buffer buffer) {
            this.name = name;
            this.role = role;
            this.buffer = buffer;
        }


        @Override
        public void run() throws IllegalArgumentException {
            int data;
            Random random = new Random();
            while (!pseudoCond.end) {
                try {
                    Thread.sleep(100, 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!pseudoCond.stop) {

                    if (role.equals("producer")) {
                        data = random.nextInt(10);
                        buffer.produce(data);
                        System.out.println("Producer " + name + " produced: " + data);
                    } else if (role.equals("consumer")) {
                        data = buffer.consume();
                        System.out.println("Consumer " + name + " consumed " + data);
                    } else {
                        throw new IllegalArgumentException("Incorrect role for worker");
                    }

                } else {
                    pseudoCond.wait_();
                }
            }
        }
    }


    private static Worker[] initWorkers(int n, String role, Buffer buffer) {
        Worker[] workers = new Worker[n];
        for (int i=0; i<n; i++) {
            workers[i] = new Worker(role + i, role, buffer);
        }
        return workers;
    }

    private static Thread[] declareWorkersThreads(Worker[] workers) {
        Thread[] workersThreads = new Thread[workers.length];
        for (int i=0; i<workers.length; i++) {
            workersThreads[i] = new Thread(workers[i]);
        }
        return workersThreads;
    }

    private static void startThreads(Thread [] threads) {
        for (Thread th : threads)
            th.start();
    }
    private static void joinThreads(Thread[] threads) {
        for (Thread th : threads)
            try {
                th.join();
            } catch (InterruptedException ignore) { }
    }

    public static void main(String[] args) {

        int P = 3;
        int C = 3;
        int bufferSize = 10;
        pseudoCond = new PseudoCond();
        Buffer buffer = new Buffer(bufferSize, pseudoCond);

        Worker[] producers = initWorkers(P, "producer", buffer);
        Worker[] consumers = initWorkers(C, "consumer", buffer);

        Thread[] producersThreads = declareWorkersThreads(producers);
        Thread[] consumersThreads = declareWorkersThreads(consumers);

        startThreads(producersThreads);
        startThreads(consumersThreads);




        // To end
        Scanner scanner = new Scanner(System.in);
        String command = "";
        while (! command.equals("end")) {
            command = scanner.nextLine();
            System.out.println("1: <" + command + ">");
            pseudoCond.stop = true;
            command = scanner.nextLine();
            System.out.println("2: <" + command + ">");
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignore) {}
            if (command.equals("continue") || command.equals("end")) {
                pseudoCond.stop = false;
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignore) {}
                pseudoCond.notifyAll_();
            }
        }
        System.out.println("out of loop");
        try {
            Thread.sleep(5);
        } catch (InterruptedException ignore) {}
        pseudoCond.end = true;
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignore) {}

        joinThreads(consumersThreads);
        System.out.println("Consumers joined");
        joinThreads(producersThreads);
        System.out.println("Producers joined");
    }
}

