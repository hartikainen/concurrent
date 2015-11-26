package tasks;

import akka.japi.Function;
import scala.concurrent.duration.Duration;

import akka.actor.*;

import messages.*;
import akkachat.JokeGenerator;
import akkachat.DidNotGetJokeException;
import akkachat.JokeConnectionClosedException;

public class Joker extends UntypedActor {
    private final String JOKE_CHANNEL = "jokes";
    private final String USERNAME = "Joker";
    private final int NUM_RESETS = 10;

    @Override
    public void preStart() {
        // TODO: do we need to wait for the response?
        context().actorSelection("/user/channels")
            .tell(new GetOrCreateChannel(JOKE_CHANNEL), self());
        ActorRef jokeGenerator = context().actorOf(
            Props.create(JokeGenerator.class),
            "jokeGenerator");
        context().system().eventStream().subscribe(self(), NewSession.class);
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        // TODO: Implement the required functionality.
        if (msg instanceof String) {
            final String joke = (String) msg;
            context().actorSelection("/user/channels/" + JOKE_CHANNEL)
                .tell(new ChatMessage(USERNAME, joke), self());
        } else if (msg instanceof NewSession) {
            final NewSession session = (NewSession) msg;
            // TODO: what should be the last param of the .tell in this case?
            context().actorSelection("/user/channels/" + JOKE_CHANNEL)
                .tell(new AddUser(session.session), self());
        } else {
            unhandled(msg);
        }
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        Function<Throwable, SupervisorStrategy.Directive> decider =
            new Function<Throwable, SupervisorStrategy.Directive>() {
                @Override
                public SupervisorStrategy.Directive apply(Throwable t) {
                    if (t instanceof DidNotGetJokeException) {
                        return SupervisorStrategy.resume();
                    } else if (t instanceof JokeConnectionClosedException) {
                        return SupervisorStrategy.restart();
                    } else {
                        return SupervisorStrategy.escalate();
                    }
                }
            };
        return new OneForOneStrategy(NUM_RESETS, Duration.Inf(), decider);
    }
}
