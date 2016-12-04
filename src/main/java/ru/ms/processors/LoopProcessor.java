package ru.ms.processors;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import ru.ms.Context;
import ru.ms.cfg.BasicBlock;
import ru.ms.cfg.CfGraph;
import ru.ms.cfg.Edge;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created by sergey on 27.11.16.
 */
public class LoopProcessor implements IProcessor {

    @Override
    public void transform(Context ctx) {

        CfGraph g = ctx.getCurrentGraph();

        DominatorTree<BasicBlock> dominatorExtractor = new DominatorTree<>(
                (x) -> g
                        .incomingEdgesOf(x)
                        .stream()
                        .map( (e) -> e.getFrom() )
                        .filter( (y) -> y.isInScope() )
                        .collect(Collectors.toCollection(CopyOnWriteArrayList::new)),
                (x) -> g
                        .outgoingEdgesOf(x)
                        .stream()
                        .map( (e) -> e.getTo()   )
                        .filter( (y) -> y.isInScope() )
                        .collect(Collectors.toCollection(CopyOnWriteArrayList::new)),
                g.headBb()
        );

        dominatorExtractor.process(BasicBlock::isInScope);

        Set backEdges = dominatorExtractor.calculateBackEdges(
                g.edges(),
                (e) -> ((Edge)e).getFrom(),
                (e) -> ((Edge)e).getTo()
        );

        System.out.println("---------------------------");
        System.out.println("-----BACKEDGES-------------");
        System.out.println("---------------------------");
        backEdges.stream().forEach(System.out::println);
        System.out.println("---------------------------");
    }

    public static void processArgParser(ArgumentParser parser) {

    }
}
