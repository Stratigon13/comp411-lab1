

import java.io.*;
import java.util.ArrayList;

/** Each parser object in this class contains an embedded lexer which contains an embedded input stream.  The
  * class include a parse() method that will translate the program text in the input stream to the corresponding
  * AST assuming that the program text forms a syntactically valid Jam program.
  */
public class Parser {
  
  private Lexer in;

  
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
	  AST result = null;
	  try {
		  result = parseExp();
	  } catch (Exception e) {
		  throw new ParseException("");
	  }
	  Token token = in.readToken();
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
      //System.out.println("FAC: "+factor.toString());
      Token next = in.peek();
      if (next == LeftParen.ONLY) {
    	//System.out.println(token.toString()+"(");
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
  private AST parseExp(){
	  AST result = null;
	  Token token = in.readToken();
	  TokenType type = token.getType();
	  switch (type) {
	  case KEYWORD:		// map | if | let
		  KeyWord word = (KeyWord) token;
		  if (word.getName().equals("map")){
			  token = in.readToken();
			  ArrayList<Variable> vars = new ArrayList<Variable>();
			  
			  
			  if (token instanceof Variable){
				  Variable var = (Variable) token;
				  vars.add(var);
				  token = in.readToken();
				  while (true){
					  if (token instanceof Comma){
						  token = in.readToken();
					  } else {
						  break;
					  }
					  //System.out.println(token.toString());
					  if (token instanceof Variable){
						  vars.add((Variable) token);
						  token = in.readToken();
					  } else {
						  error(token,"map expected var");
					  }
					  if (token instanceof KeyWord){
						  break;
					  }
				  }	  			  			  
			  }
			  if (token instanceof KeyWord){
				  word = (KeyWord) token;
				  if (!(word.getName().equals("to"))) {
					  error(token,"map");
				  }
			  } else {
				  error(token,"map");
			  }
			  Variable[] arr = new Variable[vars.size()];
			  vars.toArray(arr);
			  return new Map(arr,parseExp());

		  } else if (word.getName().equals("if")){
			  AST t = parseExp();
			  Token next = in.peek();
			  if (next instanceof KeyWord){
				  word = (KeyWord) next;
				  if (word.getName().equals("then")){
					  in.readToken();
				  } else {
					  error(token,"if _ then");
				  }
			  } else {
				  error(token,"if _ then");
			  }
			  AST c = parseExp();
			  next = in.peek();
			  if (next instanceof KeyWord){
				  word = (KeyWord) next;
				  if (word.getName().equals("else")){
					  token = in.readToken();
				  } else {
					  error(token,"if _ then _ else");
				  }
			  } else {
				  error(token,"if _ then _ else");
			  }
			  AST a = parseExp();
			  return new If(t,c,a);
		  } else if (word.getName().equals("let")) {
			  ArrayList<Def> defs = new ArrayList<Def>();
			  token = in.readToken();
			  if (token instanceof Variable){
				  while (token instanceof Variable) {
					  Variable id = (Variable) token;
					  token = in.readToken();
					  if (token instanceof KeyWord) {
						  word =  (KeyWord) token;
						  if (word.getName().equals(":=")){
							  Def def = new Def(id,parseExp());
							  defs.add(def);
							  token = in.readToken();
							  if (token instanceof SemiColon){
								  token = in.readToken();
							  } else {
								  error(token,"let expected ;");
							  }
						  } else {
							  error(token,"let expected := op");
						  }
					  } else {
						  
					  }
				  }
				  if (token instanceof KeyWord){
					  word = (KeyWord) token;
					  if (!(word.getName().equals("in"))) {
						  error(token,"let _ in");
					  }
				  } else {
					  error(token,"let _");
				  }
				  Def[] arr = new Def[defs.size()];
				  defs.toArray(arr);
				  return new Let(arr,parseExp());
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
				  //System.out.println("OP: "+op.toString());
				  //System.out.println("TERM: "+term.toString());
				  //System.out.println("EXP: "+exp.toString());
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
	  if (token == LeftParen.ONLY){
		  exp = parseExp();
		  token = in.readToken();
		  if (token == RightParen.ONLY){
			  //System.out.println("in fac: "+in.peek().toString());
			  return exp;
		  } else {
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
	  Token next = in.peek();
	  while (next != RightParen.ONLY) {
		  args.add(parseExp());
		  next = in.peek();
		  if (next instanceof Comma){
			  in.readToken();
		  }
		  next = in.peek();
	  }
	  in.readToken();
	  AST[] arr = new AST[args.size()]; 
	  args.toArray(arr);
	  //System.out.println("ARGS: "+args.toString());
	  return arr;
  }
  
  private void error(Token token, String message) throws ParseException{
	  System.err.println(token.toString() + " caused an error: " + message);
	  throw new ParseException(message);
  }



  
//TODO
}

