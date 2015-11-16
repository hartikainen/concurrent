package tasks;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import messages.*;
import akka.actor.*;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.FiniteDuration;

public class PartyBot extends UntypedActor {

    private static final String USERNAME = "PartyBot";
    private static final String FESTIVE_MESSAGE = "Party! Party!";
    private static final Object JOIN_CHANNELS = new Object();
    private static final FiniteDuration PARTY_DURATION =
        FiniteDuration.create(5, TimeUnit.SECONDS);
    private final ExecutionContext ec = context().system().dispatcher();
    private final HashMap<String, ActorRef> currentChannels =
        new HashMap<String, ActorRef>();

    public PartyBot() {
        context().system().scheduler().schedule(FiniteDuration.Zero(),
                                                PARTY_DURATION,
                                                self(),
                                                JOIN_CHANNELS,
                                                ec,
                                                self());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        // TODO: Check the implemented functionality
        if (msg == JOIN_CHANNELS) {
            ActorSelection channelSelection = context()
                .actorSelection("/user/channels/*");
            // TODO: How to join only channels not already joined?
            channelSelection.tell(new AddUser(self()), self());
        } else if (msg instanceof UserAdded) {
            final UserAdded added = (UserAdded) msg;

            currentChannels.put(added.channelName, added.channel);
            getSender().tell(new ChatMessage(USERNAME,
                                             FESTIVE_MESSAGE),
                                             self());
        } else if (msg instanceof UserRemoved) {
            final UserRemoved removed = (UserRemoved) msg;

            currentChannels.remove(removed.channelName);
            System.out.println("removed from channel");
        } else {
            unhandled(msg);
        }
    }
}
