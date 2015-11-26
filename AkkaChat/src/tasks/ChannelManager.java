package tasks;

import java.util.HashMap;
import java.io.Serializable;

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
        } else if (msg instanceof GetAllChannels) {
            // TODO: What's the correct way to send the channels?
            final ChannelList channelsMsg = new ChannelList(this.channelMap);
            getSender().tell(channelsMsg, getSelf());
        } else {
            unhandled(msg);
        }
    }

    public static class GetAllChannels implements Serializable {
	private static final long serialVersionUID = 1L;
    }

    public static class ChannelList implements Serializable {
        private static final long serialVersionUID = 1L;
        public final HashMap<String, ActorRef> channelMap;

        public ChannelList(HashMap<String, ActorRef> channelMap) {
            this.channelMap = channelMap;
        }
    }
}
