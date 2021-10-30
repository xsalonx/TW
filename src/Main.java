import java.util.Random;
import java.util.Scanner;

public class Main {

    static private PseudoCond pseudoCond;
    static private int dataSizeUpperBound;
    static private int dataSizeLowerBound;

    static private int dataBound;
    static private int workersDelay;


    static class Worker implements Runnable {

        private final String name;
        private final int index;
        private final String role;
        private final Buffer buffer;


        Worker(String name, String role, Buffer buffer, int index) {
            this.name = name;
            this.role = role;
            this.buffer = buffer;
            this.index = index;
        }

        private static int[] genData() {
            Random random = new Random();
            int[] data = new int[random.nextInt(dataSizeUpperBound - dataSizeLowerBound + 1) + dataSizeLowerBound];
            for (int i=0; i<data.length; i++) {
                data[i] = random.nextInt(dataBound);
            }
            return data;
        }


        @Override
        public void run() throws IllegalArgumentException {
            int[] data;
            int size;
            Random random = new Random();
            while (!pseudoCond.end) {
                sleep(workersDelay);
                if (!pseudoCond.stop) {

                    if (role.equals("producer")) {
                        data = genData();
                        buffer.produce(data);
                    } else if (role.equals("consumer")) {
                        size = random.nextInt(dataSizeUpperBound - dataSizeLowerBound + 1) + dataSizeLowerBound;
                        data = buffer.consume(size);
                    } else {
                        throw new IllegalArgumentException("Incorrect role for worker");
                    }

                } else {
                    pseudoCond.wait_();
                }
            }
        }
    }

    private static String dataToString(int[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        for (int a : data) {
            stringBuilder.append(a);
            stringBuilder.append(", ");
        }
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    private static Worker[] initWorkers(int n, String role, Buffer buffer) {
        Worker[] workers = new Worker[n];
        for (int i=0; i<n; i++) {
            workers[i] = new Worker(role + i, role, buffer, i);
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

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignore) {}
    }

    public static void main(String[] args) {

        /*
        * set of parameters
        * */
        int producersNumb = 15;
        int consumersNumb = 15;
        int bufferSize = 10;
        dataSizeUpperBound = 5;
        dataSizeLowerBound = 1;
        dataBound = 1;
        workersDelay = 1;
        String filePath = "log1.txt";


        pseudoCond = new PseudoCond();
        ThreadTracingLogger threadTracingLogger = new ThreadTracingLogger(producersNumb, consumersNumb);
        Buffer buffer = new Buffer(bufferSize, pseudoCond, threadTracingLogger);

        Worker[] producers = initWorkers(producersNumb, "producer", buffer);
        Worker[] consumers = initWorkers(consumersNumb, "consumer", buffer);

        Thread[] producersThreads = declareWorkersThreads(producers);
        Thread[] consumersThreads = declareWorkersThreads(consumers);

        startThreads(producersThreads);
        startThreads(consumersThreads);


        Scanner scanner = new Scanner(System.in);
        String command = "";
        while (! command.equals("end")) {
            command = scanner.nextLine();
            System.out.println("1: <" + command + ">");
            pseudoCond.stop = true;

            System.out.println(threadTracingLogger);
            threadTracingLogger.save(filePath);

            command = scanner.nextLine();
            System.out.println("2: <" + command + ">");
            sleep(10);
            if (command.equals("continue") || command.equals("end")) {
                pseudoCond.stop = false;
                pseudoCond.notifyAll_();
            }
        }

        System.out.println("out of loop");
        pseudoCond.end = true;
        sleep(1000);

        joinThreads(consumersThreads);
        System.out.println("Consumers joined");
        joinThreads(producersThreads);
        System.out.println("Producers joined");
    }
}

