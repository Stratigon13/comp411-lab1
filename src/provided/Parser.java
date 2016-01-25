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
	  AST result = null;
	  Token token = in.readToken();
	  TokenType type = token.getType();
	  switch (type) {
	  case KEYWORD:		// map or if or let
		  KeyWord word = (KeyWord) token;
		  if (word.getName() == "map"){
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
					  break;
				  }
			  }
			  if (token instanceof KeyWord){
				  word = (KeyWord) token;
				  if (word.getName() == "to") {
					  token = in.readToken();
				  } else {
					  error(token,"map");
				  }
			  } else {
				  error(token,"map");
			  }
			  return new Map((Variable[])vars.toArray(),parseExp());

		  } else if (word.getName() == "if"){
			  AST t = parseExp();
			  if (token instanceof KeyWord){
				  word = (KeyWord) token;
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
				  word = (KeyWord) token;
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
		  } else if (word.getName() == "let") {
			  ArrayList<Def> defs = new ArrayList<Def>();
			  token = in.readToken();
			  if (token instanceof Def){
				  while (token instanceof Def) {
					  defs.add((Def) token);
					  token = in.readToken();
				  }
				  if (token instanceof KeyWord){
					  word = (KeyWord) token;
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
		  }
	  case BOOL: case INT: case LEFT_PAREN:
	  case NULL: case OPERATOR: case PRIM_FUN:
	  case VAR: 			// term { binop exp }
		  AST term = parseTerm(token);
		  Token next = in.peek();
		  if (next instanceof Op){
			  token = in.readToken();
			  Op op = (Op) token;
			  if (op.isBinOp()){
				  AST exp = parseExp();
				  result = new BinOpApp(op,term,exp);
			  } else {
				  error(token,"term _");
			  }
		  } else {
			  result = term;
		  }
		  return result;
	  default:
		  throw new 
	        ParseException("parseExp:`" +  type + "' is not a legal token");
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
	  ArrayList<AST> args = new ArrayList<AST>();
	  Token token = in.readToken();
	  while (token != RightParen.ONLY) {
		  args.add(parseExp());
		  Token next = in.peek();
		  if (next instanceof Comma){
			  token = in.readToken();
		  }
		  token = in.readToken();
	  }
	  return (AST[])args.toArray();
  }
  
  private void error(Token token, String message){
	  System.err.println(token.toString() + " caused an error: " + message);
  }
  

  
//TODO
}

