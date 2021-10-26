import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer {

    private final Integer[] buffer;
    private final PseudoCond pseudoCond;
    private int currentSize = 0;
    private int putIndex = 0;
    private int takeIndex = 0;

    private final Lock aLock = new ReentrantLock();
    private final Condition producersCond = aLock.newCondition();
    private final Condition consumersCond = aLock.newCondition();

    public Buffer(int size, PseudoCond pseudoCond) {
        buffer = new Integer[size];
        this.pseudoCond = pseudoCond;
    }

    public void produce(int data) {
        aLock.lock();
        try {
            if (currentSize == buffer.length) {
                try {
                    producersCond.await();
                } catch (InterruptedException ignore) {}
            }

            buffer[putIndex] = data;
            putIndex = (putIndex + 1) % buffer.length;
            currentSize ++;

            consumersCond.signal();
        } finally {
            aLock.unlock();
        }

    }

    public int consume() {
        aLock.lock();
        int ret;
        try {
            if (currentSize == 0) {
                try {
                    consumersCond.await();
                } catch (InterruptedException ignore) {}
            }

            ret = buffer[takeIndex];
            takeIndex = (takeIndex + 1) % buffer.length;
            currentSize--;

            producersCond.signal();
        } finally {
            aLock.unlock();
        }
        return ret;
    }
}
