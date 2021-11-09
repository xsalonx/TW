package concurrentBuffer;
import pseudoCond.PseudoCond;
import thracing.*;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BufferThreeLocks extends Buffer {

    final ReentrantLock lockOfProducers = new ReentrantLock(true);
    final ReentrantLock lockOfConsumers = new ReentrantLock(true);

    final Condition finalAccessCondition = lock.newCondition();


    BufferThreeLocks(int size, PseudoCond pseudoCond) {
        super(size, pseudoCond);
    }

    public BufferThreeLocks(int size, PseudoCond pseudoCond, ThreadTracingLogger threadTracingLogger) {
        super(size, pseudoCond, threadTracingLogger);
    }

    void signalEveryone() {}

    @Override
    void produce(int[] data) {
        lockOfProducers.lock();


        try {
            lock.lock();
            while (currentSize + data.length > buffer.length)
                finalAccessCondition.await();

            putData(data);
            printBufferState(data.length);

            finalAccessCondition.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            lockOfProducers.unlock();
        }
    }

    @Override
    int[] consume(int size) {
        lockOfConsumers.lock();


        int[] ret = new int[size];
        try {

            lock.lock();
            while (currentSize < size)
                finalAccessCondition.await();

            takeData(ret);
            printBufferState(-size);

            finalAccessCondition.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            lockOfConsumers.unlock();
        }

        return ret;
    }

    @Override
    public void produce(int[] data, int index) {
        lockOfProducers.lock();

        threadTracingLogger.logProducerAccessingMonitor(index);
        int length = data.length;
        try {

            lock.lock();
            threadTracingLogger.logProducerAccessingMonitor(index);
            while (currentSize + length > buffer.length) {
                // TODO
//                threadTracingLogger.logFirstProducer(index, length);
                finalAccessCondition.await();
                threadTracingLogger.logProducerAccessingMonitor(index);
//                threadTracingLogger.unlogFirstProducer(index);
            }

            putData(data);
            printBufferState(data.length);
            threadTracingLogger.logProducerCompletingTask(index);

            finalAccessCondition.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            lockOfProducers.unlock();
        }
    }

    @Override
    public int[] consume(int size, int index) {
        lockOfConsumers.lock();

        threadTracingLogger.logConsumerAccessingMonitor(index);

        int[] ret = new int[size];
        try {

            lock.lock();
            threadTracingLogger.logConsumerAccessingMonitor(index);
            while (currentSize < size) {
                threadTracingLogger.logFirstConsumer(index, size);
                finalAccessCondition.await();
                threadTracingLogger.logConsumerAccessingMonitor(index);
                threadTracingLogger.unlogFirstConsumer(index);
            }

            takeData(ret);
            printBufferState(-size);
            threadTracingLogger.logConsumerCompletingTask(index);

            finalAccessCondition.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            lockOfConsumers.unlock();
        }

        return ret;
    }
}
