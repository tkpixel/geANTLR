package org.geantlr.services;

import jakarta.inject.Singleton;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class SymbolTableService {

    private final AtomicReference<Set<String>> currentVariables = new AtomicReference<>(Collections.emptySet());

    // Non-blocking and thread-safe update
    public void updateSymbolTable(ParseTree tree, String[] ruleNames) {
        if (tree != null) {
            SymbolTableVisitor visitor = new SymbolTableVisitor(ruleNames);
            visitor.visit(tree);
            // Atomically update the reference to the newly built unmodifiable set
            currentVariables.set(visitor.getGlobalVariables());
        }
    }

    public List<String> getVariables() {
        return new ArrayList<>(currentVariables.get());
    }
}
