package react.experiments;

import io.reactivex.rxjava3.core.Flowable;

/**
 * To use RxJava you create Observables (which emits data items), transform those Observables to get items that
 * interest you (using Observable operators), and then react to these sequences of interesting items (by implementing
 * Observers or Subscribers and then subscribing them to the resulting transformed Observables)
 */
public class App
{
    public static void hello (String... args) {
        Flowable.fromArray (args).subscribe (s -> System.out.println ("Hello " + s + "!"));
    }

    public static void main (String[] args) {
        hello("mike", "thom", "carl");
    }
}
