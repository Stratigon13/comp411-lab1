package provided;

/** Parser for Assignment 2 */


import provided.ParseException;
import provided.Lexer;

import java.io.*;
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
	  if (token instanceof Map) {
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
				  if (word.getName() == "to") {
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
		  AST t = parseExp();
		  if (token instanceof KeyWord){
			  KeyWord word = (KeyWord) token;
			  if (word.getName() == "then"){
				  token = in.readToken();
			  } else {
				  error(token,"if _ then");
			  }
		  } else {
			  error(token,"if _ then");
		  }
		  AST c = parseExp();
		  if (token instanceof KeyWord){
			  KeyWord word = (KeyWord) token;
			  if (word.getName() == "else"){
				  token = in.readToken();
			  } else {
				  error(token,"if _ then _ else");
			  }
		  } else {
			  error(token,"if _ then _ else");
		  }
		  AST a = parseExp();
		  return new If(t,c,a);
	  } else if (token instanceof Let) {
		  ArrayList<Def> defs = new ArrayList<Def>();
		  token = in.readToken();
		  if (token instanceof Def){
			  while (token instanceof Def) {
				  defs.add((Def) token);
				  token = in.readToken();
			  }
			  if (token instanceof KeyWord){
				  KeyWord word = (KeyWord) token;
				  if (!(word.getName() == "in")) {
					  error(token,"let _ in");
				  }
			  } else {
				  error(token,"let");
			  }
			  return new Let((Def[])defs.toArray(),parseExp());
		  }else {
			  error(token,"let: no def");
		  }
	  } else {
		  AST result = null;	// exp is a term
		  if ((token instanceof BoolConstant) || (token instanceof IntConstant) || (token instanceof NullConstant)) {
			  result =  parseTerm(token);
		  } else if (token instanceof Op){
			  Op op = (Op) token;
			  if (op.isUnOp()){
				  AST term = parseTerm(token);
				  result = new UnOpApp(op,term);
			  } else {
				  error(token,"unknown op");
			  }
		  } else if ((token instanceof PrimFun) || (token instanceof Variable) || (token instanceof LeftParen)){
			  result = parseTerm(token);
		  }
		  Token next = in.peek();
	  }
  }
  
  private AST parseFactor(Token token) {
	  AST exp = null;
	  if (token instanceof LeftParen){
		  exp = parseExp();
		  token = in.readToken();
		  if (token instanceof RightParen){
			  return exp;
		  } else{
			  error(token,"factor paren");
			  exp = null;
			  return exp;
		  }
	  } else if (token instanceof PrimFun) {
		  return (PrimFun) token;
	  } else if (token instanceof Variable) {
		  return (Variable) token;
	  } else{
		  error(token,"unknown factor");
		  return exp;
	  }
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

