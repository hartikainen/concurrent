package tasks;

import messages.*;
import akka.actor.*;

public class ChannelManager extends UntypedActor {

    @Override
    public void onReceive(Object msg) throws Exception {
        // TODO: Implement the required functionality.
        if (msg instanceof GetOrCreateChannel) {

        } else
            unhandled(msg);
    }
}
