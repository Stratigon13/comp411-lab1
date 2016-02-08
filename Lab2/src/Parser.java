/** Parser for Assignment 1 */

import java.io.*;
import java.util.*;

/** Class that represented parsing errors. */
class ParseException extends RuntimeException {
  /**
	 * 
	 */
	private static final long serialVersionUID = -8577790438709411611L;

ParseException(String s) { super(s); }
}

/** Jam lexer class.              
  * Given a Lexer object, the next token in that input stream being
  * processed by the Lexer is returned by static method readToken(); it
  * throws a ParseException (a form of RuntimeException) if it
  * encounters a syntax error.  Calling readToken() advances the cursor
  * in the input stream to the next token.
  * 
  * The static method peek() in the Lexer class has the same behavior as
  * readToken() except for the fact that it does not advance the cursor.
  */
class Lexer extends StreamTokenizer {
  
  /* short names for StreamTokenizer codes */
  
  public static final int WORD = StreamTokenizer.TT_WORD; 
  public static final int NUMBER = StreamTokenizer.TT_NUMBER; 
  public static final int EOF = StreamTokenizer.TT_EOF; 
  public static final int EOL = StreamTokenizer.TT_EOL; 
  
  /* operator Tokens */
  
  // <unop>  ::= <sign> | ~   | ! 
  // <binop> ::= <sign> | "*" | / | = | != | < | > | <= | >= | & | "|" |
  //             <- 
  // <sign>  ::= "+" | -
  
  //  Note: there is no class distinction between <unop> and <binop> at 
  //  lexical level because of ambiguity; <sign> belongs to both
  
  public static final OpToken PLUS = OpToken.newBothOpToken(UnOpPlus.ONLY, BinOpPlus.ONLY); 
  public static final OpToken MINUS = OpToken.newBothOpToken(UnOpMinus.ONLY, BinOpMinus.ONLY);
  public static final OpToken TIMES = OpToken.newBinOpToken(OpTimes.ONLY);
  public static final OpToken DIVIDE = OpToken.newBinOpToken(OpDivide.ONLY);
  public static final OpToken EQUALS = OpToken.newBinOpToken(OpEquals.ONLY);
  public static final OpToken NOT_EQUALS = OpToken.newBinOpToken(OpNotEquals.ONLY);
  public static final OpToken LESS_THAN = OpToken.newBinOpToken(OpLessThan.ONLY);
  public static final OpToken GREATER_THAN = OpToken.newBinOpToken(OpGreaterThan.ONLY);
  public static final OpToken LESS_THAN_EQUALS = OpToken.newBinOpToken(OpLessThanEquals.ONLY);
  public static final OpToken GREATER_THAN_EQUALS = OpToken.newBinOpToken(OpGreaterThanEquals.ONLY);
  public static final OpToken NOT = OpToken.newUnOpToken(OpNot.ONLY);
  public static final OpToken AND = OpToken.newBinOpToken(OpAnd.ONLY);
  public static final OpToken OR = OpToken.newBinOpToken(OpOr.ONLY);
  
  /* Used to support reference cells. */
//  public static final OpToken BANG = OpToken.newUnOpToken(OpBang.ONLY);
//  public static final OpToken GETS = OpToken.newBinOpToken(OpGets.ONLY);
//  public static final OpToken REF = OpToken.newUnOpToken(OpRef.ONLY);
  
  /* Keywords */

  public static final KeyWord IF     = new KeyWord("if");
  public static final KeyWord THEN   = new KeyWord("then");
  public static final KeyWord ELSE   = new KeyWord("else");
  public static final KeyWord LET    = new KeyWord("let");
//  public static final KeyWord LETREC = new KeyWord("letrec");   // Used to support letrec extension
  public static final KeyWord IN     = new KeyWord("in");
  public static final KeyWord MAP    = new KeyWord("map");
  public static final KeyWord TO     = new KeyWord("to");
  public static final KeyWord BIND   = new KeyWord(":=");
  
  // wordtable for classifying words in token stream
  public HashMap<String,Token>  wordTable = new HashMap<String,Token>();

  // Lexer peek cannot be implemented using StreamTokenizer pushBack 
  // because some Tokens are composed of two StreamTokenizer tokens

  Token buffer;  // holds token for peek() operation
 
