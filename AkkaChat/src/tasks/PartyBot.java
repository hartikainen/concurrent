package tasks;

import java.util.ArrayList;
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

    private final ArrayList<ActorRef> currentChannels =
        new ArrayList<ActorRef>();

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
            context().actorSelection("/user/channels/*")
                .tell(new Identify(USERNAME), self());
                //.tell(new ChannelManager.GetAllChannels(), self());
        } else if (msg instanceof ActorIdentity) {
            ActorIdentity identity = (ActorIdentity) msg;
            if (identity.correlationId().equals(USERNAME)) {
                ActorRef channel = identity.getRef();
                if (channel != null && !currentChannels.contains(channel)) {
                    channel.tell(new AddUser(self()), self());
                }
            }
        } else if (msg instanceof UserAdded) {
            final UserAdded added = (UserAdded) msg;

            currentChannels.add(added.channel);
            getSender().tell(new ChatMessage(USERNAME,
                                             FESTIVE_GREETING),
                                             self());
        } else if (msg instanceof UserRemoved) {
            final UserRemoved removed = (UserRemoved) msg;

            currentChannels.remove(removed.channel);
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
