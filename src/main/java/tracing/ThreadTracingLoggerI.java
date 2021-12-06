package tracing;

import tracing.historyState.StateI;

public interface ThreadTracingLoggerI {
    StateI getCurrentState();

    String toStringHistoryTail(int tail);

    String toStringWaitersSetsStatesTail(int tail);

    void save(String filePath);
}
