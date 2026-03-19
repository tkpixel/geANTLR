package org.geantlr.services;

import org.antlr.v4.runtime.Token;
import java.util.List;

public record ParseResult(List<Token> tokens, List<SyntaxError> errors) {
}
