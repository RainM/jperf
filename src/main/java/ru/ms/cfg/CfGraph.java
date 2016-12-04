package ru.ms.cfg;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyBlock;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import ru.ms.Profile;

import java.util.*;
import java.util.function.Function;

/**
 * Created by sergey on 11.11.16.
 */
public class CfGraph {

    private final DirectedGraph<BasicBlock, Edge> cfg;
    private final BasicBlock head;
    private final NavigableSet<BasicBlock> bbsByAddr;

    public CfGraph(AssemblyBlock ab) {
        GraphWithStart graph = buildGraph(ab);
        cfg = graph.cfg;
        head = graph.startBb;
        bbsByAddr = graph.bbsByAddr;
    }

    public Set<BasicBlock> bbs() {
        return cfg.vertexSet();
    }

    public Set<Edge> edges() {
        return cfg.edgeSet();
    }

    private static class GraphWithStart {
        public final BasicBlock startBb;
        public final DirectedGraph<BasicBlock, Edge> cfg;
        private final NavigableSet<BasicBlock> bbsByAddr;

        private GraphWithStart(BasicBlock startBb, DirectedGraph<BasicBlock, Edge> cfg, NavigableSet<BasicBlock> bbsByAddr) {
            this.startBb = startBb;
            this.cfg = cfg;
            this.bbsByAddr = bbsByAddr;
        }
    }

    private static GraphWithStart buildGraph(AssemblyBlock ab) {


        List<CfgInstruction> instructionsForBb = new ArrayList<>();
        List<AssemblyInstruction> instructions = ab.getInstructions();
        ArrayList<BasicBlock> allBbs = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {
            AssemblyInstruction inst = instructions.get(i);
            CfgInstruction cfgInstruction = new CfgInstruction(inst);
            instructionsForBb.add(cfgInstruction);

            if (CfgInstruction.isLastBbInstruction(cfgInstruction)) {
                long nextAddr = 0;
                if (i < instructions.size() - 1) {
                    nextAddr = instructions.get(i+1).getAddress();
                }

                BasicBlock bb = new BasicBlock(instructionsForBb, nextAddr);
                instructionsForBb = new ArrayList<>();

                allBbs.add(bb);
            }
        }

        NavigableSet<BasicBlock> bbsByAddr = new TreeSet<>();
        bbsByAddr.addAll(allBbs);

        //long startAddr = allBbs.get(0).startAddr;
        BasicBlock firstBlock = allBbs.get(0);

        for (int i = 0; i < allBbs.size(); ++i) {
            BasicBlock bb = allBbs.get(i);

            if (!bb.isInScope()) continue;

            System.out.println("Processing: " + bb);

            CfgInstruction lastInstr = bb.lastInstr();
            //long nextBbAddr = bb.getNextBbAddr();

            CfgInstruction.CallTarget callTarget = lastInstr.getTarget();

            if (callTarget != null) {
                System.out.println("is jump/call");
                long target = callTarget.target();
                BasicBlock targetBb = getBbByAddrOrPrev(bbsByAddr, target);
                System.out.println("TargetBB: " + targetBb);
                if (targetBb == null || targetBb.getEndAddr() < target) {
                    targetBb = new BasicBlock.RuntimeCallTarget(target);
                    allBbs.add(targetBb);
                    bbsByAddr.add(targetBb);
                    System.out.println("NEW TargetBB: " + targetBb);
                }
                if (targetBb.getStartAddr() < target && targetBb.getNextBbAddr() > target) {
                    BasicBlock newBb = targetBb.splitAt(target);
                    bbsByAddr.add(newBb);
                    allBbs.add(newBb);

                    System.out.println("Splitted for: " + targetBb + " and " + newBb);
                }
            } else {
                assert CfgInstruction.isRet(lastInstr);
            }
            System.out.println("-----------------------------------------");
        }

        ArrayList<Edge> allEdges = new ArrayList<>();
        for (int i = 0; i < allBbs.size(); ++i) {
            BasicBlock bb = allBbs.get(i);
            BasicBlock nextBb = getBbByAddrOrPrev(bbsByAddr, bb.getNextBbAddr());

            if (!bb.isInScope()) continue;

            System.out.println("Processing: " + bb);

            CfgInstruction lastInstr = bb.lastInstr();

            if (
                    nextBb != null &&  // there  is next BB
                    nextBb.getStartAddr() != bb.getStartAddr() &&
                    // is it's unconditional jump, the only way is go to target
                    !CfgInstruction.isUnconditionalJump(lastInstr)
                    )
            {
                //Edge.Type edgeType = CfgInstruction.isCall(lastInstr) ? Edge.Type.CALL : Edge.Type.THROUGH;

                Edge throughEdge = new Edge(
                        bb,
                        nextBb,
                        Edge.Type.THROUGH
                );
                allEdges.add(throughEdge);
                System.out.println(throughEdge);
            }

            CfgInstruction.CallTarget callTarget = lastInstr.getTarget();

            if (callTarget != null) {
                BasicBlock target = getBbByAddrOrPrev(bbsByAddr, callTarget.target());
                Edge.Type edgeType = CfgInstruction.isCall(lastInstr) ? Edge.Type.CALL : Edge.Type.JUMP;
                Edge jumpEdge = new Edge(
                        bb,
                        target,
                        edgeType
                );
                allEdges.add(jumpEdge);
                System.out.println(jumpEdge);
            }
            System.out.println("-----------------------------------------");
        }

        DirectedGraph<BasicBlock, Edge> result = new DefaultDirectedGraph<BasicBlock, Edge>(new EdgeFactory<BasicBlock, Edge>() {
            @Override
            public Edge createEdge(BasicBlock sourceVertex, BasicBlock targetVertex) {
                throw new RuntimeException("Should not be called!");
            }
        });

        for (BasicBlock bb : allBbs) {
            result.addVertex(bb);
        }

        for (Edge e : allEdges) {
            result.addEdge(e.getFrom(), e.getTo(), e);
        }

        return new GraphWithStart(firstBlock, result, bbsByAddr);
    }

    private static BasicBlock getBbByAddrOrPrev(NavigableSet<BasicBlock> bbs, long addr) {
        BasicBlock result = bbs.lower(new BasicBlock.FakeBasicBlock(addr));
        return result;
    }

    public BasicBlock getBbByAddrOrPrev(long addr) {
        BasicBlock bb = bbsByAddr.lower(new BasicBlock.FakeBasicBlock(addr));
        if (bb != null && bb.getNextBbAddr() > addr) {
            return bb;
        }
        return null;
    }

    public <T> T processDirectedGraph(Function<DirectedGraph<BasicBlock, Edge>, T> functor) {
        return functor.apply(cfg);
    }

    public Set<Edge> incomingEdgesOf(BasicBlock bb) {
        return cfg.incomingEdgesOf(bb);
    }

    public Set<Edge> outgoingEdgesOf(BasicBlock bb) {
        return cfg.outgoingEdgesOf(bb);
    }

    public BasicBlock headBb() {
        return head;
    }
}
