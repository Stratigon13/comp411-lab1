/** Call-by-value, Call-by-name, and Call-by-need Jam interpreter */

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/** Interpreter Classes */

/** A visitor interface for interpreting Jam AST's */
interface EvalVisitor extends ASTVisitor<JamVal> {
  /** Constructs a new visitor of same class with specified environment e. */
  EvalVisitor newVisitor(PureList<Binding> e);
  PureList<Binding> env();
  Binding newBinding(Variable var, AST ast);
}

/** A class that implements call-by-value, call-by-name, and call-by-need interpretation of Jam programs. */
class Interpreter {
  
  Parser parser = null;
  
  /** Constructor for a program given in a file. */
  Interpreter(String fileName) throws IOException { parser = new Parser(fileName); }
  
  /** Constructor for a program already embedded in a Parser object. */
  Interpreter(Parser p) { parser = p; }  
  
  /** Constructor for a program embedded in a Reader. */
  Interpreter(Reader reader) { parser = new Parser(reader); }
  
  /** Class representing a context used for context sensitive checking */
  class Context {
	  HashSet<Variable> encounteredVars;
	  HashSet<Variable> currentVars;
	  Context() {
		  encounteredVars = new HashSet<Variable>();
		  currentVars = new HashSet<Variable>();
	  }
	  
	  Boolean add(Variable v){
		  if (currentVars.contains(v)){
			  return false;
		  } else {
			  currentVars.add(v);
			  return true;
		  }
	  }
	  
	  Boolean test(Variable v){
		  if (encounteredVars.contains(v)){
			  return true;
		  } else {
			  return false;
		  }
	  }
	  
	  void flush(){
		  encounteredVars.addAll(currentVars);
		  currentVars.clear();
	  }
  }
  
  /** Traverses the AST tree for free variables or repeated variables in map or let */
  public void contextCheck(AST prog, Context cIn) throws SyntaxException {
	  Context context;
	  if (cIn == null) {
		  context = new Context();
	  } else {
		  context = cIn;
	  }
	  AST cur;
	  List<AST> list = new ArrayList<AST>();
	  list.add(prog);
	  while(!list.isEmpty()) { 
		  cur = list.remove(0);
		  if (cur == NullConstant.ONLY) {
			  continue;
		  }
		  if (cur != null) {
			  list.addAll(Arrays.asList(cur.accept(new ASTVisitor<AST[]>() {
		
				@Override
				public AST[] forBoolConstant(BoolConstant b) {
					AST[] visRes = new AST[1];
					visRes[0] = NullConstant.ONLY;
					return visRes;
				}
		
				@Override
				public AST[] forIntConstant(IntConstant i) {
					AST[] visRes = new AST[1];
					visRes[0] = NullConstant.ONLY;
					return visRes;
				}
		
				@Override
				public AST[] forNullConstant(NullConstant n) {
					System.err.println("Unreachable code: NullConst ContextChecker");
					AST[] visRes = new AST[1];
					visRes[0] = NullConstant.ONLY;
					return visRes;
				}
		
				@Override
				public AST[] forVariable(Variable v) {
					if (context.test(v)) {
						AST[] visRes = new AST[1];
						visRes[0] = NullConstant.ONLY;
						return visRes;
					} else {
						throw new SyntaxException(v + " is a free variable");
					}
				}
		
				@Override
				public AST[] forPrimFun(PrimFun f) {
					AST[] visRes = new AST[1];
					visRes[0] = NullConstant.ONLY;
					return visRes;
				}
		
				@Override
				public AST[] forUnOpApp(UnOpApp u) {
					AST[] visRes = new AST[1];
					visRes[0] = u.arg();
					return visRes;
				}
		
				@Override
				public AST[] forBinOpApp(BinOpApp b) {
					AST[] visRes = new AST[2];
					visRes[0] = b.arg1();
					visRes[1] = b.arg2();
					return visRes;
				}
		
				@Override
				public AST[] forApp(App a) {
					return a.args();
				}
		
				@Override
				public AST[] forMap(Map m) {
					for (int i = 0; i < m.vars().length; i++){
						if (!context.add(m.vars()[i])){
							throw new SyntaxException(m.vars()[i] + " is repeated in map");
						}
					}
					context.flush();
					AST[] visRes = new AST[1];
					visRes[0] = m.body();
					return visRes;
				}
		
				@Override
				public AST[] forIf(If i) {
					AST[] visRes = new AST[3];
					visRes[0] = i.test();
					visRes[1] = i.conseq();
					visRes[2] = i.alt();
					return visRes;
				}
		
				@Override
				public AST[] forLet(Let l) {
					int i;
					AST[] visRes = new AST[l.defs().length + 1];
					for (i = 0; i < l.defs().length; i++) {
						if (!context.add(l.defs()[i].lhs())) {
							throw new SyntaxException(l.defs()[i].lhs() + " is repeated in let");
						}
						visRes[i] = l.defs()[i].rhs();
					}
					context.flush();
					visRes[i] = l.body();
					return visRes;
				}
				  
			  })));
		  }
	  }
	  return;
  }
  
  /* Top-level Value Cons Eval */
  
  /** Parses and CBV interprets the input embeded in parser */
  public JamVal valueValue() {
    AST prog = parser.parse();
    contextCheck(prog, null);
    return prog.accept(valueValueEvalVisitor);
  }
  
  /** Parses and CBNm interprets the input embeded in parser */
  public JamVal nameValue() {
    AST prog = parser.parse();
    contextCheck(prog, null);
    return prog.accept(nameValueEvalVisitor);
  }
  
