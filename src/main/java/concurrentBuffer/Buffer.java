package concurrentBuffer;
import pseudoCond.*;
import tracing.*;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Buffer {

    final int[] buffer;
    final PseudoCond pseudoCond;

    int currentSize = 0;
    int putIndex = 0;
    int takeIndex = 0;

    final ReentrantLock lock = new ReentrantLock(true);


    Buffer(int size, PseudoCond pseudoCond) {
        buffer = new int[size];
        this.pseudoCond = pseudoCond;
    }

    public abstract ThreadTracingLoggerI getTracer();


    void putData(int[] data) {
        for (int i=0; i<data.length; i++)
            buffer[(putIndex + i) % buffer.length] = data[i];
        putIndex = (putIndex + data.length) % buffer.length;
        currentSize += data.length;
    }

    void takeData(int[] ret) {
        int size = ret.length;
        for (int i=0; i<size; i++)
            ret[i] = buffer[(takeIndex + i) % buffer.length];
        takeIndex = (takeIndex + size) % buffer.length;
        currentSize -= size;
    }

    public String toStringBufferState() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        if (putIndex < takeIndex || currentSize == buffer.length) {
            stringBuilder.append("+".repeat(putIndex));
            stringBuilder.append(".".repeat(takeIndex - putIndex));
            stringBuilder.append("+".repeat(buffer.length - takeIndex));
        } else {
            stringBuilder.append(".".repeat(takeIndex));
            stringBuilder.append("+".repeat(putIndex - takeIndex));
            stringBuilder.append(".".repeat(buffer.length - putIndex));
        }
        stringBuilder.append(']').append("  current size:").append(currentSize);

        return stringBuilder.toString();
    }

    public void printBufferState() {
        System.out.println(toStringBufferState());
    }
    void printBufferState(int change) {
        System.out.println(toStringBufferState() + " change:" + change);
    }


    // TODO
    boolean cannotPut(int size) {
        return (currentSize + size > buffer.length);
    }
    boolean cannotTake(int size) {
        return (currentSize - size < 0);
    }

    public abstract void produce(int[] data);
    public abstract int[] consume(int size);

    public abstract void produce(int[] data, int index);
    public abstract int[] consume(int size, int index);

}
