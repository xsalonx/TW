package tracing.historyState;

public abstract class StateI {

    int linesLength = 0;
    String getTopDownBorder() {
        return  "-".repeat(linesLength) +
                '\n';
    }
    abstract public String toString();
}