  /** Parses and CBNd interprets the input embeded in parser */
  public JamVal needValue() {
    AST prog = parser.parse();
    contextCheck(prog, null);
    return prog.accept(needValueEvalVisitor);
  }
 
  /** CBV Interprets prog with respect to symbols in lexer */
  public JamVal valueValue(AST prog) {
	  contextCheck(prog, null);
	  return prog.accept(valueValueEvalVisitor);
  }
  
  /** CBName Interprets prog with respect to symbols in lexer */
  public JamVal nameValue(AST prog) {
	  contextCheck(prog, null);
	  return prog.accept(nameValueEvalVisitor); 
  }
  
  /** CBNeed Interprets prog with respect to symbols in lexer */
  public JamVal needValue(AST prog) {
	  contextCheck(prog, null);
	  return prog.accept(needValueEvalVisitor);
  }
  
  /* Top-level Name Cons Eval */
  
  /** Parses and CBV interprets the input embeded in parser */
  public JamVal valueName() {
    AST prog = parser.parse();
    contextCheck(prog, null);
    return prog.accept(valueNameEvalVisitor);
  }
  
  /** Parses and CBNm interprets the input embeded in parser */
  public JamVal nameName() {
    AST prog = parser.parse();
    contextCheck(prog, null);
    return prog.accept(nameNameEvalVisitor);
  }
  
  /** Parses and CBNd interprets the input embeded in parser */
  public JamVal needName() {
    AST prog = parser.parse();
    contextCheck(prog, null);
    return prog.accept(needNameEvalVisitor);
  }
 
  /** CBV Interprets prog with respect to symbols in lexer */
  public JamVal valueName(AST prog) {
	  contextCheck(prog, null);
	  return prog.accept(valueNameEvalVisitor);
  }
  
  /** CBName Interprets prog with respect to symbols in lexer */
  public JamVal nameName(AST prog) {
	  contextCheck(prog, null);
	  return prog.accept(nameNameEvalVisitor); 
  }
  
  /** CBNeed Interprets prog with respect to symbols in lexer */
  public JamVal needName(AST prog) {
	  contextCheck(prog, null);
	  return prog.accept(needNameEvalVisitor);
  }
  
  /* Top-level Need Cons Eval */
  
  /** Parses and CBV interprets the input embeded in parser */
  public JamVal valueNeed() {
    AST prog = parser.parse();
    contextCheck(prog, null);
    return prog.accept(valueNeedEvalVisitor);
  }
  
  /** Parses and CBNm interprets the input embeded in parser */
  public JamVal nameNeed() {
    AST prog = parser.parse();
    contextCheck(prog, null);
    return prog.accept(nameNeedEvalVisitor);
  }
  
  /** Parses and CBNd interprets the input embeded in parser */
  public JamVal needNeed() {
    AST prog = parser.parse();
    contextCheck(prog, null);
    return prog.accept(needNeedEvalVisitor);
  }
 
  /** CBV Interprets prog with respect to symbols in lexer */
  public JamVal valueNeed(AST prog) {
	  contextCheck(prog, null);
	  return prog.accept(valueNeedEvalVisitor);
  }
  
  /** CBName Interprets prog with respect to symbols in lexer */
  public JamVal nameNeed(AST prog) {
	  contextCheck(prog, null);
	  return prog.accept(nameNeedEvalVisitor); 
  }
  
  /** CBNeed Interprets prog with respect to symbols in lexer */
  public JamVal needNeed(AST prog) {
	  contextCheck(prog, null);
	  return prog.accept(needNeedEvalVisitor);
  }
  
  /** A class representing an unevaluated expresssion (together with the corresponding evaluator). */
  static class Suspension {
    private AST exp;
    private EvalVisitor ev;  
    
    Suspension(AST a, EvalVisitor e) { exp = a; ev = e; }
    
    public AST exp() { return exp; }
    public EvalVisitor ev() { return ev; }
    public void putEv(EvalVisitor e) { ev = e; }
    
    /** Evaluates this suspension. */
    JamVal eval() { 
      // System.err.println("eval() called on the susp with AST = " + exp);
      return exp.accept(ev);  } 
    
    public String toString() { return "<" + exp + ", " + ev + ">"; }
  }
  
  /** Class representing a binding in CBV evaluation. */
  static class ValueBinding extends Binding {
    ValueBinding(Variable v, JamVal jv) { super(v, jv); }
    public JamVal value() { return value; }
    public String toString() { return "[" + var + ", " + value + "]"; }
  }
  
  /** Class representing a binding in CBNm evaluation. */
  static class NameBinding extends Binding {
    protected Suspension susp;
    NameBinding(Variable v, Suspension s) { 
      super(v,null);
      susp = s;
    }
    public JamVal value() { return susp.eval(); }
    public String toString() { return "[" + var + ", " + susp + "]"; }
  }
  
  /** Class representing a binding in CBNd evaluation. */
  static class NeedBinding extends NameBinding {
    NeedBinding(Variable v, Suspension s) { super(v,s); }
    public JamVal value() {
      if (value == null) {  // a legitimate JamVal CANNOT be null
        setValue(susp.eval());
        susp = null;  // release susp object for GC!
      }
      return value;
    }
    public void setValue(JamVal v) { value = v; }
    public String toString() { return "[" + var + ", " + value + ", " + susp + "]"; }
  }
  
  /** Visitor class implementing a lookup method on environments.
    * @return value() for variable var for both lazy and eager environments. */
  static class LookupVisitor implements PureListVisitor<Binding,JamVal> {

    Variable var;   // the lexer guarantees that there is only one Variable for a given name
    
