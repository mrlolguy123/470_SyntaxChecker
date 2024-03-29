// Danny Le
// Andrew Miller

import java.util.List;
import java.util.ArrayList;

public class Parser
{
    // Parser-specific symbols/states
    public static final int ENDMARKER   =  0;
    public static final int LEXERROR    =  1;

    // Keywords
    public static final int FUNC        =  2;
    public static final int VAR         =  3;
    public static final int BEGIN       =  4;
    public static final int END         =  5;
    public static final int RETURN      =  6;
    public static final int PRINT       =  7;
    public static final int IF          =  8;
    public static final int THEN        =  9;
    public static final int ELSE        = 10;
    public static final int WHILE       = 11;
    public static final int VOID        = 12;
    public static final int NUM         = 13;
    public static final int BOOL        = 14;
    public static final int NEW         = 15;
    public static final int SIZE        = 16;

    // Symbols
    public static final int LPAREN      = 17;
    public static final int RPAREN      = 18;
    public static final int LBRACKET    = 19;
    public static final int RBRACKET    = 20;
    public static final int ASSIGN      = 21;
    public static final int TYPEOF      = 22;
    public static final int SEMI        = 23;
    public static final int COMMA       = 24;
    public static final int DOT         = 25;

    // Operators
    public static final int RELOP       = 26;
    public static final int EXPROP      = 27;
    public static final int TERMOP      = 28;

    // Literals
    public static final int BOOL_LIT    = 29;
    public static final int NUM_LIT     = 30;

    // Identifiers
    public static final int IDENT       = 31;


    public class Token
    {
        public int       type;
        public ParserVal attr;
        public Token(int type, ParserVal attr) {
            this.type   = type;
            this.attr   = attr;
        }
    }

    public ParserVal yylval;
    Token _token;
    Lexer _lexer;
    Compiler _compiler;
    public ParseTree.Program _parsetree;
    public String            _errormsg;
    public Parser(java.io.Reader r, Compiler compiler) throws Exception
    {
        _compiler  = compiler;
        _parsetree = null;
        _errormsg  = null;
        _lexer     = new Lexer(r, this);
        _token     = null;                  // _token is initially null
        Advance();                          // make _token to point the first token by calling Advance()
    }

    public void Advance() throws Exception
    {
        int token_type = _lexer.yylex();    // get next/first token from lexer
        if(token_type ==  0)      _token = new Token(ENDMARKER , null);     // if  0 => token is endmarker
        else if(token_type == 1) _token = new Token(LEXERROR  , yylval);   // if 1 => there is a lex error
        else                      _token = new Token(token_type, yylval);   // otherwise, set up _token
    }

    public String Match(int token_type) throws Exception {
        boolean match = (token_type == _token.type);
        String lexeme = "";
        if (_token.attr != null) lexeme = (String) _token.attr.obj;

        if (!match)             {             // if token does not match
            String line = String.valueOf(_lexer.getYyline());
            String col = String.valueOf(_lexer.getYycolumn());
            throw new Exception("\"" + tokenToString(token_type) + "\" is expected instead of \"" + lexeme + "\" at " + line + ":" + col + ".");  // throw exception (indicating parsing error in this assignment)
        }

        if (_token.type != ENDMARKER)    // if token is not endmarker,
            Advance();                  // make token point next token in input by calling Advance()

        return lexeme;
    }

    public String tokenToString(int token_type){
        switch(token_type) {
            case ENDMARKER:
                return "$";
            case LEXERROR:
                return "";
            case FUNC:
                return "func";
            case VAR:
                return "var";
            case BEGIN:
                return "begin";
            case END:
                return "end";
            case RETURN:
                return "return";
            case PRINT:
                return "print";
            case IF:
                return "if";
            case THEN:
                return "then";
            case ELSE:
                return "else";
            case WHILE:
                return "while";
            case VOID:
                return "void";
            case NUM:
                return "num";
            case BOOL:
                return "bool";
            case NEW:
                return "new";
            case SIZE:
                return "size";
            case LPAREN:
                return "(";
            case RPAREN:
                return ")";
            case LBRACKET:
                return "[";
            case RBRACKET:
                return "]";
            case ASSIGN:
                return ":=";
            case TYPEOF:
                return "::";
            case SEMI:
                return ";";
            case COMMA:
                return ",";
            case DOT:
                return ".";
            case RELOP:
                return "<,>,<=,>=,=, or <>";
            case TERMOP:
                return "*,/, or \"and\"";
            case EXPROP:
                return "+,-, or \"or\"";
            case BOOL_LIT:
                return "boolean";
            case NUM_LIT:
                return "number";
            case IDENT:
                return "identifier";
            default:
                return null;
        }
    }

