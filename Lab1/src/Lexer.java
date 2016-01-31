

import java.io.*;
import java.util.*;


/** Jam lexer class.              
 *  Given a Lexer object, the next token in that input stream being
 *  processed by the Lexer is returned by static method readToken(); it
 *  throws a ParseException (a form of RuntimeException) if it
 *  encounters a syntax error.  Calling readToken() advances the cursor
 *  in the input stream to the next token.

 *  The static method peek() in the Lexer class has the same behavior as
 *  readToken() except for the fact that it does not advance the cursor.
 */
public class Lexer extends StreamTokenizer {

  // short names for StreamTokenizer codes

  public static final int WORD = StreamTokenizer.TT_WORD; 
  public static final int NUMBER = StreamTokenizer.TT_NUMBER; 
  public static final int EOF = StreamTokenizer.TT_EOF; 
  public static final int EOL = StreamTokenizer.TT_EOL; 

  // wordtable for classifying words (identifiers/operators) in token stream
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
  Lexer(String fileName) throws IOException {
    this(new FileReader(fileName));
  }

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
    wordChars('0','9');
    wordChars('a','z');
    wordChars('A','Z');
    wordChars('_','_');
    wordChars('?','?');
    whitespaceChars(0,' '); 

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
    
  /** Reads the next token as defined by StreamTokenizer in the input stream 
      (consuming it).  
   */
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
  
    // uses getToken() to read next token
    // constructs Token object representing that token
    // NOTE: token representations for all Token classes except
    //   IntConstant are unique; a HashMap is used to avoid duplication
    //   Hence, == can safely be used to compare all Tokens except IntConstants
    //   for equality

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
      throw 
        new ParseException("The number " + nval + " is not a 32 bit integer");
    case WORD:
      Token regToken = wordTable.get(sval);
      if (regToken == null) {
        // must be new variable name
        Variable newVar = new Variable(sval);
        wordTable.put(sval,newVar);
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

    case '+': return wordTable.get("+");  
    case '-': return wordTable.get("-");  
    case '*': return wordTable.get("*");  
    case '/': return wordTable.get("/");  
    case '~': return wordTable.get("~");  
    case '=': return wordTable.get("=");  
    case '<': 
      tokenType = getToken();
      if (tokenType == '=') return wordTable.get("<=");  
      // if (tokenType == '-') return wordTable.get("<-");  
      pushBack();
      return wordTable.get("<");  
    case '>': 
      tokenType = getToken();
      if (tokenType == '=') return wordTable.get(">=");  
      pushBack();
      return wordTable.get(">"); 
      case '!': 
        tokenType = getToken();
        if (tokenType == '=') return wordTable.get("!=");  
        else throw new ParseException("!" + ((char) tokenType) + " is not a legal token");

        /*
         * // this alternate else clause will be added in later assignments
         * pushBack();
         * return wordTable.get("!");  
         */
    case '&': return wordTable.get("&");  
    case '|': return wordTable.get("|");  
    case ':': {
      tokenType = getToken();
      if (tokenType == '=') return wordTable.get(":=");  
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

    wordTable.put("null",  NullConstant.ONLY);
    wordTable.put("true",  BoolConstant.TRUE);
    wordTable.put("false", BoolConstant.FALSE);

    //  Install symbols constructed from self-delimiting characters

    // operators
    // <unop>  ::= <sign> | ~   | ! 
    // <binop> ::= <sign> | "*" | / | = | != | < | > | <= | >= | & | "|" |
    //             <- 
    // <sign>  ::= "+" | -

    //  Note: there is no class distinction between <unop> and <binop> at 
    //  lexical level because of ambiguity; <sign> belongs to both

    wordTable.put("+",   new Op("+",true,true)); 
    wordTable.put("-",   new Op("-",true,true)); 
    wordTable.put("~",   new Op("~",true,false)); 
    wordTable.put("!",   new Op("!",true,false)); 
    // wordTable.put("ref", new Op("ref",true,false));

    wordTable.put("*",  new Op("*")); 
    wordTable.put("/",  new Op("/")); 
    wordTable.put("=",  new Op("=")); 
    wordTable.put("!=", new Op("!=")); 
    wordTable.put("<",  new Op("<")); 
    wordTable.put(">",  new Op(">")); 
    wordTable.put("<=", new Op("<=")); 
    wordTable.put(">=", new Op(">=")); 
    wordTable.put("&",  new Op("&")); 
    wordTable.put("|",  new Op("|")); 
    wordTable.put("<-", new Op("<-")); 


    // Install primitive functions
    // <prim>  ::= number? | function? | list? | null? 
    //           | cons? | cons | first | rest | arity

    wordTable.put("number?",   new PrimFun("number?"));
    wordTable.put("function?", new PrimFun("function?"));
    // wordTable.put("ref?",      new PrimFun("ref?"));
    wordTable.put("list?",     new PrimFun("list?"));
    wordTable.put("null?",     new PrimFun("null?"));
    wordTable.put("cons?",     new PrimFun("cons?"));
    wordTable.put("arity",     new PrimFun("arity"));
    wordTable.put("cons",      new PrimFun("cons"));
    wordTable.put("first",     new PrimFun("first"));
    wordTable.put("rest",      new PrimFun("rest"));

    // keywords: if then else let in map to := 
    wordTable.put("if",   new KeyWord("if"));
    wordTable.put("then", new KeyWord("then"));
    wordTable.put("else", new KeyWord("else"));
    wordTable.put("let",  new KeyWord("let"));
    wordTable.put("in",   new KeyWord("in"));
    wordTable.put("map",  new KeyWord("map"));
    wordTable.put("to",   new KeyWord("to"));
    wordTable.put(":=",   new KeyWord(":="));

  }       

  /** Provides a command line interface to the lexer */
  public static void main(String[] args) throws IOException {
    // check for legal argument list 
    Lexer in;
    if (args.length == 0) {
      in = new Lexer();
    }
    else in = new Lexer(args[0]);
    do {
      Token t = in.readToken();
      if (t == null) break;
      System.out.println("Token " + t + " in " + t.getClass());
    } while (true);
  }
}

