package provided;

/** Parser for Assignment 2 */


import provided.ParseException;
import provided.Lexer;

import java.io.*;

/** Each parser object in this class contains an embedded lexer which contains an embedded input stream.  The
  * class include a parse() method that will translate the program text in the input stream to the corresponding
  * AST assuming that the program text forms a syntactically valid Jam program.
  */
public class Parser {
  
  private Lexer in;
  private AST result;

  //TODO
  
  public Parser(Lexer i) {
    in = i;
    initParser();
  }
  
  public Parser(Reader inputStream) { this(new Lexer(inputStream)); }
  
  public Parser(String fileName) throws IOException { this(new FileReader(fileName)); }
  
  public Lexer lexer() { return in; }
  
  private void initParser() {
	  result = null;
  }
  
  /** Parses the program text in the lexer bound to 'in' and returns the corresponding AST. 
    * @throws ParseException if a syntax error is encountered (including lexical errors). 
    */
  public AST parse() throws ParseException {
	  Token token = in.readToken();
	  TokenType type = token.getType();
	  if ((type == TokenType.BOOL) || (type == TokenType.INT) || (type == TokenType.NULL)) {
		  result = parseTerm(token);
	  } else if (token instanceof Op){
		  Op op = (Op) token;
		  if (op.isUnOp()){
			  result = parseTerm(token);
		  }
	  } else if (token instanceof Map){
		  Map map = (Map) token;
		  result = parseExp();		  
	  } else if (token instanceof If){
		  If iff = (If) token;
		  result = parseExp();	
	  } else if (token instanceof Let){
		  Let let = (Let) token;
		  result = parseExp();	
	  }
	  
	  
	return result;
  }
  

  /** Parses:
      *  <term>     ::= { <unop> } <term> | <constant> | <factor> {( <exp-list> )}
      *  <constant> ::= <null> | <int> | <bool>
      * @param token   first token in input stream to be parsed; remainder in Lexer named in.
      */
    private AST parseTerm(Token token) {

      if (token instanceof Op) {
        Op op = (Op) token;
        if (! op.isUnOp()) error(op,"unary operator");
        return new UnOpApp(op, parseTerm(in.readToken()));
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
    *     <exp> :: = if <exp> then <exp> else <exp>
    *              | let <prop-def-list> in <exp>
    *              | map <id-list> to <exp>
    *              | <term> { <biop> <exp> }
    * 
    * @return  the corresponding AST.
    */
  private AST parseExp() {
	  return (AST) in.peek();
  }
  
  private AST parseFactor(Token token) {
	  TokenType type = token.getType();
	  if (type == TokenType.OPERATOR){
		  
	  }
	  if (token instanceof Term ){
	  }
	  return (AST) in.peek();
	  }
  
  private AST[] parseArgs() {
	  AST[] out = new AST[10];
	  out[0] = (AST) in.peek();
	  return out;
  }
  
  private void error(Op op, String message){
	  System.err.println(op.toString() + " caused an error: " + message);
  }
  

  
//TODO
}

