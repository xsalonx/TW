package concurrentBuffer;
import pseudoCond.PseudoCond;
import thracing.*;

import java.util.concurrent.locks.Condition;

public class BufferTwoCond extends Buffer {

    private final Condition producersCond = lock.newCondition();
    private final Condition consumersCond = lock.newCondition();


    public BufferTwoCond(int size, PseudoCond pseudoCond) {
        super(size, pseudoCond);
    }
    public BufferTwoCond(int size, PseudoCond pseudoCond, ThreadTracingLogger threadTracingLogger) {
        super(size, pseudoCond, threadTracingLogger);
    }

    private void signalEveryone() {
        producersCond.signalAll();
        consumersCond.signalAll();
    }


    @Override
    public void produce(int[] data) {
        lock.lock();

        try {
            while (currentSize + data.length > buffer.length)
                producersCond.await();

            putData(data);
            printBufferState(data.length);

            producersCond.signal();
            consumersCond.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int[] consume(int size) {
        lock.lock();

        int[] ret = new int[size];
        try {
            while (currentSize < size)
                consumersCond.await();

            takeData(ret);
            printBufferState(-size);

            consumersCond.signal();
            producersCond.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return ret;
    }

    /*
     *
     *
     *
     *
     * */

    @Override
    public void produce(int[] data, int index) {

        lock.lock();

        threadTracingLogger.logProducerAccessingMonitor(index);
        int length = data.length;
        try {
            while (currentSize + length > buffer.length) {
                threadTracingLogger.logProducer(index, length);
                producersCond.await();
                threadTracingLogger.logProducerAccessingMonitor(index);
                threadTracingLogger.unlogProducer(index);
            }

            putData(data);
            printBufferState(length);
            threadTracingLogger.logProducerCompletingTask(index);

            producersCond.signal();
            consumersCond.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int[] consume(int size, int index) {

        lock.lock();

        threadTracingLogger.logConsumerAccessingMonitor(index);

        int[] ret = new int[size];
        try {

            while (currentSize < size) {
                threadTracingLogger.logConsumer(index, size);
                consumersCond.await();
                threadTracingLogger.logConsumerAccessingMonitor(index);
                threadTracingLogger.unlogConsumer(index);
            }

            takeData(ret);
            printBufferState(-size);
            threadTracingLogger.logConsumerCompletingTask(index);

            consumersCond.signal();
            producersCond.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return ret;
    }
}