package ru.ms.printer;

import ru.ms.cfg.BasicBlock;
import ru.ms.cfg.CfGraph;
import ru.ms.cfg.CfgInstruction;
import ru.ms.cfg.Edge;

import java.io.*;
import java.util.stream.Collectors;

/**
 * Created by sergey on 11.11.16.
 */
public class GraphvizPrinter extends PlainTextFormatPrinter {

    @Override
    protected void outputHead(PrintWriter pw) {
        pw.println("digraph G {");
    }

    @Override
    protected void outputTail(PrintWriter pw) {
        pw.println("}");
    }

    @Override
    protected void outputNode(PrintWriter pw, BasicBlock bb) {
        pw.printf("\"0x%s\" [\n", Long.toHexString(bb.getStartAddr()));
        pw.printf(
                "label=\"%s\"\n",
                String.join(
                        " \\n ",
                        bb.listing().stream().map(CfgInstruction::formatForOutput).collect(Collectors.toList())
                )
        );
        pw.printf("];\n");
    }

    @Override
    protected void outputEdge(PrintWriter pw, Edge e) {
        pw.printf(
                "\"0x%s\" -> \"0x%s\";\n",
                Long.toHexString(e.getFrom().getStartAddr()),
                Long.toHexString(e.getTo().getStartAddr())
                );
    }

    @Override
    protected String processorName() {
        return "dot";
    }
}
