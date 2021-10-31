import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ThreadTracingLogger {

    private int leftPadding = 25;
    private int spaceBetweenCells = 4;

    private final int[] producersAccessingMonitorTimes;
    private final int[] producersCompletingTaskTimes;

    private final int[] consumersAccessingMonitorTimes;
    private final int[] consumersCompletingTaskTimes;

    // TODO handling history o conditions "queues" changing;

    public ThreadTracingLogger(int producersNumb, int consumersNumb) {
        producersAccessingMonitorTimes = new int[producersNumb];
        producersCompletingTaskTimes = new int[producersNumb];

        consumersAccessingMonitorTimes = new int[consumersNumb];
        consumersCompletingTaskTimes = new int[consumersNumb];
    }

    public void logConsumerAccessingMonitor(int index) {
        consumersAccessingMonitorTimes[index] ++;
    }
    public void logConsumerCompletingTask(int index) {
        consumersCompletingTaskTimes[index] ++;
    }

    public void logProducerAccessingMonitor(int index) {
        producersAccessingMonitorTimes[index] ++;
    }
    public void logProducerCompletingTask(int index) {
        producersCompletingTaskTimes[index] ++;
    }


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
        stringBuilder.append(getOnLineOfToString(IntStream.rangeClosed(0, n-1).toArray(), cellWidth, "")).append('\n');

        stringBuilder
                .append(getOnLineOfToString(producersAccessingMonitorTimes, cellWidth, "producers accessing times"))
                .append('\n')
                .append(getOnLineOfToString(producersCompletingTaskTimes, cellWidth, "producers completed tasks"))
                .append('\n')
                .append("-".repeat(100))
                .append('\n')
                .append(getOnLineOfToString(consumersAccessingMonitorTimes, cellWidth, "consumers accessing times"))
                .append('\n')
                .append(getOnLineOfToString(consumersCompletingTaskTimes, cellWidth, "consumers completed tasks"))
                .append('\n');

        return stringBuilder.toString();
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

    private int digitNumb(int n) {
        return (int) (Math.floor(Math.log10(Math.max(1, n))) + 1);
    }

}
