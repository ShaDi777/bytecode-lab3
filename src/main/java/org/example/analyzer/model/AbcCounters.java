package org.example.analyzer.model;

public final class AbcCounters {

    private long assignments;
    private long branches;
    private long conditions;

    public void incAssignments() {
        assignments++;
    }

    public void incBranches() {
        branches++;
    }

    public void incConditions() {
        conditions++;
    }

    public void add(AbcCounters other) {
        assignments += other.assignments;
        branches += other.branches;
        conditions += other.conditions;
    }

    public long assignments() {
        return assignments;
    }

    public long branches() {
        return branches;
    }

    public long conditions() {
        return conditions;
    }

    public double abcValue() {
        return Math.sqrt(
            (double) assignments * assignments +
                (double) branches * branches +
                (double) conditions * conditions
        );
    }
}
