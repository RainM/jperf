package ru.ms.cfg;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyLabels;

import java.util.Collections;
import java.util.TreeMap;

/**
 * Created by sergey on 09.11.16.
 */
public class CfgInstruction {
    private final AssemblyInstruction instruction;

    private TreeMap<String, Long> counters = new TreeMap<>();

    public AssemblyInstruction getInstruction() {
        return instruction;
    }

    public CfgInstruction(AssemblyInstruction instruction) {
        this.instruction = instruction;
    }

    public long getAddress() {
        return instruction.getAddress();
    }

    public String getMnemonic() {
        return instruction.getMnemonic();
    }

    public long getTargetAddr() {
        String s = instruction.getOperands().get(0);
        //instruction.
        //BigInteger bi = new BigInteger(s, 16);
        long result;
        try {
            result = Long.decode(s);
        } catch (NumberFormatException nfe) {
            result = -1;
        }
        return result;
    }

    @Override
    public String toString() {
        return instruction.toString();
    }

    public CallTarget getTarget() {
        if (!isJump(this) && !isCall(this)) {
            return null;
        }
        long targetAddr = getTargetAddr();
        if (targetAddr == -1) {
            // may be something like call *%rax
            return null;
        }
        return new CallTarget(null, targetAddr, false);
    }

    public enum JumpType {
        CONDITIONAL,
        UNCONDITIONAL,
        CALL;
    }

    public static boolean isLastBbInstruction(CfgInstruction inst) {
        return isJump(inst) || isCall(inst) || isRet(inst);
    }

    public static boolean isJump(CfgInstruction inst) {
        return inst.getMnemonic().charAt(0) == 'j';
    }

    public static boolean isConditionalJump(CfgInstruction inst) {
        String mnemonic = inst.getMnemonic();
        return !mnemonic.equals("jmpq") && mnemonic.charAt(0) == 'j';
    }

    public static boolean isUnconditionalJump(CfgInstruction inst) {
        String mnemonic = inst.getMnemonic();
        return mnemonic.equals("jmpq");
    }

    public static boolean isCall(CfgInstruction inst) {
        return inst.getMnemonic().equals("callq");
    }

    public static boolean isRet(CfgInstruction inst) {
        return inst.getMnemonic().equals("retq");
    }


    public String formatForOutput() {
        return String.format(
                "0x%s %s %s",
                Long.toHexString(getAddress()),
                instruction.getMnemonic(),
                String.join(", ", instruction.getOperands())
                );
    }

    public static class OutsideCallTarget extends CfgInstruction {

        public OutsideCallTarget(long addr) {
            super(new AssemblyInstruction(
                    "",
                    addr,
                    Collections.EMPTY_LIST,
                    "target",
                    Collections.EMPTY_LIST,
                    "",
                    new AssemblyLabels()
            ));
        }
    }

    public static class CallTarget {
        private final String targetStr;
        private final long targetAddr;
        private final boolean isSymbolic;

        public CallTarget(String targetStr, long targetAddr, boolean isSymbolic) {
            this.targetStr = targetStr;
            this.targetAddr = targetAddr;
            this.isSymbolic = isSymbolic;
        }

        public boolean isSymbolic() {
            return isSymbolic;
        }

        public String targetStr() {
            return targetStr;
        }

        public long target() {
            return targetAddr;
        }
    }
}
