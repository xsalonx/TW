package tracing.historyState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class FourCondTracingHistoryState implements StateI{

    private final int leftPadding = 25;
    private final int spaceBetweenCells = 2;
    private int linesLength = 0;


    public final int[] producersAccessingMonitorTimes;
    public final int[] producersCompletingTaskTimes;

    public final HashMap<Integer, Integer> firstProducerWaiters;
    public final HashMap<Integer, Integer> producersWaiters;


    public final int[] consumersAccessingMonitorTimes;
    public final int[] consumersCompletingTaskTimes;

    public final HashMap<Integer, Integer> firstConsumerWaiters;
    public final HashMap<Integer, Integer> consumersWaiters;

    public String bufferState;


    public FourCondTracingHistoryState(int producersNumb, int consumersNumb) {
        producersAccessingMonitorTimes = new int[producersNumb];
        producersCompletingTaskTimes = new int[producersNumb];
        firstProducerWaiters = new HashMap<>();
        producersWaiters = new HashMap<>();

        consumersAccessingMonitorTimes = new int[consumersNumb];
        consumersCompletingTaskTimes = new int[consumersNumb];
        firstConsumerWaiters = new HashMap<>();
        consumersWaiters = new HashMap<>();

        bufferState = "";
    }

    public FourCondTracingHistoryState(FourCondTracingHistoryState tracingHistoryState) {
        producersAccessingMonitorTimes = tracingHistoryState.producersAccessingMonitorTimes.clone();
        producersCompletingTaskTimes = tracingHistoryState.producersCompletingTaskTimes.clone();

        firstProducerWaiters = (HashMap<Integer, Integer>) tracingHistoryState.firstProducerWaiters.clone();
        producersWaiters = (HashMap<Integer, Integer>) tracingHistoryState.producersWaiters.clone();


        consumersAccessingMonitorTimes = tracingHistoryState.consumersAccessingMonitorTimes.clone();
        consumersCompletingTaskTimes = tracingHistoryState.consumersCompletingTaskTimes.clone();

        firstConsumerWaiters = (HashMap<Integer, Integer>) tracingHistoryState.firstConsumerWaiters.clone();
        consumersWaiters = (HashMap<Integer, Integer>) tracingHistoryState.consumersWaiters.clone();


        bufferState = tracingHistoryState.bufferState;
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
                        getRatio(completion, access),
                        cellWidth, "ratios")) +
                '\n';
    }

    public String toStringWaitersData() {
        int cellWidth = 2 * digitNumb(Math.max(producersAccessingMonitorTimes.length, consumersAccessingMonitorTimes.length));
        return  waitersToString(firstProducerWaiters,
                        cellWidth, "first producer waiter") +
                '\n' +
                waitersToString(producersWaiters,
                        cellWidth, "producer waiters") +
                '\n' +
                waitersToString(firstConsumerWaiters,
                        cellWidth, "first consumer waiter") +
                '\n' +
                waitersToString(consumersWaiters,
                        cellWidth, "consumers waiters") +
                '\n';
    }

    private String waitersToString(HashMap<Integer, Integer> waiters, int cellWidth, String rowTitle) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(rowTitle).append(" ".repeat(leftPadding - rowTitle.length()));
        for (Map.Entry<Integer, Integer> e : waiters.entrySet()) {
            int index = e.getKey();
            int size = e.getValue();
            stringBuilder.append(" ".repeat(Math.max(cellWidth - digitNumb(index) - digitNumb(size) + spaceBetweenCells, 1)));
            stringBuilder.append("\u001B[31m").append(index).append("\u001B[0m").append(":\u001B[33m").append(size).append("\u001B[0m");
        }

        return stringBuilder.toString();
    }

    private int[] waitersSetToArr(HashMap<Integer, Integer> hs) {
        int[] arr = new int[hs.size()];
        int i=0;
        for (int a : hs.keySet()) {
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
            String formattedFloat = String.format("%.02f", a);
            stringBuilder.append(" ".repeat(Math.max(cellWidth - formattedFloat.length() + spaceBetweenCells, 1)));
            stringBuilder.append(formattedFloat);
        }
        return stringBuilder.toString();
    }

    private int digitNumb(int n) {
        return (int) (Math.floor(Math.log10(Math.max(1, n))) + 1);
    }
}
