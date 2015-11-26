package tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.io.Serializable;

import messages.*;
import akka.actor.*;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.FiniteDuration;

public class PartyBot extends UntypedActor {

    private static final String USERNAME = "PartyBot";
    private static final String FESTIVE_GREETING = "Party! Party!";
    private static final JoinAllChannels JOIN_ALL_MSG = new JoinAllChannels();
    private static final FiniteDuration PARTY_DELAY =
        FiniteDuration.create(5, TimeUnit.SECONDS);
    private static final FiniteDuration IMMEDIATELY = FiniteDuration.Zero();

    private final HashMap<String, ActorRef> currentChannels =
        new HashMap<String, ActorRef>();

    public PartyBot() {
        final ExecutionContext ec = context().system().dispatcher();
        context().system().scheduler().schedule(IMMEDIATELY,
                                                PARTY_DELAY,
                                                self(),
                                                JOIN_ALL_MSG,
                                                ec,
                                                self());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        // TODO: Check the implemented functionality
        if (msg instanceof JoinAllChannels) {
            // get all the channels from ChannelManager
            context().actorSelection("/user/channels")
                .tell(new ChannelManager.GetAllChannels(), self());
        } else if (msg instanceof ChannelManager.ChannelList) {
            HashMap<String, ActorRef> allChannels =
                ((ChannelManager.ChannelList)msg).channelMap;
            // check which channels the bot has joined already
            for (Map.Entry<String, ActorRef> channel : allChannels.entrySet()) {
                // if bot is already on the channel -> continue
                if (currentChannels.get(channel.getKey()) != null) continue;
                channel.getValue().tell(new AddUser(self()), self());
            }
        } else if (msg instanceof UserAdded) {
            final UserAdded added = (UserAdded) msg;

            currentChannels.put(added.channelName, added.channel);
            getSender().tell(new ChatMessage(USERNAME,
                                             FESTIVE_GREETING),
                                             self());
        } else if (msg instanceof UserRemoved) {
            final UserRemoved removed = (UserRemoved) msg;

            currentChannels.remove(removed.channelName);
        } else {
            unhandled(msg);
        }
    }


    /**
     * Serializable class for handling PartyBot's scheduler messages
     */
    private static class JoinAllChannels implements Serializable {
	private static final long serialVersionUID = 1L;
    }
}
