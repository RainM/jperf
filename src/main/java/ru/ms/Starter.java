package ru.ms;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import ru.ms.actors.*;

import java.util.function.Consumer;

/**
 * Created by Sergey Melnikov on 22.10.16.
 */
public class Starter {

    private enum Action {
        RUN(
                RunActor.class,
                RunActor.RunParams.class,
                RunActor::addOptions),
        LIST(
                ListActor.class,
                ListActor.ListParams.class,
                ListActor::addOptions),
        INTERACTIVE(
                InteractiveActor.class,
                InteractiveActor.InteractiveParams.class,
                InteractiveActor::addOptions),
        ANALYZE(
                AnalyzeActor.class,
                AnalyzeActor.AnalyzeParams.class,
                AnalyzeActor::addOptions);

        private Action(Class c, Class p, Consumer<Subparser> subparserUpdater) {
            actorClazz = c;
            paramsClazz = p;
            this.subparserUpdater = subparserUpdater;
        }

        private final Class actorClazz;
        private final Class paramsClazz;
        private final Consumer<Subparser> subparserUpdater;

        public void addOptions(Subparser sp) {
            subparserUpdater.accept(sp);
        }

        public IActor create() throws Exception {
            return (IActor) actorClazz.getConstructor().newInstance();
        }

        public Object createParams() throws Exception {
            return paramsClazz.getConstructor().newInstance();
        }
    }

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("jperf").defaultHelp(true).description("description...");
        parser.addArgument("--log").type(String.class).dest("logFile");

        Subparsers subparsers = parser.addSubparsers().title("Available commands");

        for (Action act : Action.values()) {
            Subparser subparser = subparsers
                    .addParser(act.name())
                    .setDefault("optsObj", act.createParams())
                    .setDefault("actorObj", act.create());
            act.addOptions(subparser);
        }

        try {
            //CmdParams opts = new CmdParams();
            //parser.parseArgs(args, opts);
            //Namespace opts = parser.parseKnownArgs(args, new ArrayList());
            Namespace opts = parser.parseArgs(args);
            //System.out.println("Args: " + opts);

            IActor actor = (IActor)opts.get("actorObj");

            Object o = opts.get("optsObj");
            parser.parseArgs(args, o);

            //actor.setArgs(o);
            actor._do(o);

        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
    }
}