  /* constructors */

  /** Constructs a Lexer for the specified inputStream */
  Lexer(Reader inputStream) {
    super(new BufferedReader(inputStream));
    initLexer();
  }

  /** Constructs a Lexer for the contents of the specified file */
  Lexer(String fileName) throws IOException { this(new FileReader(fileName)); }

  /** Constructs a Lexer for the default console input stream System.in */  
  Lexer() {
    super(new BufferedReader(new InputStreamReader(System.in)));
    initLexer();
  }

  /* Initializes lexer tables and the StreamTokenizer that the lexer extends */
  private void initLexer() {

    // configure StreamTokenizer portion of this
    resetSyntax();
    parseNumbers();
    ordinaryChar('-');
    slashSlashComments(true);
    wordChars('0', '9');
    wordChars('a', 'z');
    wordChars('A', 'Z');
    wordChars('_', '_');
    wordChars('?', '?');
    whitespaceChars(0, ' '); 

    // `+' `-' `*' `/' `~' `=' `<' `>' `&' `|' `:' `;' `,' '!'
    // `(' `)' `[' `]' are ordinary characters (self-delimiting)

    initWordTable();
    buffer = null;  // buffer initially empty
  }

  /** Reads tokens until next end-of-line */
  public void flush() throws IOException {
    eolIsSignificant(true);
    while (nextToken() != EOL) ; // eat tokens until EOL
    eolIsSignificant(false);
  }

  /** Returns the next token in the input stream without consuming it */
  public Token peek() { 
    if (buffer == null) buffer = readToken();
    return buffer;
  }
    
  /** Reads the next token as defined by StreamTokenizer in the input stream (consuming it). */
  private int getToken() {
    // synonymous with nextToken() except for throwing an unchecked 
    // ParseException instead of a checked IOException
    try {
      int tokenType = nextToken();
      return tokenType;
    } catch(IOException e) {
      throw new ParseException("IOException " + e + "thrown by nextToken()");
    }
  }

  /** Reads the next Token in the input stream (consuming it) */
  public Token readToken() {
    
    /* Uses getToken() to read next token and  constructs the Token object representing that token.
     * NOTE: token representations for all Token classes except IntConstant are unique; a HashMap 
     * is used to avoid duplication.  Hence, == can safely be used to compare all Tokens except 
     * IntConstants for equality (assuming that code does not gratuitously create Tokens).
     */
    
    if (buffer != null) {
      Token token = buffer;
      buffer = null;          // clear buffer
      return token;
    }
    
    int tokenType = getToken();
    
    switch (tokenType) {
      
      case NUMBER:
        int value = (int) nval;
        if (nval == (double) value) return new IntConstant(value);
        throw new ParseException("The number " + nval + " is not a 32 bit integer");
      case WORD:
        Token regToken = wordTable.get(sval);
        if (regToken == null) {
          // must be new variable name
          Variable newVar = new Variable(sval);
          wordTable.put(sval, newVar);
          return newVar;
        }
        return regToken;
        
      case EOF: return null;
      case '(': return LeftParen.ONLY;
      case ')': return RightParen.ONLY;
      case '[': return LeftBrack.ONLY;
      case ']': return RightBrack.ONLY;
      // case '{': return LeftBrace.ONLY;
      // case '}': return RightBrace.ONLY;
      case ',': return Comma.ONLY;
      case ';': return SemiColon.ONLY;
      
      case '+': return PLUS;  
      case '-': return MINUS;  
      case '*': return TIMES;  
      case '/': return DIVIDE;  
      case '~': return NOT;  
      case '=': return EQUALS;
      
      case '<': 
        tokenType = getToken();
        if (tokenType == '=') return LESS_THAN_EQUALS;  
//      if (tokenType == '-') return GETS;    // Used to support reference cells
        pushBack();
        return LESS_THAN; 
        
      case '>': 
        tokenType = getToken();
        if (tokenType == '=') return GREATER_THAN_EQUALS;  
        pushBack();
        return GREATER_THAN;
        
      case '!': 
        tokenType = getToken();
        if (tokenType == '=') return NOT_EQUALS;  
        else throw new ParseException("!" + ((char) tokenType) + " is not a legal token"); 
        
        /* this  else clause supports reference cells */
//        pushBack();
//        return wordTable.get("!");  
     
      case '&': return AND;  
      case '|': return OR;  
      case ':': {
        tokenType = getToken();
        if (tokenType == '=') return wordTable.get(":=");   // ":=" is a keyword not an operator 
        pushBack();
        throw new ParseException("`:' is not a legalken");
      }
      default:  
        throw new 
        ParseException("`" + ((char) tokenType) + "' is not a legal token");
    }
  }
    
