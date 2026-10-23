package net.xlea99.lifeguard.core;

import java.util.List;

/** Every problem the freeze found in the entry declarations, at once, so one crash fixes them all. */
public final class EntryValidationException extends RuntimeException {
    private final List<String> problems;

    public EntryValidationException(List<String> problems) {
        super(problems.size() + " problem(s) in Lifeguard's entry declarations:\n  - " + String.join("\n  - ", problems));
        this.problems = List.copyOf(problems);
    }

    public List<String> problems() {
        return problems;
    }
}
