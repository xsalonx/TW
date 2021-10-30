import javax.swing.*;
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

    private void signalEveryone() {
        producersCond.signalAll();
        firstProducerCond.signal();
        consumersCond.signalAll();
        firstConsumerCond.signal();
    }

    public Buffer(int size, PseudoCond pseudoCond) {
        buffer = new Integer[size];
        this.pseudoCond = pseudoCond;
    }

    private void putData(int[] data) {
        for (int i=0; i<data.length; i++)
            buffer[(putIndex + i) % buffer.length] = data[i];
        putIndex = (putIndex + data.length) % buffer.length;
        currentSize += data.length;
    }

    private void takeData(int[] ret) {
        int size = ret.length;
        for (int i=0; i<size; i++)
            ret[i] = buffer[(takeIndex + i) % buffer.length];
        takeIndex = (takeIndex + size) % buffer.length;
        currentSize -= size;
    }

    private String getBufferState() {
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

    private void logBufferState() {
        System.out.println(getBufferState());
    }
    private void logBufferState(int change) {
        System.out.println(getBufferState() + " " + change);
    }



    public void produce(int[] data) {
        aLock.lock();
        if (pseudoCond.end) {
            signalEveryone();
            aLock.unlock();
            return;
        }


        try {
            while (aLock.hasWaiters(firstProducerCond))
                producersCond.await();

            while (currentSize + data.length > buffer.length)
                firstProducerCond.await();

            putData(data);
            logBufferState(data.length);

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
        if (pseudoCond.end) {
            signalEveryone();
            aLock.unlock();
            return new int[size];
        }


        int[] ret = new int[size];
        try {
            while( aLock.hasWaiters(firstConsumerCond))
                consumersCond.await();

            while (currentSize < size)
                firstConsumerCond.await();

            takeData(ret);
            logBufferState(-size);

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
