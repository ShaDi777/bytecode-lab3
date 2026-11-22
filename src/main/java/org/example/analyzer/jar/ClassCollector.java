package org.example.analyzer.jar;

import java.util.ArrayList;
import java.util.List;
import org.example.analyzer.model.AbcCounters;
import org.example.analyzer.model.ClassInfo;
import org.example.analyzer.model.ClassKind;
import org.example.analyzer.model.MethodSig;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class ClassCollector extends ClassVisitor {

    private final List<MethodSig> methods = new ArrayList<>();
    private final AbcCounters abc = new AbcCounters();
    private String name;
    private String superName;
    private ClassKind kind = ClassKind.CLASS;
    private String[] interfaces = new String[0];
    private int fields;
    private ClassInfo resultInfo;

    ClassCollector() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = name;
        this.superName = superName;
        this.kind = ClassKind.fromAccess(access);
        this.interfaces = interfaces == null ? new String[0] : interfaces.clone();
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        fields++;
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        methods.add(new MethodSig(name, desc, access));
        return new AbcMethodVisitor(abc);
    }

    @Override
    public void visitEnd() {
        resultInfo = new ClassInfo(name, kind, superName, List.of(interfaces), List.copyOf(methods), fields, abc);
    }

    ClassInfo getResultInfo() {
        if (resultInfo == null) {
            throw new IllegalStateException("class " + name + " was not fully visited");
        }
        return resultInfo;
    }
}
