import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ThreadTracingLogger {

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
        getOnLineOfToString(IntStream.rangeClosed(0, n-1).toArray(), stringBuilder, cellWidth);
        stringBuilder.append('\n');

        getOnLineOfToString(producersAccessingMonitorTimes, stringBuilder, cellWidth);
        stringBuilder.append('\n');
        getOnLineOfToString(producersCompletingTaskTimes, stringBuilder, cellWidth);
        stringBuilder.append('\n');
        stringBuilder.append("-".repeat(100) + "\n");
        getOnLineOfToString(consumersAccessingMonitorTimes, stringBuilder, cellWidth);
        stringBuilder.append('\n');
        getOnLineOfToString(consumersCompletingTaskTimes, stringBuilder, cellWidth);
        stringBuilder.append('\n');

        return stringBuilder.toString();
    }

    private void getOnLineOfToString(int[] arr, StringBuilder stringBuilder, int cellWidth) {
        int spaceBetweenCells = 4;
        int leftPadding = 25;

        stringBuilder.append(" ".repeat(leftPadding));
        for (int a : arr) {
            stringBuilder.append(" ".repeat(cellWidth - digitNumb(a) + spaceBetweenCells));
            stringBuilder.append(a);
        }
    }

    private int digitNumb(int n) {
        return (int) (Math.floor(Math.log10(Math.max(1, n))) + 1);
    }

}
