package tracing;

import concurrentBuffer.Buffer;
import tracing.historyState.FourCondTracingHistoryState;
import tracing.historyState.ThreeLocksTracingHistoryState;

import java.util.ArrayList;


public class ThreeLocksTracer implements ThreadTracingLoggerI {



    ThreeLocksTracingHistoryState currentState;
    private final ArrayList<ThreeLocksTracingHistoryState> history = new ArrayList<>();
    private boolean saveHistory = false;
    private Buffer buffer;


    public ThreeLocksTracer(int producersNumb, int consumersNumb) {
        currentState = new ThreeLocksTracingHistoryState(producersNumb, consumersNumb);
    }

    public ThreeLocksTracer(int producersNumb, int consumersNumb, boolean saveHistory) {
        currentState = new ThreeLocksTracingHistoryState(producersNumb, consumersNumb);
        this.saveHistory = saveHistory;
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }

    private void saveIfEnabledCurrentStateIntoHistory() {
        if (saveHistory) {
            ThreeLocksTracingHistoryState tracingHistoryState = new ThreeLocksTracingHistoryState(currentState);
//            tracingHistoryState.bufferState = buffer.toStringBufferState();
            history.add(tracingHistoryState);

        }
    }

    public void logProducerAccessingOuterLock(int index) {
        currentState.producersAccessingOuterLockTimes[index] ++;
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void logProducerAccessingInnerLock(int index) {
        currentState.producersAccessingInnerLockTimes[index] ++;
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void logProducerCompletingTask(int index) {
        currentState.producersCompletingTaskTimes[index] ++;
        saveIfEnabledCurrentStateIntoHistory();
    }

    public void logConsumerAccessingOuterLock(int index) {
        currentState.consumersAccessingOuterLockTimes[index] ++;
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void logConsumerAccessingInnerLock(int index) {
        currentState.consumersAccessingInnerLockTimes[index] ++;
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void logConsumerCompletingTask(int index) {
        currentState.consumersCompletingTaskTimes[index] ++;
        saveIfEnabledCurrentStateIntoHistory();
    }





    public void save(String filePath) {
        System.out.println("saving logs not implemented");
    }



    public String toStringCurrentState() {
        return currentState.toString();
    }

    public String toStringHistoryTail(int tail) {
        StringBuilder stringBuilder = new StringBuilder();
        int i=1;
        int size = history.size();
        tail = Math.min(size, tail);
        for (ThreeLocksTracingHistoryState s : history) {
            if (i >= size - tail + 1) {
                System.out.println("parsing: " + i + "/" + size);
                stringBuilder.append(i).append("/").append(size).append("  ").append("-".repeat(100)).append('\n');
                stringBuilder.append(s);
            }
            i++;
        }

        return stringBuilder.toString();
    }

    public String toStringWaitersSetsStatesTail(int tail) {
        int i=1;
        int size = history.size();
        tail = Math.min(size, tail);
        StringBuilder stringBuilder = new StringBuilder();
        for (ThreeLocksTracingHistoryState s : history) {
            if (i >= size - tail + 1) {
                System.out.println("parsing: " + i + "/" + size);
                stringBuilder.append(i).append("/").append(size).append("  ").append("-".repeat(100)).append('\n');
                stringBuilder.append(s.toStringWaitersData());
                stringBuilder.append(s.bufferState).append('\n');
            }
            i++;
        }

        return stringBuilder.toString();
    }

    public ThreeLocksTracingHistoryState getCurrentState() {
        return currentState;
    }

    @Override
    public String toString() {
        return currentState.toString();
    }

}
