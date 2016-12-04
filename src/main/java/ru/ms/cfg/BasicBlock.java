package ru.ms.cfg;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyLabels;
import ru.ms.actors.AnalyzeActor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by sergey on 09.11.16.
 */
public class BasicBlock implements Comparable<BasicBlock> {
    protected final long startAddr;
    private long length;

    private final List<CfgInstruction> instructions;

    public BasicBlock(List<CfgInstruction> instrs, long nextAddr) {
        instructions = instrs;
        startAddr = instrs.get(0).getAddress();
        length = nextAddr > 0 ? nextAddr - startAddr : 1;
    }

    public CfgInstruction getInstrByAddr(long addr) {
        if (instructions.size() == 0) return null;
        if (addr > lastInstr().getAddress()) return null;
        if (addr < firstInstr().getAddress()) return null;

        for (CfgInstruction instr : instructions) {
            if (instr.getAddress() == addr) {
                return instr;
            }
        }
        return null;
    }


    public long getStartAddr() {
        return startAddr;
    }

    public long getEndAddr() {
        return startAddr + length - 1;
    }

    public long getLength() {
        return length;
    }

    public long getNextBbAddr() {
        return startAddr + length;
    }

    public CfgInstruction firstInstr() {
        return instructions.get(0);
    }

    public CfgInstruction lastInstr() {
        return instructions.get(instructions.size() - 1);
    }

    protected BasicBlock(long startAddr_) {
        startAddr = startAddr_;
        length = 1;
        instructions = null;
    }

    public boolean isInScope() {
        return true;
    }

    public BasicBlock splitAt(long addr) {
        long nextBbEndAddr = getEndAddr() + 1;
        length = addr - startAddr; // set new length

        List<CfgInstruction> instrs = new ArrayList<>();
        for (int i = instructions.size() - 1; i >= 0; --i) {
            CfgInstruction instr = instructions.get(i);
            if (instr.getAddress() >= addr) {
                instrs.add(instr);
                instructions.remove(i);
            } else {
                break;
            }
        }

        Collections.reverse(instrs);
        BasicBlock result = new BasicBlock(instrs, nextBbEndAddr);

        return result;
    }

    @Override
    public int compareTo(BasicBlock basicBlock) {
        return (int) (this.startAddr - basicBlock.startAddr);
    }

    @Override
    public String toString() {
        return "BB: {0x" + Long.toHexString(getStartAddr()) + ":0x" + Long.toHexString(getEndAddr()) + "}";
    }

    public List<CfgInstruction> listing() {
        return instructions;
    }

    public static class FakeBasicBlock extends BasicBlock {
        public FakeBasicBlock(long addr) {
            super(addr + 1);
        }

        @Override
        public List<CfgInstruction> listing () {
            return Collections.emptyList();
        }
    }

    public static class RuntimeCallTarget extends BasicBlock {
        CfgInstruction callTarget;
        public RuntimeCallTarget(long addr) {
            super(addr);
            callTarget = new CfgInstruction( new AssemblyInstruction(
                    "",
                    addr,
                    Collections.emptyList(),
                    "target",
                    Collections.emptyList(),
                    "",
                    new AssemblyLabels()
            ));
        }
        public boolean isInScope() {
            return false;
        }

        @Override
        public List<CfgInstruction> listing () {
            return Arrays.asList(callTarget);
        }
    }
}
