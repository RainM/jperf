package ru.ms.actors;

import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergey on 23.10.16.
 */
public class RunActor implements IActor {

    public static void addOptions(ArgumentParser run) {
        run.addArgument("-cp").type(String.class).dest("classPath");
        run.addArgument("-jar").type(String.class).dest("jar");
        run.addArgument("--main-class").type(String.class).dest("mainClass");
        run.addArgument("--args").type(String.class).nargs("+").dest("args");
        run.addArgument("--jvm-args").type(String.class).nargs("+").dest("vmArgs");
        run.addArgument("-n", "--dry-run").action(Arguments.storeTrue()).dest("dryRun");
        run.addArgument("--profile").action(Arguments.storeTrue()).dest("profile");
        run.addArgument("--events").type(String.class).nargs("+").dest("events");
    }

    public static class RunParams {

        @Arg(dest="logFile")
        public String  logFile;

        @Arg(dest="classPath")
        public String classPath;

        @Arg(dest="jar")
        public String jar;

        @Arg(dest="mainClass")
        public String mainClass;

        @Arg(dest="args")
        public String[] args;

        @Arg(dest="vmArgs")
        public String[] vmArgs;

        @Arg(dest="dryRun")
        public boolean dryRun;

        @Arg(dest="profile")
        public boolean shouldProfile;

        @Arg(dest="events")
        public String[] events;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                    .append("LogFile", logFile)
                    .append("classPath", classPath)
                    .append("jar", jar)
                    .append("mainClass", mainClass)
                    .append("args", args)
                    .append("vmArgs", vmArgs)
                    .append("profile", shouldProfile)
                    .toString();
        }
    }


    private static String[] trim(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            args[i] = args[i].trim();
        }
        return args;
    }

    private static String[] prepareJavaCmdLine(RunActor.RunParams params) {
        List<String> result = new ArrayList<>();

        if (params.shouldProfile) {
            result.add("perf");

            result.add("record");

            if (params.events != null && params.events.length > 0) {
                result.add("-e");
                result.add(String.join(",", params.events));
            }

            result.add("-o");
            result.add("perf.data");
        }

        result.add("java");

        result.add("-XX:+UnlockDiagnosticVMOptions");
        result.add("-XX:+TraceClassLoading");
        result.add("-XX:+LogCompilation");
        result.add("-XX:+PrintAssembly");

        if (params.logFile != null){
            String outFileCmd = "-XX:LogFile=" + params.logFile;
            result.add(outFileCmd);
        }

        if (params.vmArgs != null) {
            for (String s : trim(params.vmArgs)) {
                result.add(s);
            }
        }

        if (params.classPath != null) {
            result.add("-cp");
            result.add(params.classPath);
        }

        if (params.jar != null) {
            result.add("-jar");
            result.add(params.jar);
        }

        if (params.args != null) {
            for (String s : trim(params.args)) {
                result.add(s);
            }
        }

        return result.toArray(new String[]{});
    }

    public static void run(RunActor.RunParams params) throws IOException, InterruptedException {
        String[] cmd = prepareJavaCmdLine(params);

        if (!params.dryRun) {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            //pb.redirectError(Redirect.INHERIT);
            //pb.redirectOutput(Redirect.INHERIT);
            Process p = pb.start();

            Thread t = new Thread(() -> {
                try {
                    try (BufferedInputStream bis = new BufferedInputStream(p.getErrorStream())) {
                        byte[] buf = new byte[1024];
                        while (bis.read(buf) > 0) ;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t.start();

            try (BufferedInputStream bis = new BufferedInputStream(p.getInputStream())) {
                byte[] buf = new byte[1024];
                while (bis.read(buf) > 0) ;
            }
            t.join();
            p.waitFor();
        } else {
            System.out.println(">> " + String.join(" ", cmd));
        }
    }

    @Override
    public void _do(Object opts) throws Exception {
        RunParams arguments = (RunParams) opts;

        run(arguments);
    }
}
