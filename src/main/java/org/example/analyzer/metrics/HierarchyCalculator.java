package org.example.analyzer.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.example.analyzer.model.ClassInfo;

public final class HierarchyCalculator {

    private final Map<String, ClassInfo> typesByName;

    public HierarchyCalculator(List<ClassInfo> types) {
        this.typesByName = new HashMap<>();
        for (ClassInfo type : types) {
            typesByName.putIfAbsent(type.name(), type);
        }
    }

    public int depthOf(ClassInfo type) {
        int depth = 0;
        Set<String> visited = new HashSet<>();
        String parent = type.superName();
        while (parent != null) {
            ClassInfo parentInfo = typesByName.get(parent);
            if (parentInfo == null || !parentInfo.kind().isClass() || !visited.add(parent)) {
                break;
            }
            depth++;
            parent = parentInfo.superName();
        }
        return depth;
    }
}
