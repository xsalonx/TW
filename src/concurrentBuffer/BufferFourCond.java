package concurrentBuffer;
import pseudoCond.PseudoCond;
import tracing.*;

import java.util.concurrent.locks.Condition;

public class BufferFourCond extends Buffer{


    private final Condition producersCond = lock.newCondition();
    private final Condition firstProducerCond = lock.newCondition();

    private final Condition consumersCond = lock.newCondition();
    private final Condition firstConsumerCond = lock.newCondition();

    private final FourConditionsTracer tracer;
    


    @Override
    public ThreadTracingLoggerI getTracer() {
        return tracer;
    }

    public BufferFourCond(int size, PseudoCond pseudoCond, int producersNumb, int consumersNumb) {
        super(size, pseudoCond);
        tracer = new FourConditionsTracer(producersNumb, consumersNumb, true);
        tracer.setBuffer(this);
    }



    @Override
    public void produce(int[] data, int index) {

        lock.lock();
        tracer.logProducerAccessingMonitor(index, false);

        int size = data.length;
        try {
            while (lock.hasWaiters(firstProducerCond)) {
                tracer.logProducer(index, size, true);
                producersCond.await();
                tracer.unlogProducer(index, true);
            }

            while (cannotPut(size)) {
                tracer.logFirstProducer(index, size, true);
                firstProducerCond.await();
                tracer.unlogFirstProducer(index, true);
            }

            putData(data);
            printBufferState(size);
            tracer.logProducerCompletingTask(index, true);

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

        tracer.logConsumerAccessingMonitor(index, false);

        int[] ret = new int[size];
        try {
            while(lock.hasWaiters(firstConsumerCond)) {
                tracer.logConsumer(index, size, true);
                consumersCond.await();
                tracer.logConsumerAccessingMonitor(index, false);
                tracer.unlogConsumer(index, true);
            }
            while (cannotTake(size)) {
                tracer.logFirstConsumer(index, size, true);
                firstConsumerCond.await();
                tracer.logConsumerAccessingMonitor(index, false);
                tracer.unlogFirstConsumer(index, true);
            }

            takeData(ret);
            printBufferState(-size);
            tracer.logConsumerCompletingTask(index, true);

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
