package org.geantlr.services;

import jakarta.inject.Singleton;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CodeFormattingService {

    private static final Logger LOG = LoggerFactory.getLogger(CodeFormattingService.class);

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
            String text = token.getText();
            if (text == null) return;

            int tokenIndex = token.getTokenIndex();

            if (text.equals("}") || text.equals("]")) {
                indentLevel--;
                // Only fix whitespace before closing bracket if it doesn't already match
                String expectedPrefix = "\n" + getIndentString();
                String existingPrefix = getHiddenTextToLeft(tokenIndex);
                if (!existingPrefix.endsWith(expectedPrefix)) {
                    replaceHiddenToLeft(tokenIndex, expectedPrefix);
                }
            }

            if (text.equals("{") || text.equals("[")) {
                indentLevel++;
                // Only fix whitespace after opening bracket if it doesn't already have a newline + correct indent
                String expectedSuffix = "\n" + getIndentString();
                String existingSuffix = getHiddenTextToRight(tokenIndex);
                if (!existingSuffix.startsWith(expectedSuffix)) {
                    replaceHiddenToRight(tokenIndex, expectedSuffix);
                }
            } else if (text.equals(";") || text.equals(",")) {
                // Ensure newline + indent after statement/list separators
                String expectedSuffix = "\n" + getIndentString();
                String existingSuffix = getHiddenTextToRight(tokenIndex);
                if (!existingSuffix.startsWith(expectedSuffix)) {
                    replaceHiddenToRight(tokenIndex, expectedSuffix);
                }
            } else if (!text.equals("}") && !text.equals("]")) {
                // Ensure single space between normal tokens unless followed by punctuation
                Token nextVisibleToken = findNextVisibleToken(tokenIndex);

                if (nextVisibleToken != null && nextVisibleToken.getType() != Token.EOF) {
                    String nextText = nextVisibleToken.getText();
                    if (nextText != null && !nextText.equals(";") && !nextText.equals(",") &&
                        !nextText.equals(".") && !nextText.equals(")") &&
                        !nextText.equals("]") && !nextText.equals("}")) {
                        String existingGap = getHiddenTextToRight(tokenIndex);
                        // Only add a space if there's no whitespace at all between tokens
                        if (existingGap.isEmpty()) {
                            try {
                                rewriter.insertAfter(token, " ");
                            } catch (IllegalStateException e) {
                                LOG.trace("Token already modified", e);
                            }
                        }
                    }
                }
            }
        }

        private Token findNextVisibleToken(int tokenIndex) {
            for (int i = tokenIndex + 1; i < tokenStream.size(); i++) {
                Token t = tokenStream.get(i);
                if (t.getChannel() == Token.DEFAULT_CHANNEL) {
                    return t;
                }
            }
            return null;
        }

        /**
         * Returns the concatenated text of hidden tokens to the left of the given token index.
         */
        private String getHiddenTextToLeft(int tokenIndex) {
            java.util.List<Token> hidden = tokenStream.getHiddenTokensToLeft(tokenIndex);
            if (hidden == null) return "";
            StringBuilder sb = new StringBuilder();
            for (Token h : hidden) {
                if (h.getText() != null) sb.append(h.getText());
            }
            return sb.toString();
        }

        /**
         * Returns the concatenated text of hidden tokens to the right of the given token index.
         */
        private String getHiddenTextToRight(int tokenIndex) {
            java.util.List<Token> hidden = tokenStream.getHiddenTokensToRight(tokenIndex);
            if (hidden == null) return "";
            StringBuilder sb = new StringBuilder();
            for (Token h : hidden) {
                if (h.getText() != null) sb.append(h.getText());
            }
            return sb.toString();
        }

        /**
         * Replaces all hidden whitespace tokens to the left of the given token index with the expected text.
         */
        private void replaceHiddenToLeft(int tokenIndex, String expected) {
            java.util.List<Token> hidden = tokenStream.getHiddenTokensToLeft(tokenIndex);
            if (hidden == null || hidden.isEmpty()) {
                try {
                    rewriter.insertBefore(tokenStream.get(tokenIndex), expected);
                } catch (IllegalStateException e) {
                    LOG.trace("Token already modified", e);
                }
                return;
            }
            boolean first = true;
            for (Token h : hidden) {
                try {
                    if (first) {
                        rewriter.replace(h, expected);
                        first = false;
                    } else {
                        rewriter.delete(h);
                    }
                } catch (IllegalStateException e) {
                    LOG.trace("Token already modified", e);
                }
            }
        }

        /**
         * Replaces all hidden whitespace tokens to the right of the given token index with the expected text.
         */
        private void replaceHiddenToRight(int tokenIndex, String expected) {
            java.util.List<Token> hidden = tokenStream.getHiddenTokensToRight(tokenIndex);
            if (hidden == null || hidden.isEmpty()) {
                try {
                    rewriter.insertAfter(tokenStream.get(tokenIndex), expected);
                } catch (IllegalStateException e) {
                    LOG.trace("Token already modified", e);
                }
                return;
            }
            boolean first = true;
            for (Token h : hidden) {
                try {
                    if (first) {
                        rewriter.replace(h, expected);
                        first = false;
                    } else {
                        rewriter.delete(h);
                    }
                } catch (IllegalStateException e) {
                    LOG.trace("Token already modified", e);
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
