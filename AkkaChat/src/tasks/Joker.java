package tasks;

import messages.*;
import akka.actor.*;

public class Joker extends UntypedActor {

    @Override
    public void onReceive(Object msg) throws Exception {
        // TODO: Implement the required functionality.
        unhandled(msg);
    }
}
