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
            final String name = ((GetOrCreateChannel) msg).name;
            final ActorRef channelActor;

            // if the channel does not exist, create new channel and add it
            // to the channelMap
            if (channelMap.get(name) == null) {
                channelActor = context()
                    .actorOf(Props.create(Channel.class), name);
                channelMap.put(name, channelActor);
            } else {
                channelActor = channelMap.get(name);
            }

            getSender().tell(channelActor, getSelf());
        } else {
            unhandled(msg);
        }
    }
}
