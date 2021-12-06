import concurrentBuffer.*;
import pseudoCond.*;
import timeMeasure.*;

import java.util.Arrays;
import java.util.Random;


public class MainTimeMeasures {


    static class Worker implements Runnable {

        private final String name;
        private final int index;
        private final String role;
        private final Buffer buffer;
        private final Random random = new Random();


        Worker(String name, String role, Buffer buffer, int index) {
            this.name = name;
            this.role = role;
            this.buffer = buffer;
            this.index = index;
        }

        private int getRandSize() {
            if (index > alterPoint)
                return random.nextInt(dataSizeUpperBound_2 - dataSizeLowerBound_2 + 1) + dataSizeLowerBound_2;
            else
                return random.nextInt(dataSizeUpperBound_1 - dataSizeLowerBound_1 + 1) + dataSizeLowerBound_1;
        }

        private int[] genRandData() {
            Random random = new Random();
            int[] data = new int[getRandSize()];
            for (int i = 0; i < data.length; i++) {
                data[i] = random.nextInt(dataBound);
            }
            return data;
        }

        @Override
        public void run() throws IllegalArgumentException {
            int[] data;
            int size;
            float start, end;

            while (!pseudoCond.end) {
                sleep(workersDelay);
                if (!pseudoCond.stop) {

                    if (role.equals("producer")) {
                        start = System.nanoTime();
                        data = genRandData();
                        buffer.produce(data);
                        end = System.nanoTime();
                        timeMeter.logProducerTime(index, end - start);
                    } else if (role.equals("consumer")) {
                        start = System.nanoTime();
                        size = getRandSize();
                        data = buffer.consume(size);
                        end = System.nanoTime();
                        timeMeter.logConsumerTime(index, end - start);
                    } else {
                        throw new IllegalArgumentException("Incorrect role for worker");
                    }

                } else {
                    pseudoCond.wait_();
                }
            }
        }
    }


    static private PseudoCond pseudoCond = new PseudoCond();
    static private int dataSizeUpperBound_1;
    static private int dataSizeLowerBound_1;
    static private int dataSizeUpperBound_2;
    static private int dataSizeLowerBound_2;

    static private int dataBound = 2;
    static private int workersDelay = 1;
    static private int alterPoint = Integer.MAX_VALUE;

    static private TimeMeter timeMeter;


    public static void main(String[] args) {

        int producersNumb = Integer.parseInt(args[1]);
        int consumersNumb = Integer.parseInt(args[2]);
        int bufferSize = Integer.parseInt(args[3]);

        dataSizeLowerBound_1 = 1;
        dataSizeUpperBound_1 = bufferSize / 4;

        alterPoint = Math.max(producersNumb, consumersNumb) / 2;

        dataSizeLowerBound_2 = bufferSize / 4;
        dataSizeUpperBound_2 = bufferSize / 2;

        int secondsOfMeasuring = Integer.parseInt(args[4]);

        Buffer buffer = null;
        String monitor_type = args[0];
        switch (monitor_type) {
            case "2":
                buffer = new BufferTwoCond(bufferSize, pseudoCond);
                break;
            case "3":
                buffer = new BufferThreeLocks(bufferSize, pseudoCond);
                break;
            case "4":
                buffer = new BufferFourCond(bufferSize, pseudoCond);
                break;
        }
        if (buffer == null) {
            System.out.println("incorrect monitor type: " + monitor_type);
            System.exit(1);
        }


        Worker[] producers = initWorkers(producersNumb, "producer", buffer);
        Worker[] consumers = initWorkers(consumersNumb, "consumer", buffer);

        Thread[] producersThreads = declareWorkersThreads(producers);
        Thread[] consumersThreads = declareWorkersThreads(consumers);

        timeMeter = new TimeMeter(producersNumb, consumersNumb, producersThreads, consumersThreads);

        startThreads(producersThreads);
        startThreads(consumersThreads);


        sleep(1000 * secondsOfMeasuring);

        pseudoCond.stop = true;
        sleep(100);

        String mainOutput = timeMeter.toStringTimes().replaceAll("\u001B\\[[;\\d]*m", "");
        int len = mainOutput.split("\n")[0].length();
        System.out.println("_".repeat(len));
        System.out.println(Arrays.toString(args));
        System.out.println(mainOutput);

        System.exit(0);

    }


    static private int getTail(String[] commandAndParams) {
        int tail = 10;
        try {
            if (commandAndParams.length > 1) {
                tail = Integer.parseInt(commandAndParams[1]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println("program is still working");
        }
        return tail;
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
        for (int i = 0; i < n; i++) {
            workers[i] = new Worker(role + i, role, buffer, i);
        }
        return workers;
    }

    private static Thread[] declareWorkersThreads(Worker[] workers) {
        Thread[] workersThreads = new Thread[workers.length];
        for (int i = 0; i < workers.length; i++) {
            workersThreads[i] = new Thread(workers[i]);
        }
        return workersThreads;
    }

    private static void startThreads(Thread[] threads) {
        for (Thread th : threads)
            th.start();
    }

    private static void joinThreads(Thread[] threads) {
        for (Thread th : threads)
            try {
                th.join();
            } catch (InterruptedException ignore) {
            }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignore) {
        }
    }

}
