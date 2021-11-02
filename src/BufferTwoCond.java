import java.util.concurrent.locks.Condition;

public class BufferTwoCond extends Buffer {

    private final Condition producersCond = aLock.newCondition();
    private final Condition consumersCond = aLock.newCondition();


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
        aLock.lock();
        if (pseudoCond.end) {
            signalEveryone();
            aLock.unlock();
            return;
        }

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
            while (currentSize < size)
                consumersCond.await();

            takeData(ret);
            printBufferState(-size);

            consumersCond.signal();
            producersCond.signal();

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
            while (currentSize + data.length > buffer.length) {
                threadTracingLogger.logProducer(index);
                producersCond.await();
                threadTracingLogger.logProducerAccessingMonitor(index);
                threadTracingLogger.unlogProducer(index);
            }

            putData(data);
            printBufferState(data.length);
            threadTracingLogger.logProducerCompletingTask(index);

            producersCond.signal();
            consumersCond.signal();

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

            while (currentSize < size) {
                threadTracingLogger.logConsumer(index);
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
            aLock.unlock();
        }

        return ret;
    }
}