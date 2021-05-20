package react.experiments.flowable;

import lombok.Builder;
import lombok.Getter;

import java.net.DatagramPacket;

@Builder
@Getter
public class Event
{
    final private EventType type;
    final private DatagramPacket packet;
}
