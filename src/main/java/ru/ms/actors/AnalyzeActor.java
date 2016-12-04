package ru.ms.actors;

import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.inf.Subparser;
import ru.ms.printer.ICfgPrinter;


/**
 * Created by sergey on 23.10.16.
 */
public class AnalyzeActor implements IActor {

    public static void addOptions(Subparser analyze) {
        analyze.addArgument("--profile-type").choices("operf", "perf", "sep");
        analyze.addArgument("--method").type(Integer.class).dest("methodIdx").required(true);
        analyze.addArgument("--compilation").type(Integer.class).dest("compilationIdx").required(true);
        /*analyze.addArgument("--output-image").type(String.class).dest("outputImage");
        analyze.addArgument("--web").type(Boolean.class).dest("web");
        analyze.addArgument("--web-port").setDefault("8080").dest("webPort");*/
        analyze.addArgument("--output-format").type(ICfgPrinter.CfgPrinter.class).choices(ICfgPrinter.CfgPrinter.values()).dest("outFormat");
        analyze.addArgument("--output", "-o").type(String.class).dest("outputFile");
        analyze.addArgument("--event").type(String.class).dest("event");
        analyze.addArgument("--analyzers").type(String.class).nargs("*").dest("analyzers");
    }

    public static class AnalyzeParams {
        @Arg(dest="logFile")
        public String  logFile;

        @Arg(dest="methodIdx")
        public Integer methodIdx;

        @Arg(dest="compilationIdx")
        public Integer compilationIdx;

        @Arg(dest="outFormat")
        public ICfgPrinter.CfgPrinter printer;

        @Arg(dest="outputFile")
        public String outputFile;

        @Arg(dest="analyzers")
        public String[] analyzers;

        @Arg(dest="event")
        public String event;
    }

    private AnalyzeParams params;

    @Override
    public void _do(Object s) throws Exception {
//        params = (AnalyzeActor.AnalyzeParams)s;
//
//        CfgParser parser = CfgParser.parseLog(
//                new File(params.logFile),
//                new CfgParser.MethodsSelector() {
//                    private int idx = 0;
//                    private CfgParser.MemberCompilation compilationToAnalyze =
//                            new CfgParser.MemberCompilation();
//
//                    @Override
//                    public Collection<CfgParser.MemberCompilation> getMethodsToAnalyze() {
//                        return Arrays.asList(compilationToAnalyze);
//                    }
//
//                    @Override
//                    public void accept(IMetaMember member) {
//                        idx += 1;
//                        if (idx == params.methodIdx) {
//                            compilationToAnalyze.member = member;
//                        }
//                    }
//                }
//        );
//
//        Set<String> methods = parser.methods();
//        CfGraph graph = parser.getCfg(methods.iterator().next(), params.compilationIdx);
//
//        for (String analyzerStr : params.analyzers) {
//            IProcessor a = IProcessor.Type.valueOf(analyzerStr).create(null);
//            graph = a.analyzeAndProcess(graph, null);
//        }
//
//        ICfgPrinter printer = params.printer.create();
//
//        System.out.println("Starting output");
//        printer.printCfg(graph, new File(params.outputFile), graph.getProfile().getEventProfile(params.event));
//        System.out.println("Done");
    }
}
