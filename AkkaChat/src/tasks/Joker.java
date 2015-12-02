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
    private final String JOKER_PATH = "jokeGenerator";
    private final String USERNAME = "Joker";
    private final int NUM_RESETS = 10;

    private ActorRef jokeChannel;
    private ActorRef jokeGenerator;

    @Override
    public void preStart() {
        context().actorSelection("/user/channels")
            .tell(new GetOrCreateChannel(JOKE_CHANNEL), self());
        context().system().eventStream().subscribe(self(), NewSession.class);
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof String &&
            jokeChannel != null &&
            getSender().path().name().equals(JOKER_PATH)) {
            final String joke = (String) msg;

            context().actorSelection("/user/channels/" + JOKE_CHANNEL)
                .tell(new ChatMessage(USERNAME, joke), self());
        } else if (msg instanceof NewSession && jokeChannel != null) {
            final NewSession session = (NewSession) msg;

            context().actorSelection("/user/channels/" + JOKE_CHANNEL)
                .tell(new AddUser(session.session), self());
        } else if (msg instanceof ActorRef && jokeChannel == null) {
            final ActorRef channel = (ActorRef) msg;

            if (channel.path().name().equals(JOKE_CHANNEL)) {
                jokeChannel = channel;
                // msg contains the joke channel -> start the joke generator
                jokeGenerator = context().actorOf(
                    Props.create(JokeGenerator.class),
                    "jokeGenerator");
            }
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
