import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.IntStream;

public class TracingHistoryState {

    private final int leftPadding = 25;
    private final int spaceBetweenCells = 2;
    private int linesLength = 0;


    final int[] producersAccessingMonitorTimes;
    final int[] producersCompletingTaskTimes;

    final HashSet<Integer> firstProducerWaiters;
    final HashSet<Integer> producersWaiters;


    final int[] consumersAccessingMonitorTimes;
    final int[] consumersCompletingTaskTimes;

    final HashSet<Integer> firstConsumerWaiters;
    final HashSet<Integer> consumersWaiters;


    TracingHistoryState(int producersNumb, int consumersNumb) {
        producersAccessingMonitorTimes = new int[producersNumb];
        producersCompletingTaskTimes = new int[producersNumb];
        firstProducerWaiters = new HashSet<>();
        producersWaiters = new HashSet<>();

        consumersAccessingMonitorTimes = new int[consumersNumb];
        consumersCompletingTaskTimes = new int[consumersNumb];
        firstConsumerWaiters = new HashSet<>();
        consumersWaiters = new HashSet<>();
    }

    TracingHistoryState(TracingHistoryState tracingHistoryState) {
        producersAccessingMonitorTimes = tracingHistoryState.producersAccessingMonitorTimes.clone();
        producersCompletingTaskTimes = tracingHistoryState.producersCompletingTaskTimes.clone();

        firstProducerWaiters = (HashSet<Integer>) tracingHistoryState.firstProducerWaiters.clone();
        producersWaiters = (HashSet<Integer>) tracingHistoryState.producersWaiters.clone();


        consumersAccessingMonitorTimes = tracingHistoryState.consumersAccessingMonitorTimes.clone();
        consumersCompletingTaskTimes = tracingHistoryState.consumersCompletingTaskTimes.clone();

        firstConsumerWaiters = (HashSet<Integer>) tracingHistoryState.firstConsumerWaiters.clone();
        consumersWaiters = (HashSet<Integer>) tracingHistoryState.consumersWaiters.clone();
    }

    private int calcCellWidth() {
        int max1 = Arrays.stream(producersAccessingMonitorTimes).max().getAsInt();
        int max2 = Arrays.stream(producersCompletingTaskTimes).max().getAsInt();
        int max3 = Arrays.stream(consumersAccessingMonitorTimes).max().getAsInt();
        int max4 = Arrays.stream(consumersCompletingTaskTimes).max().getAsInt();

        int max = Math.max(Math.max(max1, max2), Math.max(max3, max4));
        int cellWidth = digitNumb(max);

        return cellWidth;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        int cellWidth = calcCellWidth();

        int n = Math.max(producersAccessingMonitorTimes.length, consumersAccessingMonitorTimes.length);
        String indexesLine = getOnLineOfToString(IntStream.rangeClosed(0, n-1).toArray(), cellWidth, "worker index");
        linesLength = indexesLine.length() + 5;
        stringBuilder
                .append(getTopDownBorder());
        stringBuilder.append(indexesLine).append('\n');


        stringBuilder
                .append("_".repeat(linesLength))
                .append(toStringAccessAndCompletionData(producersAccessingMonitorTimes, producersCompletingTaskTimes))
                .append(",".repeat(linesLength))
                .append("\n\n")
                .append(toStringAccessAndCompletionData(consumersAccessingMonitorTimes, consumersCompletingTaskTimes))
                .append("\\".repeat(linesLength))
                .append('\n')
                .append(toStringWaitersData())
                .append(getTopDownBorder());

        return stringBuilder.toString();
    }

    String getTopDownBorder() {
        return  "/".repeat(linesLength) +
                '\n' +
                "/".repeat(linesLength) +
                '\n';
    }

    String toStringAccessAndCompletionData(int[] access, int[] completion) {
        int cellWidth = calcCellWidth();
        return '\n' +
                getOnLineOfToString(access,
                        cellWidth, "producers accessing times") +
                '\n' +
                getOnLineOfToString(completion,
                        cellWidth, "producers completed tasks") +
                '\n' +
                "-".repeat(linesLength) +
                '\n' +
                (getOnLineOfToString(
                        getRatio(access, completion),
                        cellWidth, "ratios")) +
                '\n';
    }

    String toStringWaitersData() {
        int cellWidth = digitNumb(Math.max(producersAccessingMonitorTimes.length, consumersAccessingMonitorTimes.length));
        return getOnLineOfToString(waitersSetToArr(firstProducerWaiters),
                cellWidth, "first producer waiter") +
                '\n' +
                getOnLineOfToString(waitersSetToArr(producersWaiters),
                        cellWidth, "producer waiters") +
                '\n' +
                getOnLineOfToString(waitersSetToArr(firstConsumerWaiters),
                        cellWidth, "first consumer waiter") +
                '\n' +
                getOnLineOfToString(waitersSetToArr(consumersWaiters),
                        cellWidth, "consumers waiters") +
                '\n';
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
            stringBuilder.append(" ".repeat(Math.max(cellWidth - formatedFloat.length() + spaceBetweenCells, 1)));
            stringBuilder.append(formatedFloat);
        }
        return stringBuilder.toString();
    }

    private int digitNumb(int n) {
        return (int) (Math.floor(Math.log10(Math.max(1, n))) + 1);
    }
}
