package ru.ms.actors;

import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * Created by sergey on 23.10.16.
 */
public class ListActor implements IActor {

    public static void addOptions(Subparser list) {
        list.addArgument("--package").type(String.class).dest("packageName");
        list.addArgument("--class").type(String.class).dest("className");
        list.addArgument("--method").type(String.class).dest("method");
    }

    public static class ListParams {
        @Arg(dest="logFile")
        public String  logFile;

        @Arg(dest="packageName")
        public String packageName;

        @Arg(dest="className")
        public String className;

        @Arg(dest="method")
        public String method;
    }

    private ListParams params;

    @Override
    public void _do(Object opts) throws Exception {
        params = (ListParams)opts;

//        CfgParser parser = CfgParser.parseLog(
//                new File(params.logFile),
//                new CfgParser.MethodsSelector() {
//                    private int idx = 0;
//                    @Override
//                    public Collection<CfgParser.MemberCompilation> getMethodsToAnalyze() {
//                        return new ArrayList<CfgParser.MemberCompilation>();
//                    }
//
//                    @Override
//                    public void accept(IMetaMember iMetaMember) {
//                        idx += 1;
//                        if (Utils.checkPackageBelongs(params.packageName, iMetaMember.getMetaClass().getPackage())) {
//                            if (Utils.checkClassBelongs(params.className, iMetaMember.getMetaClass())) {
//                                if (Utils.checkMethodBelongs(params.method, iMetaMember)) {
//                                    if (iMetaMember.isCompiled()) {
//                                        System.out.println("[" + idx + "] " + iMetaMember);
//                                        int cmplIdx = 0;
//                                        for (Compilation cmpl : iMetaMember.getCompilations()) {
//                                            System.out.println(
//                                                    "\t[\t" + cmplIdx + "] " +
//                                                            "<" + StringUtil.formatTimestamp(cmpl.getQueuedStamp(), true) + "> " +
//                                                            "<" + StringUtil.formatTimestamp(cmpl.getCompiledStamp(), true) + "> " +
//                                                            "<" + cmpl.getCompiler() + ">" +
//                                                            "\t<" + cmpl.getLevel() + ">");
//                                            cmplIdx += 1;
//                                        }
//                                    }/* else {
//                                        System.out.println(
//                                                "\t[\t-1] Not compiled");
//                                    }*/
//                                }
//                            }
//                        }
//                    }
//                }
//        );

        /*
        HotSpotLogParser parser = new HotSpotLogParser(new IJITListener() {
            @Override
            public void handleJITEvent(JITEvent event) {
            }

            @Override
            public void handleReadStart() {
            }

            @Override
            public void handleReadComplete() {
            }
            @Override
            public void handleLogEntry(String entry) {
            }

            @Override
            public void handleErrorEntry(String entry) {
            }
        });
        parser.processLogFile(new File(params.logFile), (title, body) -> { System.out.println(title + " -> " + body); });

        JITDataModel model = parser.getModel();

        Utils.listAllMethods(model.getPackageManager(), new Consumer<IMetaMember>() {
            private int idx = 0;
            @Override
            public void accept(IMetaMember iMetaMember) {
                idx += 1;
                if (Utils.checkPackageBelongs(params.packageName, iMetaMember.getMetaClass().getPackage())) {
                    if (Utils.checkClassBelongs(params.className, iMetaMember.getMetaClass())) {
                        if (Utils.checkMethodBelongs(params.method, iMetaMember)) {
                            if (iMetaMember.isCompiled()) {
                                System.out.println("[" + idx + "] " + iMetaMember);
                                int cmplIdx = 0;
                                for (Compilation cmpl : iMetaMember.getCompilations()) {
                                    System.out.println(
                                            "\t[" + cmplIdx + "] " +
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
            }
        });*/
    }
}
