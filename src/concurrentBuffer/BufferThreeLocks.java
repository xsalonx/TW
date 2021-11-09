package concurrentBuffer;
import pseudoCond.PseudoCond;
import tracing.*;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BufferThreeLocks extends Buffer {

    final ReentrantLock lockOfProducers = new ReentrantLock(true);
    final ReentrantLock lockOfConsumers = new ReentrantLock(true);

    final Condition finalAccessCondition = lock.newCondition();

    private final ThreeLocksTracer tracer;

    BufferThreeLocks(int size, PseudoCond pseudoCond) {
        super(size, pseudoCond);
        tracer = null;
    }

    public BufferThreeLocks(int size, PseudoCond pseudoCond, int producersNumb, int consumersNumb) {
        super(size, pseudoCond);
        tracer = new ThreeLocksTracer(producersNumb, consumersNumb, true);
        tracer.setBuffer(this);
    }

    @Override
    public ThreadTracingLoggerI getTracer() {
        return (ThreadTracingLoggerI) tracer;
    }

    void signalEveryone() {}

    @Override
    void produce(int[] data) {
        lockOfProducers.lock();

        int size = data.length;
        try {
            lock.lock();
            while (cannotPut(size))
                finalAccessCondition.await();

            putData(data);
            printBufferState(size);

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
            while (cannotTake(size))
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

        tracer.logProducerAccessingOuterLock(index);
        int size = data.length;
        try {

            lock.lock();
            tracer.logProducerAccessingInnerLock(index);
            while (cannotPut(size)) {
                finalAccessCondition.await();
                tracer.logProducerAccessingInnerLock(index);
            }

            putData(data);
            printBufferState(data.length);
            tracer.logProducerCompletingTask(index);

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

        tracer.logConsumerAccessingOuterLock(index);

        int[] ret = new int[size];
        try {

            lock.lock();
            tracer.logConsumerAccessingInnerLock(index);
            while (cannotTake(size)) {
                finalAccessCondition.await();
                tracer.logConsumerAccessingInnerLock(index);
            }

            takeData(ret);
            printBufferState(-size);
            tracer.logConsumerCompletingTask(index);

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
