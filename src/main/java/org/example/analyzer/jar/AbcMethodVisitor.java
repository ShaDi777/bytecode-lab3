package org.example.analyzer.jar;

import org.example.analyzer.model.AbcCounters;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class AbcMethodVisitor extends MethodVisitor {

    private final AbcCounters abc;

    AbcMethodVisitor(AbcCounters abc) {
        super(Opcodes.ASM9);
        this.abc = abc;
    }

    // ===================== A - assignment =====================
    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        if (opcode == Opcodes.ISTORE || opcode == Opcodes.LSTORE || opcode == Opcodes.FSTORE ||
                opcode == Opcodes.DSTORE || opcode == Opcodes.ASTORE) {
            abc.incAssignments();
        }
    }

    // @Override
    // public void visitIincInsn(int varIndex, int increment) {
    //     abc.incAssignments();
    // }

    // ===================== B - branch =====================
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
        abc.incBranches();
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        abc.incBranches();
    }

    // ===================== C - condition =====================
    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (opcode != Opcodes.GOTO && opcode != Opcodes.JSR) {
            abc.incConditions();
        }
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        abc.incConditions();
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        abc.incConditions();
    }
}
