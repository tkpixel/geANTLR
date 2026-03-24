package org.geantlr.services;

import org.antlr.v4.runtime.InterpreterRuleContext;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SymbolTableVisitor extends AbstractParseTreeVisitor<Void> {

    private final Set<String> globalVariables = new HashSet<>();
    private final String[] ruleNames;

    public SymbolTableVisitor(String[] ruleNames) {
        this.ruleNames = ruleNames;
    }

    @Override
    public Void visit(ParseTree tree) {
        if (tree == null) {
            return null;
        }

        if (tree instanceof InterpreterRuleContext) {
            InterpreterRuleContext ctx = (InterpreterRuleContext) tree;
            int ruleIndex = ctx.getRuleIndex();

            if (ruleNames != null && ruleIndex >= 0 && ruleIndex < ruleNames.length) {
                String ruleName = ruleNames[ruleIndex];
                if (ruleName != null) {
                    String lowerRuleName = ruleName.toLowerCase();
                    if (lowerRuleName.contains("variabledeclaration") ||
                        lowerRuleName.contains("vardecl") ||
                        lowerRuleName.contains("declaration")) {
                        extractVariableName(tree);
                    }
                }
            }
        }

        for (int i = 0; i < tree.getChildCount(); i++) {
             visit(tree.getChild(i));
        }

        return null;
    }

    private void extractVariableName(ParseTree tree) {
        for (int i = 0; i < tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            if (child instanceof TerminalNode) {
                String text = child.getText();
                // Simple heuristic: if it looks like an identifier and is not a keyword
                if (text.matches("^[a-zA-Z_][a-zA-Z0-9_]*$") && !isKeyword(text)) {
                    globalVariables.add(text);
                    return; // Only take the first identifier in a declaration for simplicity
                }
            } else {
                extractVariableName(child);
            }
        }
    }

    private boolean isKeyword(String text) {
        return text.matches("^(int|float|double|string|boolean|var|let|const|val|def|function|class)$");
    }

    public Set<String> getGlobalVariables() {
        return Collections.unmodifiableSet(globalVariables);
    }
}
