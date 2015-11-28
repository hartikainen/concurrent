package tasks;

import java.util.ArrayList;
import java.util.List;

import messages.*;
import akka.actor.*;

public class Channel extends UntypedActor {
    final List<ChatMessage> messageHistory = new ArrayList<ChatMessage>();
    final List<ActorRef> users = new ArrayList<ActorRef>();

    private String getChannelName() {
        return self().path().name();
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof ChatMessage) {
            final ChatMessage newMessage = (ChatMessage) msg;
            final Backlog message = new Backlog(getChannelName(), newMessage);

            messageHistory.add(newMessage);

            for (ActorRef user : users) {
                user.tell(message, self());
            }
        } else if (msg instanceof AddUser) {
            final ActorRef user = ((AddUser) msg).user;
            final Backlog history = new Backlog(getChannelName(),
                                                messageHistory);

            context().watch(user);
            users.add(user);

            user.tell(new UserAdded(getChannelName(), self()), self());
            user.tell(history, self());
        } else if (msg instanceof RemoveUser) {
            final ActorRef user = ((RemoveUser) msg).user;

            user.tell(new UserRemoved(getChannelName(), self()), self());
            users.remove(user);
        } else if (msg instanceof Terminated) {
            final Terminated terminated = (Terminated) msg;

            users.remove(terminated.getActor());
        } else {
            unhandled(msg);
        }
    }
}
