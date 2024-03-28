/* Danny Le
 * Andrew Miller
 */

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2000 Gerwin Klein <lsf@jflex.de>                          *
 * All rights reserved.                                                    *
 *                                                                         *
 * Thanks to Larry Bell and Bob Jamison for suggestions and comments.      *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

%%

%class Lexer
%byaccj
%line
%column

%{

  public Parser   parser;

  public Lexer(java.io.Reader r, Parser parser) {
    this(r);
    this.parser = parser;
  }

  public int getYyline() {
        return yyline;
    }

    public int getYycolumn() {
        return yycolumn;
    }

%}

num          = [0-9]+("."[0-9]+)?
identifier   = [a-zA-Z][a-zA-Z0-9_]*
newline      = \n
whitespace   = [ \t\r]+
linecomment  = "//".*
blockcomment = "/*"[^]*"*/"


%%

"func"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.FUNC       ; }
"var"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.VAR        ; }
"return"                            { parser.yylval = new ParserVal((Object)yytext()); return Parser.RETURN     ; }
"print"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.PRINT      ; }
"if"                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.IF         ; }
"then"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.THEN       ; }
"else"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.ELSE       ; }
"while"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.WHILE      ; }
"num"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.NUM        ; }
"begin"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.BEGIN      ; }
"end"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.END        ; }
"void"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.VOID       ; }
"bool"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.BOOL       ; }
"new"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.NEW        ; }
"size"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.SIZE       ; }
"("                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.LPAREN     ; }
")"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.RPAREN     ; }
"["                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.LBRACKET   ; }
"]"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.RBRACKET   ; }
"="                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.ASSIGN     ; }
"+"|"-"|"or"                        { parser.yylval = new ParserVal((Object)yytext()); return Parser.EXPROP     ; }
"*"|"/"|"and"                       { parser.yylval = new ParserVal((Object)yytext()); return Parser.TERMOP     ; }
"::"                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.TYPEOF     ; }
";"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.SEMI       ; }
","                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.COMMA      ; }
"."                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.DOT        ; }
":="                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.ASSIGN     ; }
"<"|">"|"<="|"<="|"="|"<>"          { parser.yylval = new ParserVal((Object)yytext()); return Parser.RELOP      ; }
"true"|"false"                      { parser.yylval = new ParserVal((Object)yytext()); return Parser.BOOL_LIT   ; }
{num}                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.NUM_LIT    ; }
{identifier}                        { parser.yylval = new ParserVal((Object)yytext()); return Parser.IDENT      ; }
{linecomment}                       { /* skip */ }
{newline}                           { /* skip */ }
{whitespace}                        { /* skip */ }
{blockcomment}                      { /* skip */ }


\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { System.err.println("Error: unexpected character '"+yytext()+"'"); return Parser.LEXERROR; }
