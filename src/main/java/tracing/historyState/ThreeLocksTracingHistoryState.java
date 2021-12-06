package tracing.historyState;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ThreeLocksTracingHistoryState extends StateI{

    private final int leftPadding = 35;
    private final int spaceBetweenCells = 2;

    public final int[] producersAccessingOuterLockTimes;
    public final int[] producersAccessingInnerLockTimes;
    public final int[] producersCompletingTaskTimes;

    public final int[] consumersAccessingOuterLockTimes;
    public final int[] consumersAccessingInnerLockTimes;
    public final int[] consumersCompletingTaskTimes;


    public String bufferState;


    public ThreeLocksTracingHistoryState(int producersNumb, int consumersNumb) {
        producersAccessingOuterLockTimes = new int[producersNumb];
        producersAccessingInnerLockTimes = new int[producersNumb];
        producersCompletingTaskTimes = new int[producersNumb];


        consumersAccessingOuterLockTimes = new int[consumersNumb];
        consumersAccessingInnerLockTimes = new int[consumersNumb];
        consumersCompletingTaskTimes = new int[consumersNumb];

        bufferState = "";
    }

    public ThreeLocksTracingHistoryState(ThreeLocksTracingHistoryState state) {
        producersAccessingOuterLockTimes = state.producersAccessingOuterLockTimes.clone();
        producersAccessingInnerLockTimes = state.producersAccessingInnerLockTimes.clone();
        producersCompletingTaskTimes = state.producersCompletingTaskTimes.clone();

        consumersAccessingOuterLockTimes = state.consumersAccessingOuterLockTimes.clone();
        consumersAccessingInnerLockTimes = state.consumersAccessingInnerLockTimes.clone();
        consumersCompletingTaskTimes = state.consumersCompletingTaskTimes.clone();

        bufferState = state.bufferState;
    }

    private int calcCellWidth() {
        int max1 = Arrays.stream(producersAccessingOuterLockTimes).max().getAsInt();
        int max5 = Arrays.stream(producersAccessingInnerLockTimes).max().getAsInt();
        int max2 = Arrays.stream(producersCompletingTaskTimes).max().getAsInt();

        int max3 = Arrays.stream(consumersAccessingOuterLockTimes).max().getAsInt();
        int max6 = Arrays.stream(consumersAccessingInnerLockTimes).max().getAsInt();
        int max4 = Arrays.stream(consumersCompletingTaskTimes).max().getAsInt();

        int max = Math.max(Math.max(Math.max(max1, max2), Math.max(max3, max4)), Math.max(max5, max6));
        int cellWidth = digitNumb(max);

        return cellWidth;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        int cellWidth = calcCellWidth();

        int n = Math.max(producersAccessingOuterLockTimes.length, consumersAccessingOuterLockTimes.length);
        String indexesLine = getOnLineOfToString(IntStream.rangeClosed(0, n-1).toArray(), cellWidth, "worker index");
        linesLength = indexesLine.length() + 5;
        stringBuilder
                .append(getTopDownBorder());
        stringBuilder.append(indexesLine).append('\n');


        stringBuilder
                .append(toStringAccessAndCompletionData(producersAccessingOuterLockTimes, producersAccessingInnerLockTimes, producersCompletingTaskTimes))
                .append(toStringAccessAndCompletionData(consumersAccessingOuterLockTimes, consumersAccessingInnerLockTimes, consumersCompletingTaskTimes))
                .append(getTopDownBorder());

        return stringBuilder.toString();
    }

    String toStringAccessAndCompletionData(int[] outerLockAccess, int[] innerLockAccess, int[] completion) {
        int cellWidth = calcCellWidth();
        int[] locksAccessTime = new int[outerLockAccess.length];
        Arrays.setAll(locksAccessTime, i -> outerLockAccess[i] + innerLockAccess[i]);
        return '\n' +
                getOnLineOfToString(outerLockAccess,
                        cellWidth, "worker outer lock accessing times") +
                '\n' +
                getOnLineOfToString(innerLockAccess,
                        cellWidth, "worker inner lock accessing times") +
                '\n' +
                getOnLineOfToString(completion,
                        cellWidth, "worker completed tasks") +
                '\n' +
                (getOnLineOfToString(
                        getRatio(completion, locksAccessTime),
                        cellWidth, "ratios")) +
                '\n';
    }

    public String toStringWaitersData() {
        return "not implemented";
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
            ratios[i] = 2 * (float) nominators[i] / (float) denominators[i];
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
