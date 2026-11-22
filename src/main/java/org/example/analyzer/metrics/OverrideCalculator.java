package org.example.analyzer.metrics;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.example.analyzer.model.ClassInfo;
import org.example.analyzer.model.MethodSig;

public final class OverrideCalculator {

    private final Map<String, ClassInfo> typesByName;

    public OverrideCalculator(List<ClassInfo> types) {
        this.typesByName = new HashMap<>();
        for (ClassInfo type : types) {
            typesByName.putIfAbsent(type.name(), type);
        }
    }

    public int overriddenMethodCount(ClassInfo type) {
        Set<MethodSig> overridable = collectOverridableAncestorMethods(type);
        return (int) type.methods().stream()
                         .filter(MethodSig::countsForOverride)
                         .filter(method -> overridable.stream().anyMatch(method::isSame))
                         .count();
    }

    private Set<MethodSig> collectOverridableAncestorMethods(ClassInfo type) {
        Set<MethodSig> result = new HashSet<>();
        Deque<String> interfaceQueue = new ArrayDeque<>(type.interfaces());

        String parent = type.superName();
        while (parent != null) {
            ClassInfo parentInfo = typesByName.get(parent);
            if (parentInfo == null || !parentInfo.kind().isClass()) {
                break;
            }
            addOverridable(parentInfo, result);
            interfaceQueue.addAll(parentInfo.interfaces());
            parent = parentInfo.superName();
        }

        Set<String> seenInterfaces = new HashSet<>();
        while (!interfaceQueue.isEmpty()) {
            String interfaceName = interfaceQueue.pop();
            if (!seenInterfaces.add(interfaceName)) {
                continue;
            }
            ClassInfo interfaceInfo = typesByName.get(interfaceName);
            if (interfaceInfo == null) {
                continue;
            }
            addOverridable(interfaceInfo, result);
            interfaceQueue.addAll(interfaceInfo.interfaces());
        }
        return result;
    }

    private void addOverridable(ClassInfo type, Set<MethodSig> result) {
        type.methods()
            .stream()
            .filter(MethodSig::countsForOverride)
            .forEach(result::add);
    }
}
