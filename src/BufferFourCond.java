
import java.util.concurrent.locks.Condition;

public class BufferFourCond extends Buffer{


    private final Condition producersCond = aLock.newCondition();
    private final Condition firstProducerCond = aLock.newCondition();

    private final Condition consumersCond = aLock.newCondition();
    private final Condition firstConsumerCond = aLock.newCondition();


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
            printBufferState(data.length);

            producersCond.signal();
            firstConsumerCond.signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            aLock.unlock();
        }
    }

    @Override
    public int[] consume(int size) {
        aLock.lock();
        if (pseudoCond.end) {
            signalEveryone();
            aLock.unlock();
            return new int[size];
        }


        int[] ret = new int[size];
        try {
            while(aLock.hasWaiters(firstConsumerCond))
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
            aLock.unlock();
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

        aLock.lock();
        if (pseudoCond.end) {
            signalEveryone();
            aLock.unlock();
            return;
        }

        threadTracingLogger.logProducerAccessingMonitor(index);

        try {
            while (aLock.hasWaiters(firstProducerCond)) {
                threadTracingLogger.logProducer(index);
                producersCond.await();
                threadTracingLogger.logProducerAccessingMonitor(index);
                threadTracingLogger.unlogProducer(index);
            }

            while (currentSize + data.length > buffer.length) {
                threadTracingLogger.logFirstProducer(index);
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
            aLock.unlock();
        }
    }

    @Override
    public int[] consume(int size, int index) {

        aLock.lock();
        if (pseudoCond.end) {
            signalEveryone();
            aLock.unlock();
            return new int[size];
        }

        threadTracingLogger.logConsumerAccessingMonitor(index);

        int[] ret = new int[size];
        try {
            while(aLock.hasWaiters(firstConsumerCond)) {
                threadTracingLogger.logConsumer(index);
                consumersCond.await();
                threadTracingLogger.logConsumerAccessingMonitor(index);
                threadTracingLogger.unlogConsumer(index);
            }
            while (currentSize < size) {
                threadTracingLogger.logFirstConsumer(index);
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
            aLock.unlock();
        }

        return ret;
    }
}
