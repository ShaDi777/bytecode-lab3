package org.example.analyzer.model;

import org.objectweb.asm.Opcodes;

public record MethodSig(String name, String descriptor, int access) {

    public boolean countsForOverride() {
        return !name.startsWith("<") // <init>, <clinit>
            && (access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE)) == 0;
    }

    public boolean isSame(MethodSig other) {
        return this.name().equals(other.name()) && this.descriptor().equals(other.descriptor());
    }
}