    LookupVisitor(Variable v) { var = v; }
    
    /* Visitor methods. */
    public JamVal forEmpty(Empty<Binding> e) { throw new EvalException("variable " + var + " is unbound"); }
    
    public JamVal forCons(Cons<Binding> c) {
      Binding b = c.first();
      if (var == b.var()) return b.value();
      return c.rest().accept(this);
    }
  }
 
  /** The interface supported by various evaluation policies (CBV, CBNm, CBNd) for map applications and variable 
    * lookups. The EvalVisitor parameter appears in each method because the environment is carried by an EvalVisitor.
    * Hence as an EvalPolicy is used to interpret an expression, The passed EvalVisitor will change (!) as the
    * environment changes.  An EvalPolicy should NOT contain an EvalVisitor field.
    */
  interface EvalPolicy {
    /** Evaluates the let construct composed of var, exps, and body */
    JamVal letEval(Variable[] vars, AST[] exps, AST body, EvalVisitor ev);
    
    /** Constructs a UnOpVisitor with the specified evaluated argument */
    UnOpVisitor<JamVal> newUnOpVisitor(JamVal arg);
    
    /** Constructs a BinOpVisitor with the specified unevaluated arguments and EvalVisitor */
    BinOpVisitor<JamVal> newBinOpVisitor(AST arg1, AST arg2, EvalVisitor ev);
    
    /** Constructs a JamFunVisitor with the specified array of unevaluated arguments and EvalVisitor */
    JamFunVisitor<JamVal> newFunVisitor(AST args[], EvalVisitor ev);

    /** Constructs the appropriate binding object for this, binding var to ast in the evaluator ev */
    Binding newBinding(Variable var, AST ast, EvalVisitor ev);
    
    /** Constructs the appropriate cons object for this, incorporating the correct consEvalPolicy */
    JamCons newCons(AST f, AST r);
  }
  
  /** An EvalVisitor class where details of behavior are determined by an embedded EvalPolicy. */
  static class FlexEvalVisitor implements EvalVisitor {
    
    /* The code in this class assumes that:
     * * OpTokens are unique; 
     * * Variable objects are unique: v1.name.equals(v.name) => v1 == v2; and
     * * The only objects used as boolean values are BoolConstant.TRUE and BoolConstant.FALSE.
     * Hence,  == can be used to compare Variable objects, OpTokens, and BoolConstants. */
    
    PureList<Binding> env;  // the embdedded environment
    EvalPolicy evalPolicy;  // the embedded EvalPolicy
    
    /** Recursive constructor. */
    private FlexEvalVisitor(PureList<Binding> e, EvalPolicy ep) { 
      env = e; 
      evalPolicy = ep;
    }
    
    /** Top level constructor. */
    public FlexEvalVisitor(EvalPolicy ep) { this(new Empty<Binding>(), ep); }
    
    /** factory method that constructs a new visitor of This class with environment env */
    public EvalVisitor newVisitor(PureList<Binding> e) { return new FlexEvalVisitor(e, evalPolicy); }
    
    /** Factory method that constructs a Binding of var to ast corresponding to this */
    public Binding newBinding(Variable var, AST ast) { return evalPolicy.newBinding(var, ast, this); }
    
    /** Getter for env field */
    public PureList<Binding> env() { return env; }
    
    /* EvalVisitor methods */
    public JamVal forBoolConstant(BoolConstant b) { return b; }
    public JamVal forIntConstant(IntConstant i) { return i; }
    public JamVal forNullConstant(NullConstant n) { return JamEmpty.ONLY; }
    public JamVal forVariable(Variable v) {  return env.accept(new LookupVisitor(v)); }
    
    public JamVal forPrimFun(PrimFun f) { return f; }
    
    public JamVal forUnOpApp(UnOpApp u) { 
      return u.rator().accept(evalPolicy.newUnOpVisitor(u.arg().accept(this)));
    }
    
    public JamVal forBinOpApp(BinOpApp b) {
      return b.rator().accept(evalPolicy.newBinOpVisitor(b.arg1(), b.arg2(), this));
    }
    
    public JamVal forApp(App a) {
      JamVal rator = a.rator().accept(this);
      if (rator instanceof JamFun) return ((JamFun) rator).accept(evalPolicy.newFunVisitor(a.args(), this));
      throw new EvalException(rator + " appears at head of application " + a + " but it is not a valid function");
    }
    
    public JamVal forMap(Map m) { return new JamClosure(m,env); }
    
    public JamVal forIf(If i) {
      JamVal test = i.test().accept(this);
      if (! (test instanceof BoolConstant)) throw new EvalException("non Boolean " + test + " used as test in if");
      if (test == BoolConstant.TRUE) return i.conseq().accept(this);
      return i.alt().accept(this);
    }
    
    public JamVal forLet(Let l) {
      Def[] defs = l.defs();
      int n = defs.length;
      
      Variable[] vars = new Variable[n];
      for (int i = 0; i < n; i++) vars[i] = defs[i].lhs();
      
      AST[] exps =  new AST[n];
      for (int i = 0; i < n; i++) exps[i] = defs[i].rhs();
      
      return evalPolicy.letEval(vars, exps, l.body(), this);
    }
  }
  
  /** Top-level FlexVisitors implementing CBV, CBNm, and CBNd evaluation. */
  static EvalVisitor valueValueEvalVisitor = new FlexEvalVisitor(CallByValueValue.ONLY);
  static EvalVisitor nameValueEvalVisitor = new FlexEvalVisitor(CallByNameValue.ONLY);
  static EvalVisitor needValueEvalVisitor = new FlexEvalVisitor(CallByNeedValue.ONLY);
  