    public int yyparse() throws Exception
    {
        try
        {
            _parsetree = program();
            return 0;
        }
        catch(Exception e)
        {
            _errormsg = e.getMessage();
            return -1;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // program -> decl_list
    // decl_list -> decl_list'
    // decl_list' -> fun_decl decl_list' | ϵ
    // fun_decl -> FUNC IDENT TYPEOF prim_type LPAREN params RPAREN BEGIN local_decls stmt_list END
    // params -> param_list | ϵ
    // param_list -> param param_list'
    // param_list' -> COMMA param param_list' | ϵ
    // param -> IDENT TYPEOF type_spec
    // type_spec -> prim_type type_spec'
    // type_spec' -> LBRACKET RBRACKET | ϵ
    // prim_type -> NUM | BOOL | VOID
    // local_decls -> local_decls'
    // local_decls' -> local_decl local_decls' | ϵ
    // local_decl -> VAR IDENT TYPEOF type_spec SEMI
    // stmt_list -> stmt_list'
    // stmt_list' -> stmt stmt_list' | ϵ
    // stmt -> assign_stmt | print_stmt | return_stmt | if_stmt | while_stmt | compound_stmt
    // assign_stmt -> IDENT ASSIGN expr SEMI
    // print_stmt -> PRINT expr SEMI
    // return_stmt -> RETURN expr SEMI
    // if_stmt -> IF LPAREN expr RPAREN THEN stmt_list ELSE stmt_list END
    // while_stmt -> WHILE LPAREN expr RPAREN BEGIN stmt_list END
    // compound_stmt -> BEGIN local_decls stmt_list END
    // args -> arg_list | ϵ
    // arg_list -> expr arg_list'
    // arg_list' -> COMMA expr arg_list' | ϵ
    // expr -> term expr'
    // expr' -> EXPROP term expr' | RELOP term expr' | ϵ
    // term -> factor term'
    // term' -> TERMOP factor term' | ϵ
    // factor -> IDENT factor' | LPAREN expr RPAREN | NUM_LIT | BOOL_LIT | NEW prim_type LBRACKET expr RBRACKET
    // factor' -> LPAREN args RPAREN | LBRACKET expr RBRACKET | DOT SIZE | ϵ
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public ParseTree.Program program() throws Exception
    {
        //      program -> decl_list
        switch(_token.type)
        {
            case FUNC:
            case ENDMARKER:
                List<ParseTree.FuncDecl> funcs = decl_list();
                String v1 = Match(ENDMARKER);
                return new ParseTree.Program(funcs);
        }
        throw new Exception("No matching production in program at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }
    public List<ParseTree.FuncDecl> decl_list() throws Exception
    {
        //    decl_list -> decl_list'
        switch(_token.type)
        {
            case FUNC:
            case ENDMARKER:
                return decl_list_();
        }
        throw new Exception("No matching production in decl_list at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }
    public List<ParseTree.FuncDecl> decl_list_() throws Exception
    {
        //   decl_list' -> fun_decl decl_list'  |  eps
        switch(_token.type)
        {
            case FUNC:
                ParseTree.FuncDecl       v1 = fun_decl  ();
                List<ParseTree.FuncDecl> v2 = decl_list_();
                v2.add(0, v1);
                return v2;
            case ENDMARKER:
                return new ArrayList<ParseTree.FuncDecl>();
        }
        throw new Exception("No matching production in decl_list' at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }
    public ParseTree.FuncDecl fun_decl() throws Exception
    {
        //     fun_decl -> FUNC IDENT TYPEOF prim_type LPAREN params RPAREN BEGIN local_decls stmt_list END
        switch(_token.type)
        {
            case FUNC:
                String                    v01 = Match(FUNC  );
                String                    v02 = Match(IDENT );
                String                    v03 = Match(TYPEOF);
                ParseTree.PrimType        v04 = prim_type(  );
                String                    v05 = Match(LPAREN);
                List<ParseTree.Param>     v06 = params(     );
                String                    v07 = Match(RPAREN);
                String                    v08 = Match(BEGIN );
                List<ParseTree.LocalDecl> v09 = local_decls();
                List<ParseTree.Stmt>      v10 = stmt_list(  );
                String                    v11 = Match(END   );
                return new ParseTree.FuncDecl(v02, v04, v06, v09, v10);
        }
        throw new Exception("No matching production in fun_decl at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }
    public ParseTree.PrimType prim_type() throws Exception
    {
        //    prim_type -> NUM | BOOL | VOID
        switch(_token.type)
        {
            case NUM:
            {
                String v1 = Match(NUM);
                return new ParseTree.PrimTypeNum();
            }
            case BOOL:
            {
                String v1 = Match(BOOL);
                return new ParseTree.PrimTypeBool();
            }
            case VOID:
            {
                String v1 = Match(VOID);
                return new ParseTree.PrimTypeVoid();
            }
        }
        throw new Exception("No matching production in prim_type at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }
    public List<ParseTree.Param> params() throws Exception
    {
        //       params -> param_list | eps
        switch(_token.type)
        {
            case IDENT:
                return param_list();
            case RPAREN:
                return new ArrayList<ParseTree.Param>();

        }
        throw new Exception("No matching production in params at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public List<ParseTree.Param> param_list() throws Exception
    {
        //   param_list -> param param_list'
        switch(_token.type)
        {
            case IDENT:
                ParseTree.Param v1 = param();
                List<ParseTree.Param> v2 = param_list_();
                v2.add(0, v1);
                return v2;
        }
        throw new Exception("No matching production in param_list at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public List<ParseTree.Param> param_list_() throws Exception
    {
        // param_list' -> COMMA param param_list' | eps
        switch(_token.type)
        {
            case COMMA:
                String v1 = Match(COMMA);
                ParseTree.Param v2 = param();
                List<ParseTree.Param> v3 = param_list_();
                v3.add(0, v2);
                return v3;
            case RPAREN:
                return new ArrayList<ParseTree.Param>();
        }
        throw new Exception("No matching production in param_list' at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public ParseTree.Param param() throws Exception
    {
        //       param -> IDENT TYPEOF type_spec
        switch(_token.type)
        {
            case IDENT:
                String v1 = Match(IDENT);
                String v2 = Match(TYPEOF);
                ParseTree.TypeSpec v3 = type_spec();
                return new ParseTree.Param(v1, v3);
        }
        throw new Exception("No matching production in param at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public ParseTree.TypeSpec type_spec() throws Exception
    {
        //    type_spec -> prim_type type_spec'
        switch(_token.type)
        {
            case NUM:
            case VOID:
            case BOOL: {
                ParseTree.PrimType v1 = prim_type();
                ParseTree.TypeSpec_ v2 = type_spec_();
                return new ParseTree.TypeSpec(v1,v2);
            }
        }
        throw new Exception("No matching production in type_spec at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public ParseTree.TypeSpec_ type_spec_() throws Exception
    {
        // type_spec' -> LBRACKET RBRACKET | eps
        switch(_token.type)
        {
            case LBRACKET:
                String v1 = Match(LBRACKET);
                String v2 = Match(RBRACKET);
                return new ParseTree.TypeSpec_Array();
            case COMMA:
            case RPAREN:
            case SEMI:
                return new ParseTree.TypeSpec_Value();
        }
        throw new Exception("No matching production in type_spec' at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public List<ParseTree.LocalDecl> local_decls() throws Exception
    {
        //  local_decls -> local_decls'
        switch(_token.type)
        {
            case VAR:
            case FUNC:
            case IDENT:
            case PRINT:
            case RETURN:
            case IF:
            case WHILE:
            case BEGIN:
            case END:
            case ELSE:
            case ENDMARKER:
                return local_decls_();
        }
        throw new Exception("No matching production in local_decls at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }
    public List<ParseTree.LocalDecl> local_decls_() throws Exception
    {
        // local_decls' -> local_decl local_decls' | eps
        switch(_token.type)
        {
            case VAR:
            {
                ParseTree.LocalDecl v1 = local_decl();
                List<ParseTree.LocalDecl> v2 = local_decls_();
                v2.add(0, v1);
                return v2;

            }
            case FUNC:
            case IDENT:
            case BEGIN:
            case END:
            case PRINT:
            case RETURN:
            case IF:
            case WHILE:
            case ELSE:
            case ENDMARKER:
                return new ArrayList<ParseTree.LocalDecl>();
        }
        throw new Exception("No matching production in local_decls' at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public ParseTree.LocalDecl local_decl() throws Exception
    {
        // local_decl -> VAR IDENT TYPEOF type_spec SEMI
        switch(_token.type)
        {
            case VAR:
                String v1 = Match(VAR);
                String v2 = Match(IDENT);
                String v3 = Match(TYPEOF);
                ParseTree.TypeSpec v4 = type_spec();
                String v5 = Match(SEMI);
                return new ParseTree.LocalDecl(v2, v4);
        }
        throw new Exception("No matching production in local_decl at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }
    public List<ParseTree.Stmt> stmt_list() throws Exception
    {
        //    stmt_list -> stmt_list'
        switch(_token.type)
        {
            case IDENT:
            case PRINT:
            case RETURN:
            case ELSE:
            case IF:
            case WHILE:
            case BEGIN:
            case END:
                return stmt_list_();
        }
        throw new Exception("No matching production in stmt_list at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }
    public List<ParseTree.Stmt> stmt_list_() throws Exception
    {
        //   stmt_list' -> stmt stmt_list_ | eps
        switch(_token.type)
        {
            case IDENT:
            case PRINT:
            case RETURN:
            case IF:
            case WHILE:
            case BEGIN:
                ParseTree.Stmt v1 = stmt();
                List<ParseTree.Stmt> v2 = stmt_list_();
                v2.add(0, v1);
                return v2;
            case END:
            case ELSE:
                return new ArrayList<ParseTree.Stmt>();
        }
        throw new Exception("No matching production in stmt_list' at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public ParseTree.Stmt stmt() throws Exception
    {
        //        stmt -> assign_stmt | print_stmt | return_stmt | if_stmt | while_stmt | compound_stmt
        switch(_token.type)
        {
            case IDENT:
                return assign_stmt();
            case PRINT:
                return print_stmt();
            case RETURN:
                return return_stmt();
            case IF:
                return if_stmt();
            case WHILE:
                return while_stmt();
            case BEGIN:
                return compound_stmt();

        }
        throw new Exception("No matching production in stmt at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    private ParseTree.Stmt assign_stmt() throws Exception {
        // assign_stmt -> IDENT ASSIGN expr SEMI
        switch(_token.type)
        {
            case IDENT:
                String v1 = Match(IDENT);
                String v2 = Match(ASSIGN);
                ParseTree.Expr v3 = expr();
                String v4 = Match(SEMI);
                return new ParseTree.StmtAssign(v1, v3);
        }
        throw new Exception("No matching production in assign_stmt at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    private ParseTree.Stmt print_stmt() throws Exception {
        // print_stmt -> PRINT expr SEMI
        switch(_token.type)
        {
            case PRINT:
                String v1 = Match(PRINT);
                ParseTree.Expr v2 = expr();
                String v3 = Match(SEMI);
                return new ParseTree.StmtPrint(v2);
        }
        throw new Exception("No matching production in print_stmt at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    private ParseTree.Stmt return_stmt() throws Exception {
        // return_stmt -> RETURN expr SEMI
        switch(_token.type)
        {
            case RETURN:
                String v1 = Match(RETURN);
                ParseTree.Expr v2 = expr();
                String v3 = Match(SEMI);
                return new ParseTree.StmtReturn(v2);
        }
        throw new Exception("No matching production in return_stmt at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    private ParseTree.Stmt if_stmt() throws Exception {
        // if_stmt -> IF LPAREN expr RPAREN THEN stmt_list ELSE stmt_list END
        switch(_token.type)
        {
            case IF:
                String v1 = Match(IF);
                String v2 = Match(LPAREN);
                ParseTree.Expr v3 = expr();
                String v4 = Match(RPAREN);
                String v5 = Match(THEN);
                List<ParseTree.Stmt> v6 = stmt_list();
                String v7 = Match(ELSE);
                List<ParseTree.Stmt> v8 = stmt_list();
                String v9 = Match(END);
                return new ParseTree.StmtIf(v3, v6, v8);
        }
        throw new Exception("No matching production in if_stmt at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    private ParseTree.Stmt while_stmt() throws Exception {
        // while_stmt -> WHILE LPAREN expr RPAREN BEGIN stmt_list END
        switch(_token.type)
        {
            case WHILE:
                String v1 = Match(WHILE);
                String v2 = Match(LPAREN);
                ParseTree.Expr v3 = expr();
                String v4 = Match(RPAREN);
                String v5 = Match(BEGIN);
                List<ParseTree.Stmt> v6 = stmt_list();
                String v7 = Match(END);
                return new ParseTree.StmtWhile(v3, v6);
        }
        throw new Exception("No matching production in while_stmt at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    private ParseTree.Stmt compound_stmt() throws Exception {
        // compound_stmt -> BEGIN local_decls stmt_list END
        switch(_token.type)
        {
            case BEGIN:
                String v1 = Match(BEGIN);
                List<ParseTree.LocalDecl> v2 = local_decls();
                List<ParseTree.Stmt> v3 = stmt_list();
                String v4 = Match(END);
                return new ParseTree.StmtCompound(v2, v3);
        }
        throw new Exception("No matching production in compound_stmt at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public ParseTree.Expr expr() throws Exception
    {
        // expr -> term expr'
        switch(_token.type)
        {
            case IDENT:
            case LPAREN:
            case NUM_LIT:
            case BOOL_LIT:
            case NEW:
                ParseTree.Term v1 = term();
                ParseTree.Expr_ v2 = expr_();
                return new ParseTree.Expr(v1, v2);
        }
        throw new Exception("No matching production in expr at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public ParseTree.Expr_ expr_() throws Exception
    {
        //  expr' -> EXPROP term expr' | RELOP term expr' | eps
        switch(_token.type)
        {
            case EXPROP:
                String v01 = Match(EXPROP);
                ParseTree.Term v02 = term();
                ParseTree.Expr_ v03 = expr_();
                return new ParseTree.Expr_(v01,v02,v03);
            case RELOP:
                String v04 = Match(RELOP);
                ParseTree.Term v05 = term();
                ParseTree.Expr_ v06 = expr_();
                return new ParseTree.Expr_(v04,v05,v06);
            case COMMA:
            case RBRACKET:
            case RPAREN:
            case SEMI:
                return null;
        }
        throw new Exception("No matching production in expr at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public ParseTree.Term term() throws Exception
    {
        // term -> factor term'
        switch(_token.type)
        {
            case IDENT:
            case LPAREN:
            case NUM_LIT:
            case BOOL_LIT:
            case NEW:
                ParseTree.Factor v1 = factor();
                ParseTree.Term_ v2 = term_();
                return new ParseTree.Term(v1, v2);
        }
        throw new Exception("No matching production in term at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public ParseTree.Term_ term_() throws Exception
    {
        // term' -> TERMOP factor term' | eps
        switch(_token.type)
        {
            case TERMOP:
                String v01 = Match(TERMOP);
                ParseTree.Factor v02 = factor();
                ParseTree.Term_ v03 = term_();
                return new ParseTree.Term_(v01,v02,v03);
            case EXPROP:
            case RELOP:
            case COMMA:
            case RBRACKET:
            case RPAREN:
            case SEMI:
                return null;
        }
        throw new Exception("No matching production in term' at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public ParseTree.Factor factor() throws Exception
    {
        // factor -> IDENT factor' | LPAREN expr RPAREN | NUM_LIT | BOOL_LIT | NEW prim_type LBRACKET expr RBRACKET
        switch(_token.type)
        {
            case IDENT:
                String v1 = Match(IDENT);
                ParseTree.Factor_ v2 = factor_();
                return new ParseTree.FactorIdentExt(v1, v2);
            case LPAREN:
                String v3 = Match(LPAREN);
                ParseTree.Expr v4 = expr();
                String v5 = Match(RPAREN);
                return new ParseTree.FactorParen(v4);
            case NUM_LIT:
                double v6 = Double.parseDouble(Match(NUM_LIT));
                return new ParseTree.FactorNumLit(v6);
            case BOOL_LIT:
                boolean v7 = Boolean.parseBoolean(Match(BOOL_LIT));
                return new ParseTree.FactorBoolLit(v7);
            case NEW:
                String v8 = Match(NEW);
                ParseTree.PrimType v9 = prim_type();
                String v10 = Match(LBRACKET);
                ParseTree.Expr v11 = expr();
                String v12 = Match(RBRACKET);
                return new ParseTree.FactorNew(v9, v11);

        }
        throw new Exception("No matching production in factor at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    private ParseTree.Factor_ factor_() throws Exception {
        // factor' -> LPAREN args RPAREN | LBRACKET expr RBRACKET | DOT SIZE | eps
        switch(_token.type)
        {
            case LPAREN:
                String v1 = Match(LPAREN);
                List<ParseTree.Arg> v2 = args();
                String v3 = Match(RPAREN);
                return new ParseTree.FactorIdent_ParenArgs(v2);
            case LBRACKET:
                String v4 = Match(LBRACKET);
                ParseTree.Expr v5 = expr();
                String v6 = Match(RBRACKET);
                return new ParseTree.FactorIdent_BrackExpr(v5);
            case DOT:
                String v7 = Match(DOT);
                String v8 = Match(SIZE);
                return new ParseTree.FactorIdent_DotSize();
            case EXPROP:
            case RELOP:
            case TERMOP:
            case COMMA:
            case RBRACKET:
            case RPAREN:
            case SEMI:
                return new ParseTree.FactorIdent_Eps();
        }
        throw new Exception("No matching production in factor' at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public List<ParseTree.Arg> args() throws Exception
    {
        // args -> arg_list | eps
        switch(_token.type)
        {
            case IDENT:
            case LPAREN:
            case NUM_LIT:
            case BOOL_LIT:
            case NEW:
                return arg_list();
            case RPAREN:
                return new ArrayList<ParseTree.Arg>();
        }
        throw new Exception("No matching production in args at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public List<ParseTree.Arg> arg_list() throws Exception
    {
        // arg_list -> expr arg_list'
        switch(_token.type)
        {
            case IDENT:
            case LPAREN:
            case NUM_LIT:
            case BOOL_LIT:
            case NEW:
                ParseTree.Expr v1 = expr();
                List<ParseTree.Arg> v2 = arg_list_();
                v2.add(0, new ParseTree.Arg(v1));
                return v2;
        }
        throw new Exception("No matching production in arg_list at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

    public List<ParseTree.Arg> arg_list_() throws Exception
    {
        // arg_list' -> COMMA expr arg_list' | eps
        switch(_token.type)
        {
            case COMMA:
                String v1 = Match(COMMA);
                ParseTree.Expr v2 = expr();
                List<ParseTree.Arg> v3 = arg_list_();
                v3.add(0, new ParseTree.Arg(v2));
                return v3;
            case RPAREN:
                return new ArrayList<ParseTree.Arg>();
        }
        throw new Exception("No matching production in arg_list' at " + _lexer.getYyline() + ":" + _lexer.getYycolumn() + ".");
    }

}
