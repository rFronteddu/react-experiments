package react.experiments.flowable;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

public class SignalFlowable
{
    public static Flowable<Event> getSignalFlowable (EventType type, int numberOfSignals, boolean print) {
        return Flowable.create (emitter -> {
            Thread.sleep (1000);
            for (int i = 0; i < numberOfSignals; i++) {
                Thread.sleep (10);
                // emit a signal every 50ms
                if (print) {
                    System.out.println ("Generating " + type + "\t\t" + System.nanoTime ());
                }
                emitter.onNext (Event.builder ().type (type).build ());
                // consumer may have cancelled the flow
                if (emitter.isCancelled ()) {
                    break;
                }
            }
            System.out.println ("Signal Generator Completed");
            // end-of-sequence for consumer/s termination
            emitter.onComplete ();
        }, BackpressureStrategy.BUFFER);
    }
}
