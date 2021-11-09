package concurrentBuffer;
import pseudoCond.PseudoCond;
import thracing.*;

import java.util.concurrent.locks.Condition;

public class BufferFourCond extends Buffer{


    private final Condition producersCond = lock.newCondition();
    private final Condition firstProducerCond = lock.newCondition();

    private final Condition consumersCond = lock.newCondition();
    private final Condition firstConsumerCond = lock.newCondition();


    public BufferFourCond(int size, PseudoCond pseudoCond) {
        super(size, pseudoCond);
    }
    public BufferFourCond(int size, PseudoCond pseudoCond, ThreadTracingLogger threadTracingLogger) {
        super(size, pseudoCond, threadTracingLogger);
    }


    private void signalEveryone() {
        producersCond.signalAll();
        firstProducerCond.signal();
        consumersCond.signalAll();
        firstConsumerCond.signal();
    }


    @Override
    public void produce(int[] data) {
        lock.lock();

        try {
            while (lock.hasWaiters(firstProducerCond))
                producersCond.await();

            while (currentSize + data.length > buffer.length)
                firstProducerCond.await();

            putData(data);
            printBufferState(data.length);

            producersCond.signal();
            firstConsumerCond.signal();

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
            while(lock.hasWaiters(firstConsumerCond))
                consumersCond.await();

            while (currentSize < size)
                firstConsumerCond.await();

            takeData(ret);
            printBufferState(-size);

            consumersCond.signal();
            firstProducerCond.signal();

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
            while (lock.hasWaiters(firstProducerCond)) {
                threadTracingLogger.logProducer(index, length);
                producersCond.await();
                threadTracingLogger.logProducerAccessingMonitor(index);
                threadTracingLogger.unlogProducer(index);
            }

            while (currentSize + length > buffer.length) {
                threadTracingLogger.logFirstProducer(index, length);
                firstProducerCond.await();
                threadTracingLogger.logProducerAccessingMonitor(index);
                threadTracingLogger.unlogFirstProducer(index);
            }

            putData(data);
            printBufferState(data.length);
            threadTracingLogger.logProducerCompletingTask(index);

            producersCond.signal();
            firstConsumerCond.signal();

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
            while(lock.hasWaiters(firstConsumerCond)) {
                threadTracingLogger.logConsumer(index, size);
                consumersCond.await();
                threadTracingLogger.logConsumerAccessingMonitor(index);
                threadTracingLogger.unlogConsumer(index);
            }
            while (currentSize < size) {
                threadTracingLogger.logFirstConsumer(index, size);
                firstConsumerCond.await();
                threadTracingLogger.logConsumerAccessingMonitor(index);
                threadTracingLogger.unlogFirstConsumer(index);
            }

            takeData(ret);
            printBufferState(-size);
            threadTracingLogger.logConsumerCompletingTask(index);

            consumersCond.signal();
            firstProducerCond.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return ret;
    }
}