  /** Initializes the table of Strings used to recognize Tokens */
  private void initWordTable() {
    // initialize wordTable
    
    // constants
    // <null>  ::= null
    // <bool>  ::= true | false
    
    wordTable.put("null", NullConstant.ONLY);
    wordTable.put("true",  BoolConstant.TRUE);
    wordTable.put("false", BoolConstant.FALSE);
    
    //  Install symbols constructed from self-delimiting characters
    
    // operators
    // <unop>  ::= <sign> | ~   // formerly | ! | ref
    // <binop> ::= <sign> | "*" | / | = | != | < | > | <= | >= | & | "|" 
    // <sign>  ::= "+" | -
    
    //  Note: there is no class distinction between <unop> and <binop> at 
    //  lexical level because of ambiguity; <sign> belongs to both
    
    wordTable.put("+",   OpToken.newBothOpToken(UnOpPlus.ONLY, BinOpPlus.ONLY)); 
    wordTable.put("-",   OpToken.newBothOpToken(UnOpMinus.ONLY, BinOpMinus.ONLY)); 
    
    wordTable.put("~",   OpToken.newUnOpToken(Lexer.NOT.toUnOp())); 
    // Supports addition of ref cells to Jam
    // wordTable.put("!",   OpToken.newUnOpToken("!",   Lexer.BANG)); 
    // wordTable.put("ref", OpToken.newUnOpToken("ref", Lexer.REF)); 
    
    wordTable.put("*",   OpToken.newBinOpToken(OpTimes.ONLY)); 
    wordTable.put("/",   OpToken.newBinOpToken(OpDivide.ONLY)); 
    wordTable.put("=",   OpToken.newBinOpToken(OpEquals.ONLY)); 
    wordTable.put("!=",  OpToken.newBinOpToken(OpNotEquals.ONLY)); 
    wordTable.put("<",   OpToken.newBinOpToken(OpLessThan.ONLY)); 
    wordTable.put(">",   OpToken.newBinOpToken(OpGreaterThan.ONLY)); 
    wordTable.put("<=",  OpToken.newBinOpToken(OpLessThanEquals.ONLY)); 
    wordTable.put(">=",  OpToken.newBinOpToken(OpGreaterThanEquals.ONLY)); 
    wordTable.put("&",   OpToken.newBinOpToken(OpAnd.ONLY)); 
    wordTable.put("|",   OpToken.newBinOpToken(OpOr.ONLY)); 
    // Supports addition of ref cells to Jam
    // wordTable.put("<-",  OpToken.newBinOpToken("<-",OpGets.ONLY)); 
    
    // Install primitive functions
    // <prim>  ::= number? | function? | list? | null? | cons? ref? | 
    //             arity | cons | first | rest 
    
    wordTable.put("number?",   NumberPPrim.ONLY);
    wordTable.put("function?", FunctionPPrim.ONLY);
    wordTable.put("list?",     ListPPrim.ONLY);
    wordTable.put("null?",     NullPPrim.ONLY);
    wordTable.put("cons?",     ConsPPrim.ONLY);
    wordTable.put("ref?",      RefPPrim.ONLY);
    wordTable.put("arity",     ArityPrim.ONLY);
    wordTable.put("cons",      ConsPrim.ONLY);
    wordTable.put("first",     FirstPrim.ONLY);
    wordTable.put("rest",      RestPrim.ONLY);
    
    
    // keywords: if then else let in map to := 
    wordTable.put("if",   Lexer.IF);
    wordTable.put("then", Lexer.THEN);
    wordTable.put("else", Lexer.ELSE);
    wordTable.put("let",  Lexer.LET);
    wordTable.put("in",   Lexer.IN);
    wordTable.put("map",  Lexer.MAP);
    wordTable.put("to",   Lexer.TO);
    wordTable.put(":=",   Lexer.BIND);
    
  }
}


