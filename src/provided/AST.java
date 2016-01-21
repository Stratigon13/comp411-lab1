package provided;


/** Jam general AST type */
public interface AST {
  public <T> T accept(ASTVisitor<T> v);
}

/** Visitor class for general AST type */
interface ASTVisitor<T> {
  T forBoolConstant(BoolConstant b);
  T forIntConstant(IntConstant i);
  T forNullConstant(NullConstant n);
  T forVariable(Variable v);
  T forPrimFun(PrimFun f);
  T forUnOpApp(UnOpApp u);
  T forBinOpApp(BinOpApp b);
  T forApp(App a);
  T forMap(Map m);
  T forIf(If i);
  T forLet(Let l);
}

/** Jam term AST type */
interface Term extends AST {
  public <T> T accept(ASTVisitor<T> v);
}

/** Jam constant type */
interface Constant extends Term {
  public <T> T accept(ASTVisitor<T> v);
}

enum TokenType {
  BOOL, INT, NULL, PRIM_FUN, VAR, OPERATOR, KEYWORD,
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACK, RIGHT_BRACK,
  LEFT_BRACE, RIGHT_BRACE, COMMA, SEMICOLON;
}

/** Jam token type */
interface Token {
  public TokenType getType();
}

/** Jam Boolean constant class */
class BoolConstant implements Token, Constant {
  private boolean value;
  private BoolConstant(boolean b) { value = b; }

  // ** singleton pattern **
  public static final BoolConstant FALSE = new BoolConstant(false);
  public static final BoolConstant TRUE = new BoolConstant(true);

  public boolean getValue() { return value; }

  public <T> T accept(ASTVisitor<T> v) { return v.forBoolConstant(this); }
  public String toString() { return String.valueOf(value); }
  public TokenType getType() { return TokenType.BOOL; }
}

/** Jam integer constant class */
class IntConstant implements Token, Constant {
  private int value;

  IntConstant(int i) { value = i; }
  // duplicates can occur!

  public int getValue() { return value; }

  public <T> T accept(ASTVisitor<T> v) { return v.forIntConstant(this); }
  public String toString() { return String.valueOf(value); }
  public TokenType getType() { return TokenType.INT; }
}

/** Jam null constant class, which is a singleton */
class NullConstant implements Token, Constant {
  public static final NullConstant ONLY = new NullConstant();
  private NullConstant() {}
  public <T> T accept(ASTVisitor<T> v) { return v.forNullConstant(this); }
  public String toString() { return "null"; }
  public TokenType getType() { return TokenType.NULL; }
}

/** Jam primitive function Class */
class PrimFun implements Token, Term {
  private String name;

  PrimFun(String n) { name = n; }

  public String getName() { return name; }
  public <T> T accept(ASTVisitor<T> v) { return v.forPrimFun(this); }
  public String toString() { return name; }
  public TokenType getType() { return TokenType.PRIM_FUN; }
}

/** Jam variable class */
class Variable implements Token, Term {
  private String name;
  Variable(String n) { name = n; }

  public String getName() { return name; }
  public <T> T accept(ASTVisitor<T> v) { return v.forVariable(this); }
  public String toString() { return name; }
  public TokenType getType() { return TokenType.VAR; }
}

/** Jam operator class */
class Op implements Token {
  private String symbol;
  private boolean isUnOp;
  private boolean isBinOp;
  Op(String s, boolean iu, boolean ib) {
    symbol = s; isUnOp = iu; isBinOp = ib;
  }
  Op(String s) {
    // isBinOp only!
    this(s,false,true);
  }
  public String getSymbol() { return symbol; }
  public boolean isUnOp() { return isUnOp; }
  public boolean isBinOp() { return isBinOp; }
  public String toString() { return symbol; }
  public TokenType getType() { return TokenType.OPERATOR; }
}

class KeyWord implements Token {
  private String name;

  KeyWord(String n) { name = n; }
  public String getName() { return name; }
  public String toString() { return name; }
  public TokenType getType() { return TokenType.KEYWORD; }
}

/** Jam left paren token */
class LeftParen implements Token {
  public String toString() { return "("; }
  private LeftParen() {}
  public static final LeftParen ONLY = new LeftParen();
  public TokenType getType() { return TokenType.LEFT_PAREN; }
}

/** Jam right paren token */
class RightParen implements Token {
  public String toString() { return ")"; }
  private RightParen() {}
  public static final RightParen ONLY = new RightParen();
  public TokenType getType() { return TokenType.RIGHT_PAREN; }
}

/** Jam left bracket token */
class LeftBrack implements Token {
  public String toString() { return "["; }
  private LeftBrack() {}
  public static final LeftBrack ONLY = new LeftBrack();
  public TokenType getType() { return TokenType.LEFT_BRACK; }
}