  static EvalVisitor valueNameEvalVisitor = new FlexEvalVisitor(CallByValueName.ONLY);
  static EvalVisitor nameNameEvalVisitor = new FlexEvalVisitor(CallByNameName.ONLY);
  static EvalVisitor needNameEvalVisitor = new FlexEvalVisitor(CallByNeedName.ONLY);
  
  static EvalVisitor valueNeedEvalVisitor = new FlexEvalVisitor(CallByValueNeed.ONLY);
  static EvalVisitor nameNeedEvalVisitor = new FlexEvalVisitor(CallByNameNeed.ONLY);
  static EvalVisitor needNeedEvalVisitor = new FlexEvalVisitor(CallByNeedNeed.ONLY);
  
  /** Class that implements the evaluation of function applications given the embedded arguments and evalVisitor. */
  static class StandardFunVisitor implements JamFunVisitor<JamVal> {
    
    /** Unevaluated arguments */
    AST[] args;
    
    /** Evaluation visitor */
    EvalVisitor evalVisitor;
    
    /** PrimFun visitor */
    PrimFunVisitorFactory primFunFactory;
    
    StandardFunVisitor(AST[] asts, EvalVisitor ev, PrimFunVisitorFactory pff) {
      args = asts;
      evalVisitor = ev;
      primFunFactory = pff;
    }
    
    /* Visitor methods. */
    public JamVal forJamClosure(JamClosure closure) {
      Map map = closure.body();
      
      int n = args.length;
      Variable[] vars = map.vars();
      if (vars.length != n) 
        throw new EvalException("closure " + closure + " applied to " + n + " arguments");
      
      // construct newEnv for JamClosure body using JamClosure env
      PureList<Binding> newEnv = closure.env();
      for (int i = n-1; i >= 0; i--) 
        newEnv = newEnv.cons(evalVisitor.newBinding(vars[i], args[i]));
      return map.body().accept(evalVisitor.newVisitor(newEnv));
    }
   
    public JamVal forPrimFun(PrimFun primFun) {
      int n = args.length;
      /* JamVal[] vals = new JamVal[n];
       for (int i = 0; i < n; i++)       
       vals[i] = args[i].accept(evalVisitor); */
      return primFun.accept(primFunFactory.newVisitor(evalVisitor, args));
    }
  }
  
  abstract static class CommonEvalPolicy implements EvalPolicy {
    
    public JamVal letEval(Variable[] vars, AST[] exps, AST body, EvalVisitor evalVisitor) {
      /* let semantics */
      
      int n = vars.length;
   
      // construct newEnv for Let body; vars are bound to values of corresponding exps using evalVisitor
      PureList<Binding> newEnv = evalVisitor.env();
      for (int i = n-1; i >= 0; i--) newEnv = newEnv.cons(evalVisitor.newBinding(vars[i], exps[i]));
      
      EvalVisitor newEvalVisitor = evalVisitor.newVisitor(newEnv);  
      
      return body.accept(newEvalVisitor);
    }
          
    public UnOpVisitor<JamVal> newUnOpVisitor(JamVal arg) {
      return new StandardUnOpVisitor(arg);
    }
    public BinOpVisitor<JamVal> newBinOpVisitor(AST arg1, AST arg2, EvalVisitor ev) {
      return new StandardBinOpVisitor(arg1, arg2, ev);
    }
    
    public JamFunVisitor<JamVal> newFunVisitor(AST[] args, EvalVisitor ev) {
      return new StandardFunVisitor(args, ev, StandardPrimFunFactory.ONLY);
    }
  }
  
  /** Substitution method for callByValue let-rec*/
  public static JamVal letRecEvalValue(Variable[] vars, AST[] exps, AST body, EvalVisitor evalVisitor) {
      /* let semantics */
      
      int n = vars.length;
      EvalVisitor newEvalVisitor = evalVisitor;
   
      // construct newEnv for Let body; vars are bound to values of corresponding exps using evalVisitor
      PureList<Binding> newEnv = evalVisitor.env();
      for (int i = n-1; i >= 0; i--) newEnv = newEnv.cons(evalVisitor.newBinding(vars[i], null));
      for (int i = 0; i < n; i++){
    	  newEnv = newEnv.cons(new ValueBinding(vars[i], letRecEvalValue(vars, exps, body, evalVisitor)));
      }
      newEvalVisitor = evalVisitor.newVisitor(newEnv);
        
      
      return body.accept(newEvalVisitor);
    }
  
  /** Substitution method for callByName and CallByNeed let-rec*/
  public static JamVal letRecEvalName(Variable[] vars, AST[] exps, AST body, EvalVisitor evalVisitor) {
      /* let semantics */
      
      int n = vars.length;
      EvalVisitor newEvalVisitor = evalVisitor;
   
      // construct newEnv for Let body; vars are bound to values of corresponding exps using evalVisitor
      PureList<Binding> newEnv = evalVisitor.env();
      for (int i = n-1; i >= 0; i--) newEnv = newEnv.cons(evalVisitor.newBinding(vars[i], exps[i]));
      //letRecEvalName(vars,exps,body,evalVisitor);
      newEvalVisitor = evalVisitor.newVisitor(newEnv);
        
      
      return body.accept(newEvalVisitor);
    }
     
  /* Value Cons Constructor Policies */
  
  static class CallByValueValue extends CommonEvalPolicy {
    
    public static final CallByValueValue ONLY = new CallByValueValue();
    private CallByValueValue() { }
    
    /** Inherited letEval works because newBinding method is customized! */
    
