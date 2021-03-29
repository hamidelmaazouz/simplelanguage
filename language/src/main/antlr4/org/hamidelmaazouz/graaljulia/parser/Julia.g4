grammar Julia;

@parser::header
{
// DO NOT MODIFY - generated from Julia.g4 using ANTLR v4 Maven plugin

import java.util.Map;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.RootCallTarget;
import org.hamidelmaazouz.graaljulia.JuliaLanguage;
import org.hamidelmaazouz.graaljulia.parser.JuliaParseError;
}

@lexer::header
{
// DO NOT MODIFY - generated from Julia.g4 using ANTLR v4 Maven plugin
}

@parser::members
{
private JuliaNodeFactory factory;
private Source source;

private static final class BailoutErrorListener extends BaseErrorListener {
    private final Source source;

    BailoutErrorListener(Source source) {
        this.source = source;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throwParseError(source, line, charPositionInLine, (Token) offendingSymbol, msg);
    }
}

public void SemErr(Token token, String message) {
    assert token != null;
    throwParseError(source, token.getLine(), token.getCharPositionInLine(), token, message);
}

private static void throwParseError(Source source, int line, int charPositionInLine, Token token, String message) {
    int col = charPositionInLine + 1;
    String location = "-- line " + line + " col " + col + ": ";
    int length = token == null ? 1 : Math.max(token.getStopIndex() - token.getStartIndex(), 0);
    throw new JuliaParseError(source, line, col, length, String.format("Error(s) parsing script:%n" + location + message));
}

public static Map<String, RootCallTarget> parseJulia(JuliaLanguage language, Source source) {
    JuliaLexer lexer = new JuliaLexer(CharStreams.fromString(source.getCharacters().toString()));
    JuliaParser parser = new JuliaParser(new CommonTokenStream(lexer));
    lexer.removeErrorListeners();
    parser.removeErrorListeners();
    BailoutErrorListener listener = new BailoutErrorListener(source);
    lexer.addErrorListener(listener);
    parser.addErrorListener(listener);
    parser.factory = new JuliaNodeFactory(language, source);
    parser.source = source;
    parser.julia();
    return parser.factory.getAllFunctions();
}
}

// parser

julia
:
(expression TERMINATOR)*
EOF
;

expression
:
literal
|
assignment
;

assignment
:
IDENTIFIER
'='
expression
;

literal
:
IDENTIFIER
|
STRING
|
NUMBER
;

// lexer

IDENTIFIER
:
ID_START ID_CONTINUE*
;

NUMBER
:
DIGIT+
;

STRING
:
SINGLE_DOUBLE_QUOTED_STRING
|
TRIPLE_DOUBLE_QUOTED_STRING
;

COMMENT
:
SINGLE_LINE_COMMENT
|
MULTI_LINE_COMMENT
;

SINGLE_DOUBLE_QUOTED_STRING
:
'"' ~('"' | '\n')* '"'
;

TRIPLE_DOUBLE_QUOTED_STRING
:
'"""' CHAR* '"""'
;

SKIP_ : (WHITE_SPACE | COMMENT) -> skip;

fragment ID_START : [A-Z] | [a-z] | '_' | '$';
fragment ID_CONTINUE : CHAR;
fragment NON_ZERO_DIGIT : [1-9];
fragment DIGIT : [0-9];
fragment SINGLE_LINE_COMMENT: '#' ~[\n]*;
fragment MULTI_LINE_COMMENT : '#=' CHAR* '=#';
fragment WHITE_SPACE : [ \t\n]+;
fragment TERMINATOR : '\n' | ';';
fragment CHAR : .;
