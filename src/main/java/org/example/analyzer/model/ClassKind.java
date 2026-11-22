package org.example.analyzer.model;

import org.objectweb.asm.Opcodes;

public enum ClassKind {
    CLASS, INTERFACE, ANNOTATION, ENUM, RECORD;

    public boolean isClass() {
        return this == CLASS || this == ENUM || this == RECORD;
    }

    public static ClassKind fromAccess(int access) {
        if ((access & Opcodes.ACC_INTERFACE) != 0) {
            return (access & Opcodes.ACC_ANNOTATION) != 0 ? ANNOTATION : INTERFACE;
        }
        if ((access & Opcodes.ACC_ENUM) != 0) {
            return ENUM;
        }
        if ((access & Opcodes.ACC_RECORD) != 0) {
            return RECORD;
        }
        return CLASS;
    }
}