class Parser {
  
  private Lexer in;
  
  Parser(Lexer i) {
    in = i;
  }
  
  Parser(Reader inputStream) { this(new Lexer(inputStream)); }
  
  Parser(String fileName) throws IOException { this(new FileReader(fileName)); }
  
  Lexer lexer() { return in; }
  
  public AST parse() throws ParseException {
    AST prog = parseExp();
    Token t = in.readToken();
    if (t == null) return prog;
    else throw
      new ParseException("Legal program followed by extra token " + t);
  }
  
  /** Parses:
   *   <exp> :: = if <exp> then <exp> else <exp>
   *            | let <prop-def-list> in <exp>
   *            | map <id-list> to <exp>
   *            | <term> { <biop> <term> }*  // (left associatively!)
   */
  private AST parseExp() {
    Token token = in.readToken();

    if (token == Lexer.IF) return parseIf();
//    if (token == Lexer.LETREC) return parseLetRec();  // supports addition of letrec
    if (token == Lexer.LET) return parseLet();
    if (token == Lexer.MAP) return parseMap();
    
    /*  Supports the addition of blocks to Jam */
//    if (token == LeftBrace.ONLY) {
//      AST[] exps = parseExps(SemiColon.ONLY,RightBrace.ONLY);  
//      // including closing brace
//      if (exps.length == 0) throw new ParseException("Illegal empty block");
//      return new Block(exps);
//    }

    /* phrase begin with a term */
    AST exp = parseTerm(token);
    
    Token next = in.peek();
    while (next instanceof OpToken) {
      OpToken op = (OpToken) next;
      in.readToken(); // remove next from input stream
      if (! (op.isBinOp())) error(next, "binary operator");
      AST newTerm = parseTerm(in.readToken());
      exp = new BinOpApp(op.toBinOp(), exp, newTerm);
//      System.err.println("exp updated to: " + exp);
      next = in.peek();
    }
//    System.err.println("parseTerm returning " + exp);
    return exp;
  }
  
  /** Parses:
    *  <term>     ::= { <unop> } <term> | <constant> | <factor> {( <exp-list> )}
    *  <constant> ::= <null> | <int> | <bool>
    * @param token   first token in input stream to be parsed; remainder in Lexer named in.
    */
  private AST parseTerm(Token token) {

    if (token instanceof OpToken) {
      OpToken op = (OpToken) token;
      if (! op.isUnOp()) error(op,"unary operator");
      return new UnOpApp(op.toUnOp(), parseTerm(in.readToken()));
    }
    
    if (token instanceof Constant) return (Constant) token;
    AST factor = parseFactor(token);
    Token next = in.peek();
    if (next == LeftParen.ONLY) {
      in.readToken();  // remove next from input stream
      AST[] exps = parseArgs();  // including closing paren
      return new App(factor,exps);
    }
    return factor;
  }
  
  /** Parses:
    *  <factor>   ::= <prim> | <variable> | ( <exp> )
    * @param token   first token in input stream to be parsed; remainder in Lexer named in.
    */
  private AST parseFactor(Token token) {
    
//    System.err.println("parseFactor(" + token + ") called");
    
    if (token == LeftParen.ONLY) {
      AST exp = parseExp();
      token = in.readToken();
      if (token != RightParen.ONLY) error(token,"`)'");
      return exp;
    }
    
    if (! (token instanceof PrimFun) && ! (token instanceof Variable))
      error(token,"constant, primitive, variable, or `('");
    
    // Term\Constant = Variable or PrimFun       
    return (Term) token;
  }      
  
  /** Parses `if <exp> then <exp> else <exp>' given that `if' has already been read. */
  private AST parseIf() {
    
    AST test = parseExp();
    Token key1 = in.readToken();
    if (key1 != Lexer.THEN) error(key1,"`then'");
    AST conseq = parseExp();
    Token key2 = in.readToken();
    if (key2 != Lexer.ELSE) error(key2,"`else'");
    AST alt = parseExp();
    return new If(test,conseq,alt);
  }
  
    
  /** Parses `let <prop-def-list> in <exp>' given that `let' has already been read. */ 
  private AST parseLet() {
    Def[] defs = parseDefs(false); 
    // consumes `in'; false means rhs may be non Map
    AST body = parseExp();
    return new Let(defs,body);
  }
  
