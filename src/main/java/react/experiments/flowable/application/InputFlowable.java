package react.experiments.flowable.application;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @param <T> Object to pass to subscribers
 */
public class InputFlowable <T>
{
    private final BlockingDeque<T> inputQueue = new LinkedBlockingDeque<> ();

    /**
     * @param obj Event to pass to subscriber
     */
    public void addInput (T obj) {
        inputQueue.add (obj);
    }

    public Flowable<T> getInputFlowable () {
        return Flowable
                .create (emitter -> {
                    while (!emitter.isCancelled ()) {
                        final T obj = inputQueue.take ();
                        emitter.onNext (obj);
                    }
                    // end-of-sequence for consumer/s termination
                    emitter.onComplete ();
                }, BackpressureStrategy.BUFFER);
    }
}