    /*public JamVal letEval(Variable[] vars, AST[] exps, AST body, EvalVisitor evalVisitor){
    	return letRecEvalValue(vars,exps,body,evalVisitor);
    }*
    
    /** Constructs binding of var to value of arg in ev */
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new ValueBinding(var, arg.accept(ev)); }
    public JamCons newCons(AST f, AST r) {return ConsEvalPolicyByValue.ONLY.newCons(f, r);}
  }
    
  static class CallByNameValue extends CommonEvalPolicy {
    public static final CallByNameValue ONLY = new CallByNameValue();
    private CallByNameValue() {}
    
    /** Inherited letEval works because newBinding method is customized! */
    
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new NameBinding(var, new Suspension(arg, ev)); }
    public JamCons newCons(AST f, AST r) {return ConsEvalPolicyByValue.ONLY.newCons(f, r);}
  }
  
  static class CallByNeedValue extends CommonEvalPolicy {
    public static final CallByNeedValue ONLY = new CallByNeedValue();
    private CallByNeedValue() {}
    
    /** Inherited letEval works because newBinding method is customized! */
    
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new NeedBinding(var, new Suspension(arg, ev)); }
    public JamCons newCons(AST f, AST r) {return ConsEvalPolicyByValue.ONLY.newCons(f, r);}
  }
  
  /* Name Cons Constructor Policies */
  
  static class CallByValueName extends CommonEvalPolicy {
    
    public static final CallByValueName ONLY = new CallByValueName();
    private CallByValueName() { }
    
    /** Inherited letEval works because newBinding method is customized! */
    
    /** Constructs binding of var to value of arg in ev */
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new ValueBinding(var, arg.accept(ev)); }
    public JamCons newCons(AST f, AST r) {return ConsEvalPolicyByName.ONLY.newCons(f, r);}
  }
    
  static class CallByNameName extends CommonEvalPolicy {
    public static final CallByNameName ONLY = new CallByNameName();
    private CallByNameName() {}
    
    /** Inherited letEval works because newBinding method is customized! */
    
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new NameBinding(var, new Suspension(arg, ev)); }
    public JamCons newCons(AST f, AST r) {return ConsEvalPolicyByName.ONLY.newCons(f, r);}
  }
  
  static class CallByNeedName extends CommonEvalPolicy {
    public static final CallByNeedName ONLY = new CallByNeedName();
    private CallByNeedName() {}
    
    /** Inherited letEval works because newBinding method is customized! */
    
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new NeedBinding(var, new Suspension(arg, ev)); }
    public JamCons newCons(AST f, AST r) {return ConsEvalPolicyByName.ONLY.newCons(f, r);}
  }

  
  /* Need Cons Constructor Policies */
  
  static class CallByValueNeed extends CommonEvalPolicy {
    
    public static final CallByValueNeed ONLY = new CallByValueNeed();
    private CallByValueNeed() { }
    
    /** Inherited letEval works because newBinding method is customized! */
    
    /** Constructs binding of var to value of arg in ev */
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new ValueBinding(var, arg.accept(ev)); }
    public JamCons newCons(AST f, AST r) {return ConsEvalPolicyByNeed.ONLY.newCons(f, r);}
  }
    
  static class CallByNameNeed extends CommonEvalPolicy {
    public static final CallByNameValue ONLY = new CallByNameValue();
    private CallByNameNeed() {}
    
    /** Inherited letEval works because newBinding method is customized! */
    
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new NameBinding(var, new Suspension(arg, ev)); }
    public JamCons newCons(AST f, AST r) {return ConsEvalPolicyByNeed.ONLY.newCons(f, r);}
  }
  
  static class CallByNeedNeed extends CommonEvalPolicy {
    public static final CallByNeedNeed ONLY = new CallByNeedNeed();
    private CallByNeedNeed() {}
    
    /** Inherited letEval works because newBinding method is customized! */
    
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new NeedBinding(var, new Suspension(arg, ev)); }
    public JamCons newCons(AST f, AST r) {return ConsEvalPolicyByNeed.ONLY.newCons(f, r);}
  }

  
  static class StandardUnOpVisitor implements UnOpVisitor<JamVal> {
    private JamVal val;
    StandardUnOpVisitor(JamVal jv) { val = jv; }
    
    private IntConstant checkInteger(UnOp op) {
      if (val instanceof IntConstant) return (IntConstant) val;
      throw new EvalException("Unary operator `" + op + "' applied to non-integer " + val);
    }
    
    private BoolConstant checkBoolean(UnOp op) {
      if (val instanceof BoolConstant) return (BoolConstant) val;
      throw new EvalException("Unary operator `" + op + "' applied to non-boolean " + val);
    }

    public JamVal forUnOpPlus(UnOpPlus op) { return checkInteger(op); }
    public JamVal forUnOpMinus(UnOpMinus op) { 
      return new IntConstant(- checkInteger(op).value()); 
    }
    public JamVal forOpNot(OpNot op) { return checkBoolean(op).not(); }
    // public JamVal forOpBang(OpBang op) { return ... ; }  // Supports addition of ref cells to Jam
    // public JamVal forOpRef(OpRef op) { return ... ; }    // Supports addition of ref cells to Jam
  }
  
  static class StandardBinOpVisitor implements BinOpVisitor<JamVal> { 
    private AST arg1, arg2;
    private EvalVisitor evalVisitor;
    
    StandardBinOpVisitor(AST a1, AST a2, EvalVisitor ev) { arg1 = a1; arg2 = a2; evalVisitor = ev; }
    
    private IntConstant evalIntegerArg(AST arg, BinOp b) {
      JamVal val = arg.accept(evalVisitor);
      if (val instanceof IntConstant) return (IntConstant) val;
      throw new EvalException("Binary operator `" + b + "' applied to non-integer " + val);
    }
    
    private BoolConstant evalBooleanArg(AST arg, BinOp b) {
      JamVal val = arg.accept(evalVisitor);
      if (val instanceof BoolConstant) return (BoolConstant) val;
      throw new EvalException("Binary operator `" + b + "' applied to non-boolean " + val);
    }
    
    public JamVal forBinOpPlus(BinOpPlus op) {
      return new IntConstant(evalIntegerArg(arg1,op).value() + evalIntegerArg(arg2,op).value());
    }
    public JamVal forBinOpMinus(BinOpMinus op) {
      return new IntConstant(evalIntegerArg(arg1,op).value() - evalIntegerArg(arg2,op).value());
    }
    
    public JamVal forOpTimes(OpTimes op) {
      return new IntConstant(evalIntegerArg(arg1,op).value() * evalIntegerArg(arg2,op).value());
    }
    
    public JamVal forOpDivide(OpDivide op) {
      return new IntConstant(evalIntegerArg(arg1,op).value() / evalIntegerArg(arg2,op).value());
    }
    
    public JamVal forOpEquals(OpEquals op) {
      return BoolConstant.toBoolConstant(arg1.accept(evalVisitor).equals(arg2.accept(evalVisitor)));
    }
    
    public JamVal forOpNotEquals(OpNotEquals op) {
      return BoolConstant.toBoolConstant(! arg1.accept(evalVisitor).equals(arg2.accept(evalVisitor)));
    }
    
    public JamVal forOpLessThan(OpLessThan op) {
      return BoolConstant.toBoolConstant(evalIntegerArg(arg1,op).value() < evalIntegerArg(arg2,op).value());
    }
    
    public JamVal forOpGreaterThan(OpGreaterThan op) {
      return BoolConstant.toBoolConstant(evalIntegerArg(arg1,op).value() > evalIntegerArg(arg2,op).value());
    }
    
    public JamVal forOpLessThanEquals(OpLessThanEquals op) {
      return BoolConstant.toBoolConstant(evalIntegerArg(arg1,op).value() <= evalIntegerArg(arg2,op).value());
    }
    
    public JamVal forOpGreaterThanEquals(OpGreaterThanEquals op) {
      return BoolConstant.toBoolConstant(evalIntegerArg(arg1,op).value() >= evalIntegerArg(arg2,op).value());
    }
    
    public JamVal forOpAnd(OpAnd op) {
      BoolConstant b1 = evalBooleanArg(arg1,op);
      if (b1 == BoolConstant.FALSE) return BoolConstant.FALSE;
      return evalBooleanArg(arg2,op);
    }
    public JamVal forOpOr(OpOr op) {
      BoolConstant b1 = evalBooleanArg(arg1,op);
      if (b1 == BoolConstant.TRUE) return BoolConstant.TRUE;
      return evalBooleanArg(arg2,op);
    }
    // public JamVal forOpGets(OpGets op) { return ... ; }  // Supports addition of ref cells to Jam
  }
  
  /** Interface for a factory that constructs a PrimFunVisitor with a given EvalVisitor and args. */
  interface PrimFunVisitorFactory {
    PrimFunVisitor<JamVal> newVisitor(EvalVisitor ev, AST[] args);
  }
  
  static class StandardPrimFunFactory implements PrimFunVisitorFactory {
    
    public static StandardPrimFunFactory ONLY = new StandardPrimFunFactory();
    private StandardPrimFunFactory() {}
    
    public PrimFunVisitor<JamVal> newVisitor(EvalVisitor ev, AST[] args) {
      return new StandardPrimFunVisitor(ev, args);
    }
    
    static private class StandardPrimFunVisitor implements PrimFunVisitor<JamVal> {
      
      EvalVisitor evalVisitor;
      AST[] args;
            
      StandardPrimFunVisitor(EvalVisitor ev, AST[] asts) {
    	  evalVisitor = ev;
    	  args = asts;
    }
          
      private JamVal[] evalArgs() {
        int n = args.length;
        JamVal[] vals = new JamVal[n];
        for (int i=0; i < n; i++) vals[i] = args[i].accept(evalVisitor);
        return vals;
      }
      
      
      private JamVal primFunError(String fn) {
        throw new EvalException("Primitive function `" + fn + "' applied to " + 
                                args.length + " arguments");
      }
  
      private JamCons evalJamConsArg(AST arg, String fun) {
        JamVal val = arg.accept(evalVisitor);
        if (val instanceof JamCons) return (JamCons) val;
                                throw new EvalException("Primitive function `" + fun + "' applied to argument " + val + 
                                        " that is not a JamCons");
      }
      
      public JamVal forFunctionPPrim() {
        JamVal[] vals = evalArgs();
        if (vals.length != 1) return primFunError("function?");
        return BoolConstant.toBoolConstant(vals[0] instanceof JamFun);
      }
      
      public JamVal forNumberPPrim() {
        JamVal[] vals = evalArgs();
        if (vals.length != 1) return primFunError("number?");
        return BoolConstant.toBoolConstant(vals[0] instanceof IntConstant);
      }
      
      public JamVal forListPPrim() {
        JamVal[] vals = evalArgs();
        if (vals.length != 1) return primFunError("list?");
        return BoolConstant.toBoolConstant(vals[0] instanceof JamList);
      }
      
      public JamVal forConsPPrim() {
        JamVal[] vals = evalArgs();
        if (vals.length != 1) return primFunError("cons?");
        return BoolConstant.toBoolConstant(vals[0] instanceof JamCons);
      }
      
      public JamVal forNullPPrim() {
        JamVal[] vals = evalArgs();
        if (vals.length != 1) return primFunError("null?");
        return BoolConstant.toBoolConstant(vals[0] instanceof JamEmpty);
      }
      
      public JamVal forConsPrim() {
        JamVal[] vals = evalArgs();
        if (vals.length != 2) return primFunError("cons");
        if (vals[1] instanceof JamList) return new JamCons(vals[0], (JamList) vals[1]);
        throw new EvalException("Second argument " + vals[1] + " to `cons' is not a JamList");
      }
      
      public JamVal forArityPrim() { 
        JamVal[] vals = evalArgs();
        if (vals.length != 1) return primFunError("arity");
        if (! (vals[0] instanceof JamFun) ) throw new EvalException("arity applied to argument " +
                                                                    vals[0]);
        return ((JamFun) vals[0]).accept(ArityVisitor.ONLY);
      }
      
      public JamVal forFirstPrim() { return evalJamConsArg(args[0], "first").first(); }
      public JamVal forRestPrim() { return evalJamConsArg(args[0], "rest").rest(); }
      
      /** Visitor class that implements the Jam arity method. */
      static private class ArityVisitor implements JamFunVisitor<IntConstant> {
        static public ArityVisitor ONLY = new ArityVisitor();
        private ArityVisitor() {}
        public IntConstant forJamClosure(JamClosure jc) { return new IntConstant(jc.body().vars().length); }
        public IntConstant forPrimFun(PrimFun jpf) { return jpf.accept(PrimArityVisitor.ONLY); }
      }
      
      /** Visitor class that implements the Jam arity method on primitive functions. */  
      static private class PrimArityVisitor implements PrimFunVisitor<IntConstant> {
        static public PrimArityVisitor ONLY = new PrimArityVisitor();
        private PrimArityVisitor() {}
        
        public IntConstant forFunctionPPrim() { return new IntConstant(1); }
        public IntConstant forNumberPPrim() { return new IntConstant(1); }
        public IntConstant forListPPrim() { return new IntConstant(1); }
        public IntConstant forConsPPrim() { return new IntConstant(1); }
        public IntConstant forNullPPrim() { return new IntConstant(1); }
        public IntConstant forArityPrim() { return new IntConstant(1); }
        public IntConstant forConsPrim() { return new IntConstant(2); }
        public IntConstant forFirstPrim() { return new IntConstant(1); }
        public IntConstant forRestPrim() { return new IntConstant(1); }
      }
    }
  }
  
  /** Interface for evaluating the Cons function */
  interface ConsEvalPolicy {
	  JamCons newCons(AST f, AST r, EvalVisitor ev);
  }
  
  /** Ultimately unnecessary intermediate abstract class */
  abstract static class ConsEvalPolicyCommon implements ConsEvalPolicy {
	  public JamCons newCons(AST f, AST r) {return null;}
  }
  
  /** Evaluation policy of Cons according to callByValue */
  static class ConsEvalPolicyByValue extends ConsEvalPolicyCommon {
	  public static final ConsEvalPolicyByValue ONLY = new ConsEvalPolicyByValue();
	  public ConsEvalPolicyByValue() {}
	  
	  public JamCons newCons(AST f, AST r, EvalVisitor ev) {
		  JamVal first = f.accept(ev);
		  JamVal rest = r.accept(ev);
		  if (rest instanceof JamList) {
		  	return new JamCons(first, (JamList) rest);
		  } else {
			  throw new EvalException("expected JamList in rest of cons: " + rest.toString());
		  }
	  }
  }
  
  /** Evaluation policy of Cons according to callByName */
  static class ConsEvalPolicyByName extends ConsEvalPolicyCommon {
	  public static final ConsEvalPolicyByName ONLY = new ConsEvalPolicyByName();
	  private ConsEvalPolicyByName() {}
	  
	  public JamCons newCons(AST f, AST r, EvalVisitor ev) {
		  return new JamLazyNameCons(f, r, ev);
	  }
  }

  /** Evaluation policy of Cons according to callByNeed */
  static class ConsEvalPolicyByNeed extends ConsEvalPolicyCommon {
	  public static final ConsEvalPolicyByNeed ONLY = new ConsEvalPolicyByNeed();
	  private ConsEvalPolicyByNeed() {}
	  
	  public JamCons newCons(AST f, AST r, EvalVisitor ev) {
		  return new JamLazyNeedCons(f, r, ev);
	  }
  }
  
  /** Extension of  JamCons class evaluated lazily by name */
  static class JamLazyNameCons extends JamCons {
	  Suspension fSus;
	  Suspension rSus;
	  
	  JamLazyNameCons(AST f, AST r, EvalVisitor ev) {
		  super(null, null);
		  fSus = new Suspension(f, ev);
		  rSus = new Suspension(r, ev);
	  }
	  
	  @Override
	  public JamVal first(){
		  return fSus.eval();
	  }
	  
	  @Override
	  public JamList rest() {
		  JamVal res = rSus.eval();
		  if (res instanceof JamList) {
			  return (JamList) res;
		  } else {
			  throw new EvalException("expected JamList in rest of cons: " + res.toString());
		  }
	  }
	  
	  @Override
	  public String toString() {
		  JamVal res = rSus.eval();
		  if (res instanceof JamEmpty)
			  return "(" + fSus.eval() + ((JamEmpty)res).toStringHelp() + ")";
		  else if (res instanceof JamLazyNameCons)
			  return "(" + fSus.eval() + ((JamLazyNameCons)res).toStringHelp(1000) + ")";
		  else 
			  throw new EvalException("cons toString expected JamList in rest: " + res.toString()); 
	  }
	  
	  public String toStringHelp(int i) {
		  if (i > 0) {
			  JamVal res = rSus.eval();
			  if (res instanceof JamEmpty)
				  return "(" + fSus.eval() + ((JamEmpty)res).toStringHelp() + ")";
			  else if (res instanceof JamLazyNameCons)
				  return "(" + fSus.eval() + ((JamLazyNameCons)res).toStringHelp(i - 1) + ")";
			  else
				  throw new EvalException("cons toString expected JamList in rest: " + res.toString());
		  } else
			  return "...";
	  }
  }
  
  /** Extension of  JamCons class evaluated lazily by need */
  static class JamLazyNeedCons extends JamCons {
	  Suspension fSus;
	  Suspension rSus;
	  
	  JamVal fRes = null;
	  JamVal rRes = null;
	  
	  JamLazyNeedCons(AST f, AST r, EvalVisitor ev) {
		  super(null, null);
		  fSus = new Suspension(f, ev);
		  rSus = new Suspension(r, ev);
	  }
	  
	  @Override
	  public JamVal first(){
		  if (fRes == null) { fRes = fSus.eval(); }
		  return fRes;
	  }
	  
	  @Override
	  public JamList rest() {
		  if (rRes == null) { rRes = rSus.eval(); }
		  if (rRes instanceof JamList) {
			  return (JamList) rRes;
		  } else {
			  throw new EvalException("expected JamList in rest of cons: " + rRes.toString());
		  }
	  }
	  
	  @Override
	  public String toString() {
		  if (fRes == null) { fRes = fSus.eval(); }
		  if (rRes == null) { rRes = rSus.eval(); }
		  if (rRes instanceof JamEmpty)
			  return "(" + fRes + ((JamEmpty)rRes).toStringHelp() + ")";
		  else if (rRes instanceof JamLazyNeedCons)
			  return "(" + fRes + ((JamLazyNeedCons)rRes).toStringHelp(1000) + ")";
		  else 
			  throw new EvalException("cons toString expected JamList in rest: " + rRes.toString()); 
	  }
	  
	  public String toStringHelp(int i) {
		  if (i > 0) {
			  if (fRes == null) { fRes = fSus.eval(); }
			  if (rRes == null) { rRes = rSus.eval(); }
			  if (rRes instanceof JamEmpty)
				  return "(" + fRes + ((JamEmpty)rRes).toStringHelp() + ")";
			  else if (rRes instanceof JamLazyNeedCons)
				  return "(" + fRes + ((JamLazyNeedCons)rRes).toStringHelp(i - 1) + ")";
			  else
				  throw new EvalException("cons toString expected JamList in rest: " + rRes.toString());
		  } else
			  return "...";
	  }
  }
  
  public static void main(String[] args) throws IOException {
    
    Parser p;
    if (args.length == 0) {
      System.out.println("Usage: java Interpreter <filename> { value | name | need } { value | name | need }");
      return;
    }
    p = new Parser(args[0]);
    AST prog = p.parse();
    System.out.println("AST is: " + prog);
    Interpreter i = new Interpreter(p);
    if (args.length == 1) {
      System.out.println("Call-by-value Answer is: " + i.valueValue(prog));
      System.out.println("Call-by-name Answer is: " + i.nameValue(prog));
      System.out.println("Call-by-need Answer is: " + i.needValue(prog));
    } else if (args.length == 2) {
	    if (args[1].equals("value")) {
	      System.out.println("Call-by-value Answer is: " + i.valueValue(prog));
	    }
	    else if (args[1].equals("need")) {
	      System.out.println("Call-by-need Answer is: " + i.needValue(prog));
	    }
	    else 
	      System.out.println("Call-by-name Answer is: " + i.nameValue(prog));
    } else {
    	if (args[1].equals("value")) {
    		if (args[2].equals("value")) {
    	  	      System.out.println("valueValue Answer is: " + i.valueValue(prog));
    	  	    }
    	  	    else if (args[2].equals("need")) {
    	  	      System.out.println("valueNeed Answer is: " + i.valueNeed(prog));
    	  	    }
    	  	    else 
    	  	      System.out.println("valueName Answer is: " + i.valueName(prog));
    	    }
    		else if (args[1].equals("need")) {
	  	    	if (args[2].equals("value")) {
	  	  	      System.out.println("needValue Answer is: " + i.needValue(prog));
	  	  	    }
	  	  	    else if (args[2].equals("need")) {
	  	  	      System.out.println("needNeed Answer is: " + i.needNeed(prog));
	  	  	    }
	  	  	    else 
	  	  	      System.out.println("needName Answer is: " + i.needName(prog));
  	    }
  	    else 
	    	if (args[2].equals("value")) {
	  	      System.out.println("nameValue Answer is: " + i.nameValue(prog));
	  	    }
	  	    else if (args[2].equals("need")) {
	  	      System.out.println("nameNeed Answer is: " + i.nameNeed(prog));
	  	    }
	  	    else 
	  	      System.out.println("nameName Answer is: " + i.nameName(prog));
    }
  }
}

/** Exception thrown during evaluation at runtime */
class EvalException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1962023094252778366L;

	EvalException(String msg) { super(msg); }
}

/** Exception thrown during context sensitive pass */
class SyntaxException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1962023094252778366L;

	SyntaxException(String msg) { super(msg); }
}
