import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.IntStream;

public class ThreadTracingLogger {

    private int leftPadding = 25;
    private int spaceBetweenCells = 4;
    private int linesLength = 0;

    private final int[] producersAccessingMonitorTimes;
    private final int[] producersCompletingTaskTimes;

    private final HashSet<Integer> firstProducerWaiters;
    private final HashSet<Integer> producersWaiters;


    private final int[] consumersAccessingMonitorTimes;
    private final int[] consumersCompletingTaskTimes;

    private final HashSet<Integer> firstConsumerWaiters;
    private final HashSet<Integer> consumersWaiters;


    // TODO handling history o conditions "queues" changing;

    public ThreadTracingLogger(int producersNumb, int consumersNumb) {
        producersAccessingMonitorTimes = new int[producersNumb];
        producersCompletingTaskTimes = new int[producersNumb];
        firstProducerWaiters = new HashSet<>();
        producersWaiters = new HashSet<>();

        consumersAccessingMonitorTimes = new int[consumersNumb];
        consumersCompletingTaskTimes = new int[consumersNumb];
        firstConsumerWaiters = new HashSet<>();
        consumersWaiters = new HashSet<>();


    }

    public void logConsumerAccessingMonitor(int index) {
        consumersAccessingMonitorTimes[index] ++;
    }
    public void logConsumerCompletingTask(int index) {
        consumersCompletingTaskTimes[index] ++;
    }

    public void logFirstConsumer(int index) {firstConsumerWaiters.add(index);}
    public void unlogFirstConsumer(int index) {firstConsumerWaiters.remove(index);}
    public void logConsumer(int index) {consumersWaiters.add(index);}
    public void unlogConsumer(int index) {consumersWaiters.remove(index);}


    public void logProducerAccessingMonitor(int index) {
        producersAccessingMonitorTimes[index] ++;
    }
    public void logProducerCompletingTask(int index) {
        producersCompletingTaskTimes[index] ++;
    }

    public void logFirstProducer(int index) {firstProducerWaiters.add(index);}
    public void unlogFirstProducer(int index) {firstProducerWaiters.remove(index);}
    public void logProducer(int index) {producersWaiters.add(index);}
    public void unlogProducer(int index) {producersWaiters.remove(index);}


    public void save(String filePath) {
        System.out.println("saving logs not implemented");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        int max1 = Arrays.stream(producersAccessingMonitorTimes).max().getAsInt();
        int max2 = Arrays.stream(producersCompletingTaskTimes).max().getAsInt();
        int max3 = Arrays.stream(consumersAccessingMonitorTimes).max().getAsInt();
        int max4 = Arrays.stream(consumersCompletingTaskTimes).max().getAsInt();

        int max = Math.max(Math.max(max1, max2), Math.max(max3, max4));
        int cellWidth = digitNumb(max);

        int n = Math.max(producersAccessingMonitorTimes.length, consumersAccessingMonitorTimes.length);
        String indexesLine = getOnLineOfToString(IntStream.rangeClosed(0, n-1).toArray(), cellWidth, "worker index");
        linesLength = indexesLine.length() + 5;
        stringBuilder
                .append("/".repeat(linesLength))
                .append('\n')
                .append(("/").repeat(linesLength))
                .append('\n');
        stringBuilder.append(indexesLine).append('\n');


        stringBuilder
                .append("_".repeat(linesLength))
                .append('\n')
                .append(getOnLineOfToString(producersAccessingMonitorTimes,
                                            cellWidth, "producers accessing times"))
                .append('\n')
                .append(getOnLineOfToString(producersCompletingTaskTimes,
                                            cellWidth, "producers completed tasks"))
                .append('\n')
                .append("-".repeat(linesLength))
                .append('\n')
                .append(getOnLineOfToString(
                        getRatio(producersCompletingTaskTimes, producersAccessingMonitorTimes),
                        cellWidth, "ratios"))
                .append('\n')
                .append(",".repeat(linesLength))
                .append("\n\n")
                .append(getOnLineOfToString(consumersAccessingMonitorTimes,
                                            cellWidth, "consumers accessing times"))
                .append('\n')
                .append(getOnLineOfToString(consumersCompletingTaskTimes,
                                            cellWidth, "consumers completed tasks"))
                .append('\n')
                .append("-".repeat(linesLength))
                .append('\n')
                .append(getOnLineOfToString(
                        getRatio(consumersCompletingTaskTimes, consumersAccessingMonitorTimes),
                        cellWidth, "ratios"))
                .append('\n')
                .append("\\".repeat(linesLength))

                .append('\n')
                .append(getOnLineOfToString(waitersSetToArr(firstProducerWaiters),
                                            cellWidth, "first producer waiter"))
                .append('\n')
                .append(getOnLineOfToString(waitersSetToArr(producersWaiters),
                                            cellWidth, "producer waiters"))
                .append('\n')
                .append(getOnLineOfToString(waitersSetToArr(firstConsumerWaiters),
                                            cellWidth, "first consumer waiter"))
                .append('\n')
                .append(getOnLineOfToString(waitersSetToArr(consumersWaiters),
                                            cellWidth, "consumers waiters"))
                .append('\n')

                .append("/".repeat(linesLength))
                .append('\n')
                .append("/".repeat(linesLength))
                .append('\n');

        return stringBuilder.toString();
    }


    private int[] waitersSetToArr(HashSet<Integer> hs) {
        int[] arr = new int[hs.size()];
        int i=0;
        for (int a : hs) {
            arr[i] = a;
            i ++;
        }
        return arr;
    }


    private float[] getRatio(int[] nominators, int[] denominators) {
        assert nominators.length == denominators.length;
        float[] ratios = new float[nominators.length];
        for (int i =0; i<nominators.length; i++) {
            ratios[i] = (float) nominators[i] / (float) denominators[i];
        }
        return ratios;
    }

    private String getOnLineOfToString(int[] arr, int cellWidth, String rowTitle) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(rowTitle).append(" ".repeat(leftPadding - rowTitle.length()));
        for (int a : arr) {
            stringBuilder.append(" ".repeat(cellWidth - digitNumb(a) + spaceBetweenCells));
            stringBuilder.append(a);
        }
        return stringBuilder.toString();
    }
    private String getOnLineOfToString(float[] arr, int cellWidth, String rowTitle) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(rowTitle).append(" ".repeat(leftPadding - rowTitle.length()));
        for (float a : arr) {
            String formatedFloat = String.format("%.02f", a);
            stringBuilder.append(" ".repeat(cellWidth - formatedFloat.length() + spaceBetweenCells));
            stringBuilder.append(formatedFloat);
        }
        return stringBuilder.toString();
    }

    private int digitNumb(int n) {
        return (int) (Math.floor(Math.log10(Math.max(1, n))) + 1);
    }

}
