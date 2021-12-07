package tracing;

import concurrentBuffer.Buffer;
import tracing.historyState.FourCondTracingHistoryState;

import java.util.ArrayList;


public class FourConditionsTracer implements ThreadTracingLoggerI{



    FourCondTracingHistoryState currentState;
    private final ArrayList<FourCondTracingHistoryState> history = new ArrayList<>();
    private boolean saveHistory;
    private Buffer buffer;


    public FourConditionsTracer(int producersNumb, int consumersNumb, boolean saveHistory) {
        currentState = new FourCondTracingHistoryState(producersNumb, consumersNumb);
        this.saveHistory = saveHistory;
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }

    private void saveStateIntoHistory() {
        FourCondTracingHistoryState tracingHistoryState = new FourCondTracingHistoryState(currentState);
        tracingHistoryState.bufferState = buffer.toStringBufferState();
        history.add(tracingHistoryState);
    }


    public void logProducerAccessingMonitor(int index, int size, boolean save) {
        currentState.producersAccessingMonitorTimes[index] ++;
        currentState.setWorkerInMonitor(index, 'p', size);
        currentState.setCurrentCompliting(null, null, null);

        if (save) saveStateIntoHistory();
    }

    public void logProducerCompletingTask(int index, int size, boolean save) {
        currentState.producersCompletingTaskTimes[index] ++;
        currentState.setWorkerInMonitor(null, null, null);
        currentState.setCurrentCompliting(index, 'p', size);

        if (save) saveStateIntoHistory();
    }

    public void logConsumerAccessingMonitor(int index, int size, boolean save) {
        currentState.consumersAccessingMonitorTimes[index] ++;
        currentState.setWorkerInMonitor(index, 'c', size);
        currentState.setCurrentCompliting(null, null, null);

        if (save) saveStateIntoHistory();
    }

    public void logConsumerCompletingTask(int index, int size, boolean save) {
        currentState.consumersCompletingTaskTimes[index] ++;
        currentState.setWorkerInMonitor(null, null, null);
        currentState.setCurrentCompliting(index, 'c', size);

        if (save) saveStateIntoHistory();
    }


    public void logFirstProducer(int index, int size, boolean save) {
        currentState.firstProducerWaiters.put(index, size);
        currentState.setWorkerInMonitor(null, null, null);
        currentState.setCurrentCompliting(null, null, null);

        if (save) saveStateIntoHistory();
    }

    public void unlogFirstProducer(int index, int size, boolean save) {
        currentState.firstProducerWaiters.remove(index);
        currentState.setWorkerInMonitor(index, 'p', size);
        currentState.setCurrentCompliting(null, null, null);

        if (save) saveStateIntoHistory();
    }

    public void logProducer(int index, int size, boolean save) {
        currentState.producersWaiters.put(index, size);
        currentState.setWorkerInMonitor(null, null, null);
        currentState.setCurrentCompliting(null, null, null);
        if (save) saveStateIntoHistory();
    }
    public void unlogProducer(int index, int size, boolean save) {
        currentState.producersWaiters.remove(index);
        currentState.setWorkerInMonitor(index, 'p', size);
        currentState.setCurrentCompliting(null, null, null);

        if (save) saveStateIntoHistory();
    }

    public void logFirstConsumer(int index, int size, boolean save) {
        currentState.firstConsumerWaiters.put(index, size);
        currentState.setWorkerInMonitor(null, null, null);
        currentState.setCurrentCompliting(null, null, null);

        if (save) saveStateIntoHistory();
    }

    public void unlogFirstConsumer(int index, int size, boolean save) {
        currentState.firstConsumerWaiters.remove(index);
        currentState.setWorkerInMonitor(index, 'c', size);
        currentState.setCurrentCompliting(null, null, null);

        if (save) saveStateIntoHistory();
    }

    public void logConsumer(int index, int size, boolean save) {
        currentState.consumersWaiters.put(index, size);
        currentState.setWorkerInMonitor(null, null, null);
        currentState.setCurrentCompliting(null, null, null);

        if (save) saveStateIntoHistory();
    }

    public void unlogConsumer(int index, int size, boolean save) {
        currentState.consumersWaiters.remove(index);
        currentState.setWorkerInMonitor(index, 'c', size);
        currentState.setCurrentCompliting(null, null, null);

        if (save) saveStateIntoHistory();
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
        for (FourCondTracingHistoryState s : history) {
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
        for (FourCondTracingHistoryState s : history) {
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

    public FourCondTracingHistoryState getCurrentState() {
        return currentState;
    }

    @Override
    public String toString() {
        return currentState.toString();
    }

}
