package concurrentBuffer;
import pseudoCond.*;
import tracing.*;

import java.util.concurrent.locks.Condition;

public class BufferFourCond extends Buffer{


    private final Condition producersCond = lock.newCondition();
    private final Condition firstProducerCond = lock.newCondition();

    private final Condition consumersCond = lock.newCondition();
    private final Condition firstConsumerCond = lock.newCondition();

    private final FourConditionsTracer tracer;
    
    public BufferFourCond(int size, PseudoCond pseudoCond) {
        super(size, pseudoCond);
        tracer = null;
    }

    @Override
    public ThreadTracingLoggerI getTracer() {
        return (ThreadTracingLoggerI) tracer;
    }

    public BufferFourCond(int size, PseudoCond pseudoCond, int producersNumb, int consumersNumb) {
        super(size, pseudoCond);
        tracer = new FourConditionsTracer(producersNumb, consumersNumb, true);
        tracer.setBuffer(this);
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

        int size = data.length;
        try {
            while (lock.hasWaiters(firstProducerCond))
                producersCond.await();

            while (cannotPut(size))
                firstProducerCond.await();

            putData(data);
            printBufferState(size);

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

            while (cannotTake(size))
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

        tracer.logProducerAccessingMonitor(index);
        int size = data.length;
        try {
            while (lock.hasWaiters(firstProducerCond)) {
                tracer.logProducer(index, size);
                producersCond.await();
                tracer.logProducerAccessingMonitor(index);
                tracer.unlogProducer(index);
            }

            while (cannotPut(size)) {
                tracer.logFirstProducer(index, size);
                firstProducerCond.await();
                tracer.logProducerAccessingMonitor(index);
                tracer.unlogFirstProducer(index);
            }

            putData(data);
            printBufferState(size);
            tracer.logProducerCompletingTask(index);

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

        tracer.logConsumerAccessingMonitor(index);

        int[] ret = new int[size];
        try {
            while(lock.hasWaiters(firstConsumerCond)) {
                tracer.logConsumer(index, size);
                consumersCond.await();
                tracer.logConsumerAccessingMonitor(index);
                tracer.unlogConsumer(index);
            }
            while (cannotTake(size)) {
                tracer.logFirstConsumer(index, size);
                firstConsumerCond.await();
                tracer.logConsumerAccessingMonitor(index);
                tracer.unlogFirstConsumer(index);
            }

            takeData(ret);
            printBufferState(-size);
            tracer.logConsumerCompletingTask(index);

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
