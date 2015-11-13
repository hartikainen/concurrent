package tasks;

import messages.*;
import akka.actor.*;

public class Channel extends UntypedActor {

    private String getChannelName() {
        return self().path().name();
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        // TODO: Implement the required functionality.
        if (msg instanceof ChatMessage) {

        } else if (msg instanceof AddUser) {

        } else if (msg instanceof RemoveUser) {

        } else
            unhandled(msg);
    }
}