/** Jam right bracket token */
class RightBrack implements Token {
  public String toString() { return "]"; }
  private RightBrack() {}
  public static final RightBrack ONLY = new RightBrack();
  public TokenType getType() { return TokenType.RIGHT_BRACK; }
}

/** Jam left brace token */
class LeftBrace implements Token {
  public String toString() { return "{"; }
  private LeftBrace() {}
  public static final LeftBrace ONLY = new LeftBrace();
  public TokenType getType() { return TokenType.LEFT_BRACE; }
}

/** Jam right brace token */
class RightBrace implements Token {
  public String toString() { return "}"; }
  private RightBrace() {}
  public static final RightBrace ONLY = new RightBrace();
  public TokenType getType() { return TokenType.RIGHT_BRACE; }
}

/** Jam comma token */
class Comma implements Token {
  public String toString() { return ","; }
  private Comma() {}
  public static final Comma ONLY = new Comma();
  public TokenType getType() { return TokenType.COMMA; }
}

/** Jam semi-colon token */
class SemiColon implements Token {
  public String toString() { return ";"; }
  private SemiColon() {}
  public static final SemiColon ONLY = new SemiColon();
  public TokenType getType() { return TokenType.SEMICOLON; }
}


// AST class definitions

/** Jam unary operator application class */
class UnOpApp implements AST {
  private Op rator;
  private AST arg;

  UnOpApp(Op r, AST a) { rator = r; arg = a; }

  public Op getRator() { return rator; }
  public AST getArg() { return arg; }
  public <T> T accept(ASTVisitor<T> v) { return v.forUnOpApp(this); }
  public String toString() { return rator + " " + arg; }
}

/** Jam binary operator application class */
class BinOpApp implements AST {
  private Op rator;
  private AST arg1, arg2;

  BinOpApp(Op r, AST a1, AST a2) { rator = r; arg1 = a1; arg2 = a2; }

  public Op getRator() { return rator; }
  public AST getArg1() { return arg1; }
  public AST getArg2() { return arg2; }
  public <T> T accept(ASTVisitor<T> v) { return v.forBinOpApp(this); }
  public String toString() { 
    return "(" + arg1 + " " + rator + " " + arg2 + ")"; 
  }
}

/** Jam map (closure) class */
class Map implements AST {
  private Variable[] vars;
  private AST body;

  Map(Variable[] v, AST b) { vars = v; body = b; }
  public Variable[] getVars() { return vars; }
  public AST getBody() { return body; }
  public <T> T accept(ASTVisitor<T> v) { return v.forMap(this); }
  public String toString() { 
    return "map " + ToString.toString(vars,",") + " to " + body ;
  }
}  

/** Jam function (PrimFun or Map) application class */
class App implements AST {
  private AST rator;
  private AST[] args;

  App(AST r, AST[] a) { rator = r; args = a; }

  public AST getRator() { return rator; }
  public AST[] getArgs() { return args; }

  public <T> T accept(ASTVisitor<T> v) { return v.forApp(this); }
  public String toString() { 
    if ((rator instanceof Variable) || (rator instanceof PrimFun))
      return rator + "(" + ToString.toString(args,", ") + ")"; 
    else
      return "(" +  rator + ")(" + ToString.toString(args,", ") + ")"; 
  }
}  

/** Jam if expression class */
class If implements AST {
  private AST test, conseq, alt;
  If(AST t, AST c, AST a) { test = t; conseq = c; alt = a; }

  public AST getTest() { return test; }
  public AST getConseq() { return conseq; }
  public AST getAlt() { return alt; }
  public <T> T accept(ASTVisitor<T> v) { return v.forIf(this); }
  public String toString() { 
    return "if " + test + " then " + conseq + " else " + alt ; 
  }
}  

/** Jam let expression class */
class Let implements AST {
  private Def[] defs;
  private AST body;
  Let(Def[] d, AST b) { defs = d; body = b; }

  public <T> T accept(ASTVisitor<T> v) { return v.forLet(this); }
  public Def[] getDefs() { return defs; }
  public AST getBody() { return body; }
  public String toString() { 
    return "let " + ToString.toString(defs," ") + " in " + body; 
  }
}  


/** Jam definition class */
class Def {
  private Variable lhs;
  private AST rhs;  

  Def(Variable l, AST r) { lhs = l; rhs = r; }
  public Variable getLhs() { return lhs; }
  public AST getRhs() { return rhs; }

  public String toString() { return lhs + " := " + rhs + ";"; }
}

/** String utility class */
class ToString {

  /** prints array a with separator s between elements 
   *  this method does NOT accept a == null, since null
   *  is NOT an array */
  public static String toString(Object[] a, String s) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < a.length; i++) {
      if (i > 0) result.append(s);
      Object elt = a[i];
      String eltString = (elt instanceof Object[]) ?
        toString((Object[]) elt, s) : elt.toString();
      result.append(eltString);
    }
    return result.toString();
  }
}

