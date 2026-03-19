package org.geantlr.services;

public record SyntaxError(int line, int charPositionInLine, int length, String message) {
}
