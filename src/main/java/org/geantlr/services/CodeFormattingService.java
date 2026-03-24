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

        // Ensure that the tokens we are rewriting are fully buffered before walking
        // tokenStream.fill() was already called, which is good.

        ParseTreeWalker walker = new ParseTreeWalker();
        FormatterListener listener = new FormatterListener(rewriter, tokenStream);
        walker.walk(listener, tree);

        String result = rewriter.getText();

        // As a generic fallback to fix any LLM output that resulted in weird spacing or formatting
        // that the generic ANTLR tree walker missed, ensure no extra blank lines
        if (result != null) {
            result = result.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n");
            result = result.trim();
        } else {
            result = code;
        }
        return result;
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
            int tokenIndex = token.getTokenIndex();

            if (tokenIndex < 0) return;

            java.util.List<Token> hiddenTokens = tokenStream.getHiddenTokensToLeft(tokenIndex);
            if (hiddenTokens != null) {
                for (Token hidden : hiddenTokens) {
                    if (hidden.getType() != Token.EOF && hidden.getText() != null && hidden.getText().trim().isEmpty()) {
                        try {
                            rewriter.delete(hidden);
                        } catch (Exception e) {}
                    }
                }
            }

            String text = token.getText();
            if (text == null) return;

            if (text.equals("}") || text.equals("]")) {
                indentLevel = Math.max(0, indentLevel - 1);
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
                // Determine space formatting for ordinary tokens.
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
                        // Don't insert space after an opening parenthesis
                        if (!text.equals("(")) {
                            rewriter.insertAfter(token, " ");
                        }
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
