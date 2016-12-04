package ru.ms.actors;

import edu.rice.cs.util.ArgumentTokenizer;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Subparser;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import ru.ms.Context;
import ru.ms.cfg.BasicBlock;
import ru.ms.cfg.CfGraph;
import ru.ms.cfg.Edge;
import ru.ms.printer.ICfgPrinter;
import ru.ms.processors.DominatorTree;
import ru.ms.processors.PerfProfileProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created by sergey on 06.11.16.
 */
public class InteractiveActor implements IActor {
    public static void addOptions(ArgumentParser parser) {
        parser.addArgument("--command", "-c").type(String.class).dest("command");
    }

    public static class InteractiveParams {
        @Arg(dest="logFile")
        public String  logFile;

        @Arg(dest="command")
        public String command;
    }

    @Override
    public void _do(Object s) throws Exception {
        InteractiveParams params = (InteractiveParams) s;

        Context ctx = null;
        if (params.logFile != null && !params.logFile.isEmpty()) {
            ctx = Context.initInstance(new File(params.logFile));
            ctx.load();
        }

        DominatorTree<BasicBlock> domTree = null;

        BufferedReader br = null;
        if (params.command != null && !params.command.isEmpty()) {
            br = new BufferedReader(
                    new InputStreamReader(
                            new ByteArrayInputStream(
                                    params.command.getBytes())));
        } else {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        System.out.print("> ");
        String line = br.readLine();
        while (line != null) {

            String[] commands = line.split(";");

            for (String cmd : commands) {
                List<String> args = ArgumentTokenizer.tokenize(cmd);
                String[] argsAr = args.toArray(new String[args.size()]);

                if (args.size() > 0) {
                    String[] parameters = Arrays.copyOfRange(argsAr, 1, argsAr.length);
                    try {
                        switch (args.get(0)) {
                            case "load":
                            case "reload": {
                                String file = args.size() > 1 ? args.get(1) : params.logFile;
                                ctx = Context.initInstance(new File(file));
                                ctx.load();
                                break;
                            }
                            case "list": {
                                if (args.size() > 1) {
                                    switch (args.get(1)) {
                                        case "methods": {
                                            String pattern = args.size() > 2 ? args.get(2) : ".*";
                                            op_list_methods(ctx, pattern);
                                            break;
                                        }
                                        default: {
                                            System.out.println("Unknown list argument. Available: methods");
                                        }
                                    }
                                } else {
                                    System.out.println("No sufficient argument for list command");
                                }
                                break;
                            }
                            case "run": {
                                ArgumentParser parser = ArgumentParsers.newArgumentParser("jperf-run");
                                RunActor.addOptions(parser);
                                RunActor.RunParams runParams = new RunActor.RunParams();


                                parser.parseArgs(parameters, runParams);
                                RunActor.run(runParams);

                                break;
                            }
                            case "load-profile": {
                                PerfProfileProcessor.ProfileStatistic result = PerfProfileProcessor.loadPerfProfile(ctx.getProfile());
                                System.out.println("Read events: " + result.eventsCount);
                                System.out.println("Available counters: " + String.join(", ", result.availableCounters));
                                break;
                            }
                            case "set-event": {
                                if (ctx == null) {
                                    System.out.println("Context isn't set. Try load/reload command");
                                } else {
                                    ctx.setEvent(parameters[0]);
                                }
                                break;
                            }
                            case "set-compilation": {
                                if (ctx == null) {
                                    System.out.println("Context isn't set. Try load/reload command");
                                } else {
                                    int method = Integer.valueOf(parameters[0]);
                                    int compilation = Integer.valueOf(parameters[1]);
                                    ctx.setCompilation(method, compilation);
                                }
                                break;
                            }
                            case "gui": {
                                if (ctx == null) {
                                    System.out.println("Context isn't set. Try load/reload command");
                                } else {
                                    String guiType = parameters[0];
                                    ICfgPrinter.CfgPrinter printerType = ICfgPrinter.CfgPrinter.valueOf(guiType);

                                    printerType.create().printCfg(
                                            ctx.getCurrentGraph().bbs().stream(),
                                            ctx.getCurrentGraph().edges().stream(),
                                            new File("out"),
                                            ctx.getCurrentEventProfile(),
                                            domTree
                                    );
                                }

                                break;
                            }
                            case "analysis-dom":
                            case "analysis-dominators": {
                                CfGraph g = ctx.getCurrentGraph();
                                domTree = new DominatorTree<> (
                                        (x) -> g
                                        .incomingEdgesOf(x)
                                        .stream()
                                        .map(Edge::getFrom)
                                        .filter(BasicBlock::isInScope)
                                        .collect(Collectors.toCollection(CopyOnWriteArrayList::new)),
                                        (x) -> g
                                                .outgoingEdgesOf(x)
                                                .stream()
                                                .map(Edge::getTo)
                                                .filter(BasicBlock::isInScope)
                                                .collect(Collectors.toCollection(CopyOnWriteArrayList::new)),
                                        g.headBb()
                                    );

                                domTree.process(BasicBlock::isInScope);

                                domTree.buildDomTree(g.bbs());

                                break;
                            }
                            case "exit":
                            case "quit": {
                                return;
                            }
                            default: {
                                System.out.println("Unknown command");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }

            System.out.print("> ");
            line = br.readLine();
        }
    }

    private static void op_list_methods(Context ctx, String pattern) {
        List<Context.CompilationInfo> compilations = ctx.getCompilations(pattern);
        for (Context.CompilationInfo cmplInfo : compilations) {
            if (cmplInfo.compilations.size() > 0) {
                System.out.println("[" + cmplInfo.methodId + "] " + cmplInfo.methodName);
                int cmplIdx = 0;
                for (Compilation cmpl : cmplInfo.compilations) {
                    System.out.println(
                            "\t[\t" + cmplIdx + "] " +
                                    "<" + StringUtil.formatTimestamp(cmpl.getQueuedStamp(), true) + "> " +
                                    "<" + StringUtil.formatTimestamp(cmpl.getCompiledStamp(), true) + "> " +
                                    "<" + cmpl.getCompiler() + ">" +
                                    "\t<" + cmpl.getLevel() + ">");
                    cmplIdx += 1;
                }
            }
        }
    }
}
