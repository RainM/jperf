package ru.ms.printer;

import ru.ms.Profile;
import ru.ms.cfg.*;
import ru.ms.processors.DominatorTree;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by sergey on 27.11.16.
 */
public class XvcgPrinter extends PlainTextFormatPrinter {

    DominatorTree<BasicBlock> doms = null;

    private static class BbColorizer {

        public enum XcvgColor {
            WHITE(0),
            PINK(28),
            LIGHTRED(17),
            RED(2),
            DARKRED(9);

            private final int color;

            private XcvgColor(int c) {
                color = c;
            }

            public int getXcvgColor() {
                return color;
            }
        }

        private static class ColorStatus {
            public BasicBlock bb;
            public XcvgColor color;
            public long total;
        }

        private final Profile.EventProfile profile;
        private Map<BasicBlock, ColorStatus> colorStatusByBasicBlock;

        private BbColorizer(Profile.EventProfile profile) {
            this.profile = profile;
        }

        private void calculateTotals(Stream<BasicBlock> bbs) {
            ArrayList<ColorStatus> statusByBb = new ArrayList<>();

            bbs.forEach(
                    (bb) -> {
                        long total = bb
                                .listing()
                                .stream()
                                .mapToLong(
                                        (y) -> getCounter(y))
                                .reduce(
                                        (y, z) -> y + z)
                                .orElse(0);

                        ColorStatus status = new ColorStatus();
                        status.bb = bb;
                        status.total = total;

                        statusByBb.add(status);
                    }
            );

            statusByBb.sort((x, y) -> {
                long ctr1 = x.total;
                long ctr2 = y.total;
                if (ctr1 > ctr2) {
                    return 1;
                } else if (ctr1 < ctr2) {
                    return -1;
                }
                return 0;
            });

            int notZeroStartIdx = 0;
            for ( ; notZeroStartIdx < statusByBb.size(); ++notZeroStartIdx) {
                if (statusByBb.get(notZeroStartIdx).total > 0) {
                    break;
                }
                statusByBb.get(notZeroStartIdx).color = XcvgColor.WHITE;
            }

            int groupLen = 1 + ((statusByBb.size() - notZeroStartIdx) / (XcvgColor.values().length - 1));
            int startIdx = notZeroStartIdx;
            int endIdx = startIdx + groupLen;
            XcvgColor[] colors = XcvgColor.values();
            colors = Arrays.copyOfRange(colors, 1, colors.length);
            for (XcvgColor color : colors) {

                for (int i = startIdx; i < Math.min(endIdx, statusByBb.size()); ++i) {
                    statusByBb.get(i).color = color;
                }

                startIdx = endIdx;
                endIdx += groupLen;
            }

            colorStatusByBasicBlock = statusByBb
                    .stream()
                    .collect(
                            Collectors.toMap(
                                    (x) -> x.bb,
                                    (x) -> x
                            )
                    );
        }

        public long getCounter(CfgInstruction i) {
            return profile.getCounter(i.getAddress());
        }

        public long getTotalCounterByBb(BasicBlock bb) {
            return colorStatusByBasicBlock.get(bb).total;
        }

        public XcvgColor getBbColor(BasicBlock bb) {
            return colorStatusByBasicBlock.get(bb).color;
        }
    }

    private BbColorizer bbColorizer;

    @Override
    public boolean printCfg(Stream<BasicBlock> bbs, Stream<Edge> edges, File out, Profile.EventProfile profile, Object param) throws Exception {
        bbColorizer = new BbColorizer(profile);

        bbColorizer.calculateTotals(bbs);

        if (param != null) {
            doms = (DominatorTree<BasicBlock>) param;
        }

        return super.printCfg(bbs, edges, out, profile, param);
    }

    private static class EdgeStyle {
        public final String linestyle;
        public final String color;
        public final int _class;

        private EdgeStyle(String linestyle, String color, int aClass) {
            this.linestyle = linestyle;
            this.color = color;
            _class = aClass;
        }
    }

    private static final EnumMap<Edge.Type, EdgeStyle> styles = fillStyles();

    private static EnumMap<Edge.Type, EdgeStyle> fillStyles() {
        EnumMap<Edge.Type, EdgeStyle> result = new EnumMap<Edge.Type, EdgeStyle>(Edge.Type.class);

        result.put(
                Edge.Type.CALL,
                new EdgeStyle(
                        "dashed",
                        "black",
                        3
                )
        );

        result.put(
                Edge.Type.JUMP,
                new EdgeStyle(
                        "continuous",
                        "red",
                        2
                )
        );

        result.put(
                Edge.Type.THROUGH,
                new EdgeStyle(
                        "continuous",
                        "green",
                        1
                )
        );

        return result;
    }

    @Override
    protected String processorName() {
        return "xvcg";
    }

    @Override
    protected void outputHead(PrintWriter pw) {
        pw.println("graph: {");
        pw.println("title: \"Control Flow\"");
        pw.println("layoutalgorithm: minbackward");
        pw.println("crossingweight: medianbary");
        pw.println("layoutalgorithm: minbackward");
        pw.println("manhatten_edges: yes");
        pw.println("layout_nearfactor: 0");
        pw.println("layout_upfactor: 40");
        pw.println("classname 1 : \"THROUGH\"");
        pw.println("classname 2 : \"JUMP\"");
        pw.println("classname 3 : \"CALL\"");
        pw.println();
    }

    @Override
    protected void outputTail(PrintWriter pw) {
        pw.println("}");
    }

    @Override
    protected void outputNode(PrintWriter pw, BasicBlock bb) {
        pw.printf("node: { title: \"0x%s\" color: %d \n", Long.toHexString(bb.getStartAddr()), bbColorizer.getBbColor(bb).color);
        final CopyOnWriteArrayList<BasicBlock> dominators = doms.getDominators(bb);
        pw.printf(
                "\tlabel: \"Total: %E\\n\\n%s\\nDominators: \\n%s\"\n",
                (float)bbColorizer.getTotalCounterByBb(bb),
                String.join(
                        " \\n ",
                        bb.listing().stream().map(CfgInstruction::formatForOutput).collect(Collectors.toList())
                ),
                String.join(
                        "\\n",
                        dominators == null ? Arrays.asList() : dominators.stream().map((x) -> x.toString()).collect(Collectors.toList())
                )
        );
        pw.printf("}\n");
    }

    @Override
    protected void outputEdge(PrintWriter pw, Edge e) {
        // linestyle: dashed color: red class: 1
        EdgeStyle style = styles.get(e.getType());
        pw.printf(
                "edge: { sourcename: \"0x%s\" targetname: \"0x%s\" linestyle: %s color: %s class: %d}\n",
                Long.toHexString(e.getFrom().getStartAddr()),
                Long.toHexString(e.getTo().getStartAddr()),
                style.linestyle,
                style.color,
                style._class
        );
    }

}
