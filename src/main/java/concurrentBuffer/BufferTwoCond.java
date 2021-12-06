package concurrentBuffer;
import pseudoCond.*;
import tracing.*;

import java.util.concurrent.locks.Condition;

public class BufferTwoCond extends Buffer {

    private final Condition producersCond = lock.newCondition();
    private final Condition consumersCond = lock.newCondition();

    private final TwoConditionsTracer tracer;

    public BufferTwoCond(int size, PseudoCond pseudoCond) {
        super(size, pseudoCond);
        tracer = null;
    }
    public BufferTwoCond(int size, PseudoCond pseudoCond, int producersNumb, int consumersNumb) {
        super(size, pseudoCond);
        tracer = new TwoConditionsTracer(producersNumb, consumersNumb, true);
        tracer.setBuffer(this);
    }

    @Override
    public ThreadTracingLoggerI getTracer() {
        return (ThreadTracingLoggerI) tracer;
    }


    @Override
    public void produce(int[] data) {
        lock.lock();

        int size = data.length;
        try {
            while (cannotPut(size))
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
            while (cannotTake(size))
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

        tracer.logProducerAccessingMonitor(index);
        int size = data.length;
        try {
            while (cannotPut(size)) {
                tracer.logProducer(index, size);
                producersCond.await();
                tracer.logProducerAccessingMonitor(index);
                tracer.unlogProducer(index);
            }

            putData(data);
            printBufferState(size);
            tracer.logProducerCompletingTask(index);

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

        tracer.logConsumerAccessingMonitor(index);

        int[] ret = new int[size];
        try {

            while (cannotTake(size)) {
                tracer.logConsumer(index, size);
                consumersCond.await();
                tracer.logConsumerAccessingMonitor(index);
                tracer.unlogConsumer(index);
            }

            takeData(ret);
            printBufferState(-size);
            tracer.logConsumerCompletingTask(index);

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