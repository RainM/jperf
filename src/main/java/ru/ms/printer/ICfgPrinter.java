package ru.ms.printer;

import ru.ms.Profile;
import ru.ms.cfg.BasicBlock;
import ru.ms.cfg.CfGraph;
import ru.ms.cfg.Edge;

import java.io.File;
import java.util.stream.Stream;

/**
 * Created by sergey on 11.11.16.
 */
public interface ICfgPrinter {

    public enum CfgPrinter {
        GRAPHVIZ(GraphvizPrinter.class),
        XVCG(XvcgPrinter.class);

        private final Class printer;

        private CfgPrinter(Class c) {
            printer = c;
        }

        public ICfgPrinter create() throws Exception {
            return (ICfgPrinter) printer.getConstructor().newInstance();
        }
    }

    boolean printCfg(
            Stream<BasicBlock> bbs,
            Stream<Edge> edges,
            File outFile,
            Profile.EventProfile profile,
            Object param) throws Exception;

}
