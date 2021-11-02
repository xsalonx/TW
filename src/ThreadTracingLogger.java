import java.util.ArrayList;


public class ThreadTracingLogger {



    TracingHistoryState currentState;
    private final ArrayList<TracingHistoryState> history = new ArrayList<>();
    private boolean saveHistory = false;


    public ThreadTracingLogger(int producersNumb, int consumersNumb) {
        currentState = new TracingHistoryState(producersNumb, consumersNumb);
    }

    public ThreadTracingLogger(int producersNumb, int consumersNumb, boolean saveHistory) {
        currentState = new TracingHistoryState(producersNumb, consumersNumb);
        this.saveHistory = saveHistory;
    }

    private void saveIfEnabledCurrentStateIntoHistory() {
        if (saveHistory)
            history.add(new TracingHistoryState(currentState));
    }

    public void logProducerAccessingMonitor(int index) {
        currentState.producersAccessingMonitorTimes[index] ++;
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void logProducerCompletingTask(int index) {
        currentState.producersCompletingTaskTimes[index] ++;
        saveIfEnabledCurrentStateIntoHistory();
    }

    public void logConsumerAccessingMonitor(int index) {
        currentState.consumersAccessingMonitorTimes[index] ++;
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void logConsumerCompletingTask(int index) {
        currentState.consumersCompletingTaskTimes[index] ++;
        saveIfEnabledCurrentStateIntoHistory();
    }


    public void logFirstProducer(int index, int size) {
        currentState.firstProducerWaiters.put(index, size);
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void unlogFirstProducer(int index) {
        currentState.firstProducerWaiters.remove(index);
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void logProducer(int index, int size) {
        currentState.producersWaiters.put(index, size);
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void unlogProducer(int index) {
        currentState.producersWaiters.remove(index);
        saveIfEnabledCurrentStateIntoHistory();
    }

    public void logFirstConsumer(int index, int size) {
        currentState.firstConsumerWaiters.put(index, size);
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void unlogFirstConsumer(int index) {
        currentState.firstConsumerWaiters.remove(index);
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void logConsumer(int index, int size) {
        currentState.consumersWaiters.put(index, size);
        saveIfEnabledCurrentStateIntoHistory();
    }
    public void unlogConsumer(int index) {
        currentState.consumersWaiters.remove(index);
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
        for (TracingHistoryState s : history) {
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
        for (TracingHistoryState s : history) {
            if (i >= size - tail + 1) {
                System.out.println("parsing: " + i + "/" + size);
                stringBuilder.append(i).append("/").append(size).append("  ").append("-".repeat(100)).append('\n');
                stringBuilder.append(s.toStringWaitersData());
            }
            i++;
        }

        return stringBuilder.toString();
    }


}
