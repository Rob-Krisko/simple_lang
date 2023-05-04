// lexer
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleLangLexer {

    public List<Token> lex(String input) {
        List<Token> tokens = new ArrayList<>();
        int index = 0;

        while (index < input.length()) {
            boolean matched = false;

            for (TokenType type : TokenType.values()) {
                Matcher matcher = type.pattern.matcher(input.substring(index));
                if (matcher.find() && matcher.start() == 0) {
                    String lexeme = matcher.group().trim();

                    if (type != TokenType.WHITESPACE) {
                        tokens.add(new Token(lexeme, type));
                    }

                    index += matcher.end();
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                throw new RuntimeException("Unexpected character: " + input.charAt(index));
            }
        }

        return tokens;
    }

    public enum TokenType {
        REAL_LITERAL("\\d+\\.\\d+"),
        NATURAL_LITERAL("\\d+"),
        BOOL_LITERAL("true|false"),
        CHAR_LITERAL("'[^']'"),
        STRING_LITERAL("\"[^\"]*\""),
        KEYWORD("var|if|else|function|return"),
        PLUS("\\+"),
        MINUS("-"),
        MULTIPLY("\\*"),
        DIVIDE("/"),
        EXPONENT("\\^"),
        EQUALS("=="),
        NOT_EQUALS("!="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_EQUAL(">="),
        LESS_EQUAL("<="),
        AND("&&"),
        OR("\\|\\|"),
        NOT("!"),
        LEFT_PAREN("\\("),
        RIGHT_PAREN("\\)"),
        LEFT_BRACE("\\{"),
        RIGHT_BRACE("\\}"),
        LEFT_BRACKET("\\["),
        RIGHT_BRACKET("\\]"),
        SEMICOLON(";"),
        COMMA(","),
        ASSIGN("="),
        IDENTIFIER("[a-zA-Z_]\\w*"),
        ARRAY_LITERAL("\\[[^\\]]*\\]"),
        SINGLE_LINE_COMMENT("//[^\n]*"),
        MULTI_LINE_COMMENT("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/"),
        WHITESPACE("\\s+"),
        EOF("<<EOF>>"); 

        public final Pattern pattern;

        TokenType(String regex) {
            pattern = Pattern.compile("^" + regex);
        }
    }

    public static class Token {
        private String lexeme;
        private TokenType type;

        public Token(String lexeme, TokenType type) {
            this.lexeme = lexeme;
            this.type = type;
        }

        public String getLexeme() {
            return lexeme;
        }

        public TokenType getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Token{" +
                    "lexeme='" + lexeme + '\'' +
                    ", type=" + type +
                    '}';
        }
    }
}
