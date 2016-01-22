package provided;

/** Parser for Assignment 2 */


import provided.ParseException;
import provided.Lexer;

import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;

/** Each parser object in this class contains an embedded lexer which contains an embedded input stream.  The
  * class include a parse() method that will translate the program text in the input stream to the corresponding
  * AST assuming that the program text forms a syntactically valid Jam program.
  */
public class Parser {
  
  private Lexer in;

  //TODO
  
  public Parser(Lexer i) {
    in = i;
    initParser();
  }
  
  public Parser(Reader inputStream) { this(new Lexer(inputStream)); }
  
  public Parser(String fileName) throws IOException { this(new FileReader(fileName)); }
  
  public Lexer lexer() { return in; }
  
  private void initParser() {
  }
  
  /** Parses the program text in the lexer bound to 'in' and returns the corresponding AST. 
    * @throws ParseException if a syntax error is encountered (including lexical errors). 
    */
  public AST parse() throws ParseException {
	return parseExp();
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
	  Token token = in.readToken();
	  //TokenType type = token.getType();
	  AST result = null;
	  if ((token instanceof BoolConstant) || (token instanceof IntConstant) || (token instanceof NullConstant)) {
		  result = parseTerm(token);
	  } else if (token instanceof Op){
		  Op op = (Op) token;
		  if (op.isUnOp()){
			  result = parseTerm(token);
		  }
	  } else if (token instanceof Map) {
		  token = in.readToken();
		  if (token instanceof LeftBrace){
			  token = in.readToken();
			  ArrayList<Variable> vars = new ArrayList<Variable>();
			  while (true){
				  if (token instanceof Variable){
					  Variable var = (Variable) token;
					  vars.add(var);
					  token = in.readToken();
					  if (token instanceof Comma){
						  token = in.readToken();
					  } else {
						  break;
					  }
				  } else {
					  error(token,"map");
				  }
			  }
			  if (token instanceof RightBrace){
				  token = in.readToken();
			  } else {
				  error(token,"map");
			  }
			  if (token instanceof KeyWord){
				  KeyWord word = (KeyWord) token;
				  if (word.getName() == "to"){
					  token = in.readToken();
				  } else {
					  error(token,"map");
				  }
			  } else {
				  error(token,"map");
			  }
			  return new Map((Variable[])vars.toArray(),parseExp());
		  }else {
			  error(token,"map");
		  }
	  } else if (token instanceof If) {
		  If iff = (If) token;
		  //TODO
	  } else if (token instanceof Let) {
		  Let let = (Let) token;
		  //TODO	
	  } else if (token instanceof PrimFun) {
		  result = parseFactor(token);
	  } else if (token instanceof Variable) {
	      result = parseFactor(token); 
	  } else if (token instanceof LeftParen) {
	      result = parseFactor(token);
	  }
	  return result;
  }
  
  private AST parseFactor(Token token) {
	  if (token instanceof LeftParen){
		  AST exp = parseExp();
		  token = in.readToken();
		  if (token instanceof RightParen){
			  return exp;
		  } else{
			  error(token,"factor paren");
		  }
	  } else if (token instanceof PrimFun) {
		  return (PrimFun) token;
	  } else if (token instanceof Variable) {
		  return (Variable) token;
	  } else{
		  error(token,"unknown factor");
	  }
	  return (AST) in.peek();
	  }
  
  private AST[] parseArgs() {
	  AST[] out = new AST[10];
	  out[0] = (AST) in.peek();
	  return out;
  }
  
  private void error(Token token, String message){
	  System.err.println(token.toString() + " caused an error: " + message);
  }
  

  
//TODO
}

