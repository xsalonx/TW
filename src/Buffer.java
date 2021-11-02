import java.util.concurrent.locks.ReentrantLock;

abstract class Buffer {

    final int[] buffer;
    final PseudoCond pseudoCond;
    final ThreadTracingLogger threadTracingLogger;

    int currentSize = 0;
    int putIndex = 0;
    int takeIndex = 0;

    final ReentrantLock aLock = new ReentrantLock();


    Buffer(int size, PseudoCond pseudoCond) {
        buffer = new int[size];
        this.pseudoCond = pseudoCond;
        this.threadTracingLogger = null;
    }

    Buffer(int size, PseudoCond pseudoCond, ThreadTracingLogger threadTracingLogger) {
        buffer = new int[size];
        this.pseudoCond = pseudoCond;
        this.threadTracingLogger = threadTracingLogger;
    }



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

    String toStringBufferState() {
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
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    void printBufferState() {
        System.out.println(toStringBufferState() + " " + currentSize);
    }
    void printBufferState(int change) {
        System.out.println(toStringBufferState() + " " + change);
    }


    abstract void produce(int[] data);
    abstract int[] consume(int size);

    abstract void produce(int[] data, int index);
    abstract int[] consume(int size, int index);

}
