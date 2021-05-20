package react.experiments.flowable;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * To create an observable you can either implement the Observable's behavior manually by passing
 * a function to create ( ) that exhibits Observable behavior, or you can convert an existing data
 * structure into an Observable by using operators designed for this Purpose
 */
public class PacketReceiverFlowable
{
    final DatagramSocket socket;

    public PacketReceiverFlowable (int port) throws SocketException {
        socket = new DatagramSocket (port);
    }

    /**
     * flowable will throw SocketException if it is in receive which is ok
     */
    public void close () {
        socket.close ();
    }

    public Flowable<Event> getSocketFlowable () {
        final int size = 256;
        return Flowable
                .create (emitter -> {
            DatagramPacket p = newPacket (size);
            while (true) {
                socket.receive (p);
                emitter.onNext (Event.builder ().type (EventType.PACKET).packet (p).build ());
                // consumer may have cancelled the flow
                if (emitter.isCancelled ()) {
                    break;
                }
                p = newPacket (size);
            }
            socket.close ();
            // end-of-sequence for consumer/s termination
            emitter.onComplete ();
        }, BackpressureStrategy.BUFFER);
    }

    private DatagramPacket newPacket (int bytes) {
        byte[] buf = new byte[bytes];
        return new DatagramPacket (buf, buf.length);
    }
}
