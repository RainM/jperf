package ru.ms.cfg;

import org.jgrapht.graph.DefaultEdge;
import org.omg.CORBA.LongHolder;
import ru.ms.actors.AnalyzeActor;

/**
 * Created by sergey on 09.11.16.
 */
public class Edge extends DefaultEdge {

    public Edge(BasicBlock from, BasicBlock to, Type type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public enum Type {
        THROUGH,
        JUMP,
        CALL;
    }

    public BasicBlock getFrom() {
        return from;
    }

    public BasicBlock getTo() {
        return to;
    }

    public Type getType() {
        return type;
    }

    private final BasicBlock from;
    private final BasicBlock to;
    private final Type type;

    @Override
    protected Object getSource() {
        return getFrom();
    }

    @Override
    protected Object getTarget() {
        return getTo();
    }

    @Override
    public String toString() {
        return "Edge: {" + Long.toHexString(from.getStartAddr()) + "->" + Long.toHexString(to.getStartAddr()) + "[" + type + "]}";
    }
}
