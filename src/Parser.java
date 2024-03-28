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
        int token_type = _lexer.yylex();                                    // get next/first token from lexer
        if(token_type ==  0)      _token = new Token(ENDMARKER , null);     // if  0 => token is endmarker
        else if(token_type == -1) _token = new Token(LEXERROR  , yylval);   // if -1 => there is a lex error
        else                      _token = new Token(token_type, yylval);   // otherwise, set up _token
    }

    public String Match(int token_type) throws Exception
    {
        boolean match = (token_type == _token.type);
        String lexeme = "";
        if(_token.attr != null) lexeme = (String)_token.attr.obj;

        if(match == false)                          // if token does not match
            throw new Exception("token mismatch");  // throw exception (indicating parsing error in this assignment)

        if(_token.type != ENDMARKER)    // if token is not endmarker,
            Advance();                  // make token point next token in input by calling Advance()

        return lexeme;
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
        throw new Exception("error");
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
        throw new Exception("error");
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
        throw new Exception("error");
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
        throw new Exception("error");
    }
    public ParseTree.PrimType prim_type() throws Exception
    {
        //    prim_type -> NUM
        switch(_token.type)
        {
            case NUM:
            {
                String v1 = Match(NUM);
                return new ParseTree.PrimTypeNum();
            }
        }
        throw new Exception("error");
    }
    public List<ParseTree.Param> params() throws Exception
    {
        //       params -> eps
        switch(_token.type)
        {
            case RPAREN:
                return new ArrayList<ParseTree.Param>();
        }
        throw new Exception("error");
    }
    public List<ParseTree.LocalDecl> local_decls() throws Exception
    {
        //  local_decls -> local_decls'
        switch(_token.type)
        {
            case END:
                return local_decls_();
        }
        throw new Exception("error");
    }
    public List<ParseTree.LocalDecl> local_decls_() throws Exception
    {
        // local_decls' -> eps
        switch(_token.type)
        {
            case END:
                return new ArrayList<ParseTree.LocalDecl>();
        }
        throw new Exception("error");
    }
    public List<ParseTree.Stmt> stmt_list() throws Exception
    {
        //    stmt_list -> stmt_list'
        switch(_token.type)
        {
            case END:
                return stmt_list_();
        }
        throw new Exception("error");
    }
    public List<ParseTree.Stmt> stmt_list_() throws Exception
    {
        //   stmt_list' -> eps
        switch(_token.type)
        {
            case END:
                return new ArrayList<ParseTree.Stmt>();
        }
        throw new Exception("error");
    }
}