  /* Supports the parsing of 'letrec' */
//  /** Parses `letrec <prop-def-list> in <exp>' given that `letrec' has already been read. */
//  private AST parseLetRec() {
//    
//    Def[] defs = parseDefs(true);
//    // consumes `in'; true means each rhs must be a Map
//    AST body = parseExp();
//    return new LetRec(defs,body);
//  }
  
  /* Parses `map <id-list> to <exp>' given that `map' has already been read. */
  private AST parseMap() {

    Variable[] vars = parseVars(); // consumes the delimiter `to'
    AST body = parseExp();
    return new Map(vars, body);
  }
  
  /** Parses `<exp-list> <delim>' where
    *  <exp-list>      ::= <empty> | <prop-exp-list>
    *  <empty> ::=
    *  <prop-exp-list> ::= <exp> | <exp> <separator> <prop-exp-list> 
    */
  private AST[] parseExps(Token separator, Token delim) {
    
    LinkedList<AST> exps = new LinkedList<AST>();
    Token next = in.peek();
    
    if (next == delim) {
      in.readToken(); // consume RightParen
      return new AST[0];
    }
    
    // next is still at front of input stream
    
    do {
      AST exp = parseExp();
      exps.addLast(exp);
      next = in.readToken();
    } while (next == separator);
    
    if (next != delim) error(next,"`,' or `)'");
    return (AST[]) exps.toArray(new AST[0]);
  }
  
  private AST[] parseArgs() { return parseExps(Comma.ONLY,RightParen.ONLY); }
  
  /** Parses <id-list> where
    *   <id-list>       ::= <empty> | <prop-id-list>
    *   <prop-id-list>  ::= <id> | <id> , <id-list> 
    *  NOTE: consumes `to' following <id-list>
    */
  private Variable[] parseVars() {
 
    LinkedList<Variable> vars = new LinkedList<Variable>();
    Token t = in.readToken();
    if (t == Lexer.TO) return new Variable[0];
    
    do {
      if (! (t instanceof Variable)) error(t,"variable");
      vars.addLast((Variable)t);
      t = in.readToken();
      if (t == Lexer.TO) break; 
      if (t != Comma.ONLY) error(t, "`to' or `, '");
      // Comma found, read next variable
      t = in.readToken();
    } while (true);
    return (Variable[]) vars.toArray(new Variable[0]);
  }
  
  /** Parses a proper list of definitions, more technically parses
    *   <prop-def-list> ::= <def> | <def> <def-list> 
    *   NOTE: consumes `in' following <prop-def-list> */
  
  private Def[] parseDefs(boolean forceMap) {
    LinkedList<Def> defs = new LinkedList<Def>();
    Token t = in.readToken();
    
    do {
      Def d = parseDef(t);        
      if (forceMap && (! (d.rhs() instanceof Map)))
        throw new ParseException("right hand side of definition `" + d + "' is not a map expression");
      defs.addLast(d);
      t = in.readToken();
    } while (t != Lexer.IN);
    
    return (Def[]) defs.toArray(new Def[0]);
  }
  
  /** Parses 
    *   <id> := <exp> ;
    * which is <def> given that first token var has been read.
    */
  private Def parseDef(Token var) {
    
    if (! (var instanceof Variable)) error(var, "variable");
    
    Token key = in.readToken();
    if (key != Lexer.BIND) error (key,"`:='");
    
    AST exp = parseExp();
    
    Token semi = in.readToken();
    if (semi != SemiColon.ONLY) error(semi,"`;'");
    return new Def((Variable) var, exp);
  }
  
  private AST error(Token found, String expected) {
//    for (int i = 0; i < 10; i++) {
//      System.out.println(in.readToken());
//    }   
    throw new ParseException("Token `" + found + "' appears where " + expected + " was expected");
  }
  
  public static void main(String[] args) throws IOException {
    // check for legal argument list 
    if (args.length == 0) {
//      System.out.println("Usage: java Parser <filename>");
      return;
    }
    Parser p = new Parser(args[0]);
    AST prog = p.parse();
//    System.out.println("Parse tree is: " + prog);
  }
}

