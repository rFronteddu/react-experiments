package react.experiments.flowable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.Test;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;


public class ReceiverTest
{
    /**
     * Test a simple udp receiver
     */
    @Test public void canGenerate () {
        int port = 1234;
        try {
            final PacketReceiverFlowable prf = new PacketReceiverFlowable (port);
            senderThread (prf, port, true);

            Flowable<Event> source = prf.getSocketFlowable ();
            Disposable subscribe = source
                    .subscribe ((Event e) -> {
                        switch (e.getType ()) {
                            case PACKET:
                                System.out.println ("Received packet\t\t\t" + System.nanoTime ());
                                break;
                            case SIGNAL_1:
                                throw new RuntimeException ("Unexpected type SIGNAL_1");
                        }

                    }, (Throwable t) -> {
                        if (t.getMessage ().contains ("closed")) {
                            System.out.println ("Socket was closed");
                            // will now exit subscription
                        } else {
                            t.printStackTrace ();
                            System.exit (-1);
                        }
                    });
            System.out.println ("Terminating...");

        } catch (SocketException e) {
            System.out.println ("SocketException");
            e.printStackTrace ();
            System.exit (-1);
        }
    }

    /**
     * Test merging of events of different type without necessarily waiting for both
     */
    @Test public void canMix () {
        boolean printGeneration = true;
        boolean printReceive = false;

        int port = 1234;
        try {
            final PacketReceiverFlowable prf = new PacketReceiverFlowable (port);
            senderThread (prf, port, printGeneration);

            Flowable<Event> packetSource = prf.getSocketFlowable ();
            Flowable<Event> signalSource = SignalFlowable.getSignalFlowable (EventType.SIGNAL_1, 10, printGeneration);
            Flowable<Event> signalSource2 = SignalFlowable.getSignalFlowable (EventType.SIGNAL_2, 10, printGeneration);

            Flowable.merge (
                    packetSource.subscribeOn (Schedulers.io ()),
                    signalSource.subscribeOn (Schedulers.io ()),
                    signalSource2.subscribeOn (Schedulers.io ()))
                    .blockingSubscribe ((Event e) -> {
                        switch (e.getType ()) {
                            case PACKET:
                                if (printReceive) {
                                    System.out.println ("Received packet\t\t\t" + System.nanoTime ());
                                }
                                break;
                            case SIGNAL_1:
                                if (printReceive) {
                                    System.out.println ("Received a signal1\t\t\t" + System.nanoTime ());
                                }
                                break;
                            case SIGNAL_2:
                                if (printReceive) {
                                    System.out.println ("Received a signal2\t\t\t" + System.nanoTime ());
                                }
                                break;
                        }

                    }, (Throwable t) -> {
                        if (t.getMessage ().contains ("closed")) {
                            System.out.println ("Socket was closed");
                            // will now exit subscription
                        } else {
                            t.printStackTrace ();
                            System.exit (-1);
                        }
                    });
        } catch (SocketException e) {
            System.out.println ("SocketException");
            e.printStackTrace ();
            System.exit (-1);
        }
    }

    private void senderThread (PacketReceiverFlowable prf, int port, boolean printGeneration) {
        new Thread (() -> {
            try {
                Thread.sleep (1000);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket ();
            } catch (SocketException e) {
                e.printStackTrace ();
            }
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep (30);
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
                byte[] b = new byte[256];
                Arrays.fill (b, (byte) 0);
                InetSocketAddress isa = new InetSocketAddress ("127.0.0.1", port);
                DatagramPacket p = new DatagramPacket (b, b.length, isa);
                try {
                    socket.send (p);
                } catch (IOException e) {
                    e.printStackTrace ();
                }
                if (printGeneration) {
                    System.out.println ("sending\t\t\t\t\t" + System.nanoTime ());
                }

            }
            prf.close ();
            // simulate application termination
        }).start ();
    }
}
