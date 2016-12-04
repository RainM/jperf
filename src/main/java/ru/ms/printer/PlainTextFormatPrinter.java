package ru.ms.printer;

import ru.ms.Profile;
import ru.ms.cfg.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by sergey on 27.11.16.
 */
public abstract class PlainTextFormatPrinter implements ICfgPrinter {

    private static void runProcessor(String processor, String outputFile) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(processor, outputFile);
        Process p = pb.inheritIO().start();
        p.waitFor();
    }

    @Override
    public boolean printCfg(Stream<BasicBlock> bbs, Stream<Edge> edges, File out, Profile.EventProfile profile, Object o) throws Exception {
        try (
                PrintWriter pw = new PrintWriter(new FileOutputStream(out)))
        {

            outputHead(pw);

            bbs.forEach(
                    (x ->
                            outputNode(pw, x))
            );

            edges.forEach(
                    (x ->
                            outputEdge(pw, x))
            );

            outputTail(pw);

        }

        runProcessor(processorName(), out.getAbsolutePath());

        return true;
    }

    protected abstract String processorName();

    protected abstract void outputHead(PrintWriter pw);

    protected abstract void outputTail(PrintWriter pw);

    protected abstract void outputNode(PrintWriter pw, BasicBlock bb);

    protected abstract void outputEdge(PrintWriter pw, Edge e);
}
