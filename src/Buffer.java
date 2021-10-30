import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer {

    private final Integer[] buffer;
    private final PseudoCond pseudoCond;
    private int currentSize = 0;
    private int putIndex = 0;
    private int takeIndex = 0;

    private final ReentrantLock aLock = new ReentrantLock();

    private final Condition producersCond = aLock.newCondition();
    private final Condition firstProducerCond = aLock.newCondition();

    private final Condition consumersCond = aLock.newCondition();
    private final Condition firstConsumerCond = aLock.newCondition();


    public Buffer(int size, PseudoCond pseudoCond) {
        buffer = new Integer[size];
        this.pseudoCond = pseudoCond;
    }

    public void produce(int[] data) {
        aLock.lock();
        try {
            while (aLock.hasWaiters(firstProducerCond))
                producersCond.await();

            while (currentSize + data.length > buffer.length)
                firstProducerCond.await();

            for (int i=0; i<data.length; i++)
                buffer[(putIndex + i) % buffer.length] = data[i];

            putIndex = (putIndex + data.length) % buffer.length;
            currentSize += data.length;

            producersCond.signal();
            firstConsumerCond.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            aLock.unlock();
        }
    }

    public int[] consume(int size) {
        aLock.lock();
        int[] ret = new int[size];
        try {
            while( aLock.hasWaiters(firstConsumerCond))
                consumersCond.await();

            while (currentSize < size)
                firstConsumerCond.await();


            for (int i=0; i<size; i++)
                ret[i] = buffer[(takeIndex + i) % buffer.length];

            takeIndex = (takeIndex + size) % buffer.length;
            currentSize -= size;

            consumersCond.signal();
            firstProducerCond.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            aLock.unlock();
        }
        return ret;
    }
}
