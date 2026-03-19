package org.geantlr.services;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomErrorListener extends BaseErrorListener {

    private final List<SyntaxError> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {

        int length = 1; // default fallback
        if (offendingSymbol instanceof Token) {
            Token token = (Token) offendingSymbol;
            if (token.getText() != null) {
                length = token.getText().length();
            }
        }

        errors.add(new SyntaxError(line, charPositionInLine, length, msg));
    }

    public List<SyntaxError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
