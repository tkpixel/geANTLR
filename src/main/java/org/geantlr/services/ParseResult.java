package org.geantlr.services;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.List;

public record ParseResult(List<Token> tokens, List<SyntaxError> errors, ParseTree tree) {
}
