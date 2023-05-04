import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleLangParser {
    private String currentMethod;

    private final List<SimpleLangLexer.Token> tokens;
    private int current = 0;

    public SimpleLangParser(List<SimpleLangLexer.Token> tokens) {
        this.tokens = tokens;
    }

    public Map<String, Object> parse() {
        return program();
    }

    private Map<String, Object> program() {
        String previousMethod = currentMethod;
        currentMethod = "program";
        Map<String, Object> node = new HashMap<>();
        node.put("type", "program");
    
        List<Map<String, Object>> statements = new ArrayList<>();
        while (!isAtEnd()) {
            try {
                Map<String, Object> statementNode = statement();
                if (statementNode != null) {
                    statements.add(statementNode);
                }
            } catch (RuntimeException e) {
                System.err.println("Error while parsing statement:");
                e.printStackTrace();
                break;
            }
        }
    
        node.put("statements", statements);
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> variableDeclaration() {
        String previousMethod = currentMethod;
        currentMethod = "variableDeclaration";
        System.out.println("Entering variableDeclaration()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "variableDeclaration");
        
        if (!match(SimpleLangLexer.TokenType.KEYWORD) || !"var".equals(previous().getLexeme())) {
            throw new RuntimeException("Expected 'var' keyword for variable declaration.");
        }
    
        if (!match(SimpleLangLexer.TokenType.IDENTIFIER)) {
            throw new RuntimeException("Expected variable identifier.");
        }
    
        node.put("identifier", previous().getLexeme());
    
        if (match(SimpleLangLexer.TokenType.ASSIGN)) {
            node.put("value", expression());
        }
    
        if (!match(SimpleLangLexer.TokenType.SEMICOLON)) {
            throw new RuntimeException("Expected ';' after variable declaration.");
        }
    
        System.out.println("Exiting variableDeclaration()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> statement() {
        String previousMethod = currentMethod;
        currentMethod = "statement";
        System.out.println("Entering statement()");
        if (peek().getType() == SimpleLangLexer.TokenType.KEYWORD && "var".equals(peek().getLexeme())) {
            System.out.println("routing to variableDeclaration()");
            currentMethod = previousMethod;
            return variableDeclaration();
        } else if (peek().getType() == SimpleLangLexer.TokenType.KEYWORD && "if".equals(peek().getLexeme())) {
            System.out.println("Routing to ifStatement()");
            currentMethod = previousMethod;
            return ifStatement();
        } else if (peek().getType() == SimpleLangLexer.TokenType.KEYWORD && "while".equals(peek().getLexeme())) {
            System.out.println("Routing to whileStatment()");
            currentMethod = previousMethod;
            return whileStatement();
        } else if (peek().getType() == SimpleLangLexer.TokenType.KEYWORD && "function".equals(peek().getLexeme())) {
            System.out.println("routing to functionDeclaration()");
            currentMethod = previousMethod;
            return functionDeclaration();
        } else if (peek().getType() == SimpleLangLexer.TokenType.IDENTIFIER) {
            if (peek(1) != null && peek(1).getType() == SimpleLangLexer.TokenType.ASSIGN) {
                System.out.println("Routing to assignment()");
                currentMethod = previousMethod;
                return assignment();
            } else if (peek(1) != null && peek(1).getType() == SimpleLangLexer.TokenType.LEFT_PAREN) {
                System.out.println("routing to functionCall()");
                currentMethod = previousMethod;
                return functionCall();
            }
        } else if (peek().getType() == SimpleLangLexer.TokenType.IDENTIFIER && peek(1) != null && peek(1).getType() == SimpleLangLexer.TokenType.ASSIGN) {
            System.out.println("Routing to assignment()");

            return assignment();
        }
        // Add more rules for other statements if the language specification expands.
        currentMethod = previousMethod;
        throw new RuntimeException("Unexpected statement.");
    }
    
    private Map<String, Object> blockStatement() {
        String previousMethod = currentMethod;
        currentMethod = "blockStatement";
        System.out.println("Entering blockStatement()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "block");
    
        consume(SimpleLangLexer.TokenType.LEFT_BRACE, "Expected '{'.");
    
        List<Map<String, Object>> statements = new ArrayList<>();
        while (peek().getType() != SimpleLangLexer.TokenType.RIGHT_BRACE && peek().getType() != SimpleLangLexer.TokenType.EOF) {
            statements.add(statement());
        }
    
        consume(SimpleLangLexer.TokenType.RIGHT_BRACE, "Expected '}'.");
    
        node.put("statements", statements);
        System.out.println("Exiting blockStatement()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> assignment() {
        String previousMethod = currentMethod;
        currentMethod = "assignment";
        System.out.println("Entering assignment()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "assignment");
        node.put("name", consume(SimpleLangLexer.TokenType.IDENTIFIER, "Expected a variable name.").getLexeme());
        consume(SimpleLangLexer.TokenType.ASSIGN, "Expected '='.");
        node.put("value", expression());
        consume(SimpleLangLexer.TokenType.SEMICOLON, "Expected ';'.");
        System.out.println("Exiting assignment()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> ifStatement() {
        String previousMethod = currentMethod;
        currentMethod = "ifStatement";
        System.out.println("Entering ifStatement()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "ifStatement");
    
        if (!match(SimpleLangLexer.TokenType.KEYWORD) || !"if".equals(previous().getLexeme())) {
            throw new RuntimeException("Expected 'if' keyword.");
        }
    
        consume(SimpleLangLexer.TokenType.LEFT_PAREN);
    
        node.put("condition", expression());
    
        consume(SimpleLangLexer.TokenType.RIGHT_PAREN);
        consume(SimpleLangLexer.TokenType.LEFT_BRACE);
    
        List<Map<String, Object>> trueBranch = new ArrayList<>();
        while (!check(SimpleLangLexer.TokenType.RIGHT_BRACE)) {
            trueBranch.add(statement());
        }
        node.put("trueBranch", trueBranch);
    
        consume(SimpleLangLexer.TokenType.RIGHT_BRACE);
    
        if (match(SimpleLangLexer.TokenType.KEYWORD) && "else".equals(previous().getLexeme())) {
            consume(SimpleLangLexer.TokenType.LEFT_BRACE);
    
            List<Map<String, Object>> falseBranch = new ArrayList<>();
            while (!check(SimpleLangLexer.TokenType.RIGHT_BRACE)) {
                falseBranch.add(statement());
            }
            node.put("falseBranch", falseBranch);
    
            consume(SimpleLangLexer.TokenType.RIGHT_BRACE);
        }
        System.out.println("Exiting ifStatement()");
        currentMethod = previousMethod;
        return node;
    }
    
    
    
    private Map<String, Object> whileStatement() {
        String previousMethod = currentMethod;
        currentMethod = "whileStatement";
        System.out.println("Entering whileStatement()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "whileStatement");
    
        if (!match(SimpleLangLexer.TokenType.KEYWORD) || !"while".equals(previous().getLexeme())) {
            throw new RuntimeException("Expected 'while' keyword.");
        }
    
        if (!match(SimpleLangLexer.TokenType.LEFT_PAREN)) {
            throw new RuntimeException("Expected '(' after 'while' keyword.");
        }
    
        node.put("condition", expression());
    
        if (!match(SimpleLangLexer.TokenType.RIGHT_PAREN)) {
            throw new RuntimeException("Expected ')' after condition.");
        }
    
        if (!match(SimpleLangLexer.TokenType.LEFT_BRACE)) {
            throw new RuntimeException("Expected '{' after condition.");
        }
    
        List<Map<String, Object>> body = new ArrayList<>();
        while (!match(SimpleLangLexer.TokenType.RIGHT_BRACE)) {
            body.add(statement());
        }
        node.put("body", body);
    
        System.out.println("Exiting whileStatement()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> forStatement() {
        String previousMethod = currentMethod;
        currentMethod = "forStatement";
        System.out.println("Entering forStatement()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "forStatement");
    
        if (!match(SimpleLangLexer.TokenType.KEYWORD) || !"for".equals(previous().getLexeme())) {
            throw new RuntimeException("Expected 'for' keyword.");
        }
    
        if (!match(SimpleLangLexer.TokenType.LEFT_PAREN)) {
            throw new RuntimeException("Expected '(' after 'for' keyword.");
        }
    
        node.put("initializer", statement());
    
        node.put("condition", expression());
    
        if (!match(SimpleLangLexer.TokenType.SEMICOLON)) {
            throw new RuntimeException("Expected ';' after condition.");
        }
    
        node.put("increment", expression());
    
        if (!match(SimpleLangLexer.TokenType.RIGHT_PAREN)) {
            throw new RuntimeException("Expected ')' after increment.");
        }
    
        if (!match(SimpleLangLexer.TokenType.LEFT_BRACE)) {
            throw new RuntimeException("Expected '{' after increment.");
        }
    
        List<Map<String, Object>> body = new ArrayList<>();
        while (!match(SimpleLangLexer.TokenType.RIGHT_BRACE)) {
            body.add(statement());
        }
        node.put("body", body);
    
        System.out.println("Exiting forStatement()");
        currentMethod = previousMethod;
        return node;
    }

    private Map<String, Object> functionDeclaration() {
        String previousMethod = currentMethod;
        currentMethod = "functionDeclaration";
        System.out.println("Entering functionDeclaration()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "functionDeclaration");
    
        if (!match(SimpleLangLexer.TokenType.KEYWORD) || !"function".equals(previous().getLexeme())) {
            throw new RuntimeException("Expected 'function' keyword.");
        }
    
        if (!match(SimpleLangLexer.TokenType.IDENTIFIER)) {
            throw new RuntimeException("Expected function identifier.");
        }
    
        node.put("name", previous().getLexeme());
    
        if (!match(SimpleLangLexer.TokenType.LEFT_PAREN)) {
            throw new RuntimeException("Expected '(' after function identifier.");
        }
    
        List<String> parameters = new ArrayList<>();
        while (!match(SimpleLangLexer.TokenType.RIGHT_PAREN)) {
            if (!match(SimpleLangLexer.TokenType.IDENTIFIER)) {
                throw new RuntimeException("Expected parameter identifier.");
            }
            parameters.add(previous().getLexeme());
            match(SimpleLangLexer.TokenType.COMMA);
        }
        node.put("parameters", parameters);
    
        if (!match(SimpleLangLexer.TokenType.LEFT_BRACE)) {
            throw new RuntimeException("Expected '{' after parameters.");
        }
    
        List<Map<String, Object>> body = new ArrayList<>();
        while (!match(SimpleLangLexer.TokenType.RIGHT_BRACE)) {
            body.add(statement());
        }
        node.put("body", body);
    
        System.out.println("Exiting functionDeclaration()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> functionCall() {
        String previousMethod = currentMethod;
        currentMethod = "functionCall";
        System.out.println("Entering functionCall()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "functionCall");
    
        if (!match(SimpleLangLexer.TokenType.IDENTIFIER)) {
            throw new RuntimeException("Expected function identifier.");
        }
    
        node.put("name", previous().getLexeme());
    
        if (!match(SimpleLangLexer.TokenType.LEFT_PAREN)) {
            throw new RuntimeException("Expected '(' after function identifier.");
        }
    
        List<Map<String, Object>> arguments = new ArrayList<>();
        while (!match(SimpleLangLexer.TokenType.RIGHT_PAREN)) {
            arguments.add(expression());
            match(SimpleLangLexer.TokenType.COMMA);
        }
        node.put("arguments", arguments);
    
        System.out.println("Exiting functionCall()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> tryCatchStatement() {
        String previousMethod = currentMethod;
        currentMethod = "tryCatchStatement";
        System.out.println("Entering tryCatchStatement()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "tryCatchStatement");
    
        if (!match(SimpleLangLexer.TokenType.KEYWORD) || !"try".equals(previous().getLexeme())) {
            throw new RuntimeException("Expected 'try' keyword.");
        }
    
        if (!match(SimpleLangLexer.TokenType.LEFT_BRACE)) {
            throw new RuntimeException("Expected '{' after 'try' keyword.");
        }
    
        List<Map<String, Object>> tryBlock = new ArrayList<>();
        while (!match(SimpleLangLexer.TokenType.RIGHT_BRACE)) {
            tryBlock.add(statement());
        }
        node.put("tryBlock", tryBlock);
    
        if (!match(SimpleLangLexer.TokenType.KEYWORD) || !"catch".equals(previous().getLexeme())) {
            throw new RuntimeException("Expected 'catch' keyword.");
        }
    
        if (!match(SimpleLangLexer.TokenType.LEFT_BRACE)) {
            throw new RuntimeException("Expected '{' after 'catch' keyword.");
        }
    
        List<Map<String, Object>> catchBlock = new ArrayList<>();
        while (!match(SimpleLangLexer.TokenType.RIGHT_BRACE)) {
            catchBlock.add(statement());
        }
        node.put("catchBlock", catchBlock);
    
        System.out.println("Exiting tryCatchStatement()");
        currentMethod = previousMethod;
        return node;
    }

    private Map<String, Object> expression() {
        String previousMethod = currentMethod;
        currentMethod = "expression";
        System.out.println("Entering expression()");
        // Call the arithmetic method, which handles all arithmetic operations
        currentMethod = previousMethod;
        return arithmetic();
    }
    
    private Map<String, Object> comparison() {
        String previousMethod = currentMethod;
        currentMethod = "comparison";
        System.out.println("Entering comparison()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "comparison");
    
        node.put("left", expression());
    
        if (match(SimpleLangLexer.TokenType.EQUALS, SimpleLangLexer.TokenType.NOT_EQUALS,
                SimpleLangLexer.TokenType.GREATER_THAN, SimpleLangLexer.TokenType.LESS_THAN,
                SimpleLangLexer.TokenType.GREATER_EQUAL, SimpleLangLexer.TokenType.LESS_EQUAL)) {
            node.put("operator", previous().getLexeme());
        } else {
            throw new RuntimeException("Expected a comparison operator.");
        }
    
        node.put("right", expression());
    
        System.out.println("Exiting comparison()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> term() {
        String previousMethod = currentMethod;
        currentMethod = "term";
        System.out.println("Entering term()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "term");
    
        node.put("left", factor());
    
        while (match(SimpleLangLexer.TokenType.MULTIPLY, SimpleLangLexer.TokenType.DIVIDE)) {
            Map<String, Object> operation = new HashMap<>();
            operation.put("operator", previous().getLexeme());
            operation.put("right", factor());
            node.put("operation", operation);
        }
    
        System.out.println("Exiting term()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> factor() {
        String previousMethod = currentMethod;
        currentMethod = "factor";
        System.out.println("Entering factor()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "factor");
    
        if (match(SimpleLangLexer.TokenType.NATURAL_LITERAL, SimpleLangLexer.TokenType.REAL_LITERAL,
                SimpleLangLexer.TokenType.CHAR_LITERAL, SimpleLangLexer.TokenType.STRING_LITERAL,
                SimpleLangLexer.TokenType.BOOL_LITERAL, SimpleLangLexer.TokenType.IDENTIFIER)) {
            node.put("value", previous().getLexeme());
        } else if (match(SimpleLangLexer.TokenType.LEFT_PAREN)) {
            node.put("expression", expression());
            consume(SimpleLangLexer.TokenType.RIGHT_PAREN, "Expected a closing parenthesis.");
        } else if (match(SimpleLangLexer.TokenType.LEFT_BRACKET)) {
            node.put("array", arrayLiteral());
        } else {
            throw new RuntimeException("Expected a value, identifier, or expression in parentheses.");
        }
    
        System.out.println("Exiting factor()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> arrayLiteral() {
        String previousMethod = currentMethod;
        currentMethod = "arrayLiteral";
        System.out.println("Entering arrayLiteral()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "arrayLiteral");
        List<Object> elements = new ArrayList<>();
    
        if (!check(SimpleLangLexer.TokenType.RIGHT_BRACKET)) {
            do {
                elements.add(expression());
            } while (match(SimpleLangLexer.TokenType.COMMA));
        }
    
        consume(SimpleLangLexer.TokenType.RIGHT_BRACKET, "Expected a closing bracket.");
    
        node.put("elements", elements);
        
        System.out.println("Exiting arrayLiteral()");
        currentMethod = previousMethod;
        return node;
    }

    private Map<String, Object> arithmetic() {
        String previousMethod = currentMethod;
        currentMethod = "arithmetic";
        System.out.println("Entering arithmetic()");
        Map<String, Object> node = additive();
    
        while (match(SimpleLangLexer.TokenType.MULTIPLY, SimpleLangLexer.TokenType.DIVIDE)) {
            String operator = previous().getLexeme();
            Map<String, Object> right = additive();
            node = binaryOperationNode(node, operator, right);
        }
    
        System.out.println("Exiting arithmetic()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> additive() {
        String previousMethod = currentMethod;
        currentMethod = "additive";
        System.out.println("Entering additive()");
        Map<String, Object> node = term();
    
        while (match(SimpleLangLexer.TokenType.PLUS, SimpleLangLexer.TokenType.MINUS)) {
            String operator = previous().getLexeme();
            Map<String, Object> right = term();
            node = binaryOperationNode(node, operator, right);
        }
    
        System.out.println("Exiting additive()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> binaryOperationNode(Map<String, Object> left, String operator, Map<String, Object> right) {
        String previousMethod = currentMethod;
        currentMethod = "binaryOperationNode";
        System.out.println("Entering binaryOperationNode()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "binaryOperation");
        node.put("left", left);
        node.put("operator", operator);
        node.put("right", right);
        System.out.println("Exiting binaryOperationNode()");
        currentMethod = previousMethod;
        return node;
    }

    private Map<String, Object> printStatement() {
        String previousMethod = currentMethod;
        currentMethod = "printStatement";
        System.out.println("Entering printStatement()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "printStatement");
    
        if (!match(SimpleLangLexer.TokenType.KEYWORD) || !"print".equals(previous().getLexeme())) {
            throw new RuntimeException("Expected 'print' keyword.");
        }
    
        node.put("value", expression());
    
        if (!match(SimpleLangLexer.TokenType.SEMICOLON)) {
            throw new RuntimeException("Expected ';' after print statement.");
        }
    
        System.out.println("Exiting printStatement()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> returnStatement() {
        String previousMethod = currentMethod;
        currentMethod = "returnStatement";
        System.out.println("Entering returnStatement()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "returnStatement");
    
        if (!match(SimpleLangLexer.TokenType.KEYWORD) || !"return".equals(previous().getLexeme())) {
            throw new RuntimeException("Expected 'return' keyword.");
        }
    
        if (!check(SimpleLangLexer.TokenType.SEMICOLON)) {
            node.put("value", expression());
        }
    
        if (!match(SimpleLangLexer.TokenType.SEMICOLON)) {
            throw new RuntimeException("Expected ';' after return statement.");
        }
    
        System.out.println("Exiting returnStatement()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> breakStatement() {
        String previousMethod = currentMethod;
        currentMethod = "breakStatement";
        System.out.println("Entering breakStatement()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "breakStatement");
    
        if (!match(SimpleLangLexer.TokenType.KEYWORD) || !"break".equals(previous().getLexeme())) {
            throw new RuntimeException("Expected 'break' keyword.");
        }
    
        if (!match(SimpleLangLexer.TokenType.SEMICOLON)) {
            throw new RuntimeException("Expected ';' after break statement.");
        }
    
        System.out.println("Exiting breakStatement()");
        currentMethod = previousMethod;
        return node;
    }
    
    private Map<String, Object> continueStatement() {
        String previousMethod = currentMethod;
        currentMethod = "continueStatement";
        System.out.println("Entering continueStatement()");
        Map<String, Object> node = new HashMap<>();
        node.put("type", "continueStatement");
    
        if (!match(SimpleLangLexer.TokenType.KEYWORD) || !"continue".equals(previous().getLexeme())) {
            throw new RuntimeException("Expected 'continue' keyword.");
        }
    
        if (!match(SimpleLangLexer.TokenType.SEMICOLON)) {
            throw new RuntimeException("Expected ';' after continue statement.");
        }
    
        System.out.println("Exiting continueStatement()");
        currentMethod = previousMethod;
        return node;
    }

    // helper methods
    private SimpleLangLexer.Token peek() {
        if (isAtEnd()) return null;
        return tokens.get(current);
    }

    private SimpleLangLexer.Token peek(int offset) {
        if (offset >= tokens.size()) {
            return null;
        }
        return tokens.get(offset);
    }
    

    private SimpleLangLexer.Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private SimpleLangLexer.Token previous() {
        return tokens.get(current - 1);
    }

    private boolean match(SimpleLangLexer.TokenType... types) {
        for (SimpleLangLexer.TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(SimpleLangLexer.TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

   private SimpleLangLexer.Token consume(SimpleLangLexer.TokenType expected, String errorMessage) {
       if (peek().getType() == expected) {
           return tokens.remove(0);
       } else {
           throw new RuntimeException(errorMessage);
       }
   }

   private SimpleLangLexer.Token consume(SimpleLangLexer.TokenType expected) {
    if (peek().getType() == expected) {
        return tokens.remove(0);
    } else {
        throw new RuntimeException("Expected " + expected + " but found " + peek().getType());
    }
}


   public SimpleLangLexer.Token getCurrentToken() {
        return peek();
    }

    public String getCurrentMethod() {
        return currentMethod;
    }
    
}
