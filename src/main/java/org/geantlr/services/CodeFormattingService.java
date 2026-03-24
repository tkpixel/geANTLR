package org.geantlr.services;

import jakarta.inject.Singleton;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.ParserRuleContext;

@Singleton
public class CodeFormattingService {

    public String formatCode(DynamicGrammar grammar, String code) {
        if (grammar == null || grammar.getParserGrammar() == null || code == null || code.isEmpty()) {
            return code;
        }

        CharStream input = CharStreams.fromString(code);
        LexerInterpreter lexerInterpreter = grammar.createLexerInterpreter(input);
        lexerInterpreter.removeErrorListeners();

        CommonTokenStream tokenStream = new CommonTokenStream(lexerInterpreter);
        tokenStream.fill();

        ParserInterpreter parserInterpreter = grammar.createParserInterpreter(tokenStream);
        parserInterpreter.removeErrorListeners();

        org.antlr.v4.tool.Rule startRule = grammar.getParserGrammar().rules.values().iterator().next();
        ParseTree tree;
        try {
            tree = parserInterpreter.parse(startRule.index);
        } catch (Exception e) {
            return code; // Fallback on parse error
        }

        TokenStreamRewriter rewriter = new TokenStreamRewriter(tokenStream);

        ParseTreeWalker walker = new ParseTreeWalker();
        FormatterListener listener = new FormatterListener(rewriter, tokenStream);
        walker.walk(listener, tree);

        String result = rewriter.getText();

        // As a generic fallback to fix any LLM output that resulted in weird spacing or formatting
        // that the generic ANTLR tree walker missed, ensure no extra blank lines
        result = result.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n");
        return result.trim();
    }

    private static class FormatterListener implements ParseTreeListener {
        private final TokenStreamRewriter rewriter;
        private final CommonTokenStream tokenStream;
        private int indentLevel = 0;
        private boolean needsIndent = false;

        public FormatterListener(TokenStreamRewriter rewriter, CommonTokenStream tokenStream) {
            this.rewriter = rewriter;
            this.tokenStream = tokenStream;
        }

        private String getIndentString() {
            return "    ".repeat(Math.max(0, indentLevel));
        }

        @Override
        public void visitTerminal(TerminalNode node) {
            Token token = node.getSymbol();

            // Remove previous hidden whitespace tokens (keep comments if they are on a different channel or identifiable, though ANTLR handles this differently per grammar; we'll only delete whitespaces if we can identify them)
            // For a robust generic formatter, we should look for tokens that consist only of whitespace
            int tokenIndex = token.getTokenIndex();
            java.util.List<Token> hiddenTokens = tokenStream.getHiddenTokensToLeft(tokenIndex);
            if (hiddenTokens != null) {
                for (Token hidden : hiddenTokens) {
                    if (hidden.getType() != Token.EOF && hidden.getText() != null && hidden.getText().trim().isEmpty()) {
                        // Avoid deleting tokens if they were already rewritten/deleted to avoid IllegalStateException
                        try {
                            rewriter.delete(hidden);
                        } catch (Exception e) {}
                    }
                }
            }

            String text = token.getText();
            if (text == null) return;

            if (text.equals("}") || text.equals("]")) {
                indentLevel--;
                rewriter.insertBefore(token, "\n" + getIndentString());
                needsIndent = false;
            } else if (needsIndent) {
                rewriter.insertBefore(token, getIndentString());
                needsIndent = false;
            }

            if (text.equals("{") || text.equals("[")) {
                indentLevel++;
                rewriter.insertAfter(token, "\n");
                needsIndent = true;
            } else if (text.equals(";") || text.equals(",")) {
                rewriter.insertAfter(token, "\n");
                needsIndent = true;
            } else if (!text.equals("}") && !text.equals("]")) {
                // Ensure spaces between normal tokens unless followed by punctuation
                // Find next non-whitespace hidden token, or next visible token
                Token nextVisibleToken = null;
                for (int i = tokenIndex + 1; i < tokenStream.size(); i++) {
                    Token t = tokenStream.get(i);
                    if (t.getChannel() == Token.DEFAULT_CHANNEL) {
                        nextVisibleToken = t;
                        break;
                    }
                }

                if (nextVisibleToken != null && nextVisibleToken.getType() != Token.EOF) {
                    String nextText = nextVisibleToken.getText();
                    if (nextText != null && !nextText.equals(";") && !nextText.equals(",") &&
                        !nextText.equals(".") && !nextText.equals(")") &&
                        !nextText.equals("]") && !nextText.equals("}")) {
                        rewriter.insertAfter(token, " ");
                    }
                }
            }
        }

        @Override
        public void visitErrorNode(ErrorNode node) {}

        @Override
        public void enterEveryRule(ParserRuleContext ctx) {}

        @Override
        public void exitEveryRule(ParserRuleContext ctx) {}
    }
}
