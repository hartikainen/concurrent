package tasks;

import java.util.HashMap;

import messages.*;
import akka.actor.*;

public class ChannelManager extends UntypedActor {
    private final HashMap<String, ActorRef> channelMap =
        new HashMap<String, ActorRef>();

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof GetOrCreateChannel) {
            final String name = ((GetOrCreateChannel)msg).name;
            // TODO: should channelActor be final?
            ActorRef channelActor = channelMap.get(name);

            // if the actor was not found, create new channel and add it
            // to the channelMap
            if (channelActor == null) {
                channelActor = context()
                    .actorOf(Props.create(Channel.class), name);
                channelMap.put(name, channelActor);
            }

            getSender().tell(channelActor, getSelf());
        } else {
            unhandled(msg);
        }
    }
}
