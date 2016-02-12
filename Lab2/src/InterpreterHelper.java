import java.util.Vector;

/**
 * Created by xiaozheng on 2/11/16.
 */

public class InterpreterHelper implements ASTVisitor<JamVal> {

    PureList<Binding> env;
    CallByInterface callby;

    public InterpreterHelper(PureList<Binding> e, CallByInterface cp ){
        env = e;
        callby = cp;
    }

    public InterpreterHelper copy()
    {
        return new InterpreterHelper(env, callby);
    }

    //Returns the BoolConstant
    @Override
    public JamVal forBoolConstant(BoolConstant b) {
        return b;
    }

    //Returns the IntConstant
    @Override
    public JamVal forIntConstant(IntConstant i) {
        return i;
    }

    //Returns the NullConstant
    @Override
    public JamVal forNullConstant(NullConstant n) {
        return JamEmpty.ONLY;
    }

    @Override
    public JamVal forJamEmpty(JamEmpty je) {
        return null;
    }

    //Returns the Variable
    @Override
    public JamVal forVariable(Variable v) {
        return callby.lookup(env, v);
    }

    //Returns the PrimFun
    @Override
    public JamVal forPrimFun(PrimFun f) {
        return f;
    }

    //Constructs a unary operation application.
    @Override
    public JamVal forUnOpApp(UnOpApp u) {
        UnOp op = u.rator();
        AST ast = u.arg();
        return op.accept(new UnopVisitorImpl(ast, this.copy()));
    }

    //Constructs a binary operation application
    @Override
    public JamVal forBinOpApp(BinOpApp b) {
        AST ast1 = b.arg1();
        AST ast2 = b.arg2();
        BinOp op = b.rator();
        return op.accept(new BinOpVisitorImpl(ast1, ast2, this.copy()));

    }


    @Override
    public JamVal forApp(App a) {

        AST rator = a.rator();
        AST[] args = a.args();
        JamVal val = rator.accept(this.copy());
        if (!(val instanceof JamFun)){
            throw new EvalException("We expect Function type, but " + rator.toString() + " is evaluated to " + val.toString());
        }
        JamFun fun = (JamFun) val;

        JamVal retVal = fun.accept(new JamFunVisitorImpl(args, this.copy()));
        System.out.println(" => " + "(App: " + a + "): " + retVal);
        return retVal;
    }

    @Override
    public JamVal forMap(Map m) {
        return new JamClosure(m, this.env);
    }

    @Override
    public JamVal forIf(If i) {
        JamVal val = i.test().accept(this.copy());
        if (!(val instanceof BoolConstant)){
            throw new EvalException("We expect Bool type, but " + i.test().toString() + " is evaluated to " + val.toString());
        }
        BoolConstant boolVal = (BoolConstant) val;
        if (boolVal.value()){
            return i.conseq().accept(this.copy());
        }
        return i.alt().accept(this.copy());
    }

    /**
     * let x=v,y=v2 in M
     * -->
     * (map x,y to M) (v, v2)
     *
     * @param l
     * @return
     */
    @Override
    public JamVal forLet(Let l) {

        //System.out.println("Let: " + l + ", Env:{ " + env.accept(new PureListPrintingVarVisitor()) + "}");

        Vector<AST> argsVector = new Vector<AST>();
        Vector<Variable> varsVector = new Vector<Variable>();
        AST[] args = null;
        Variable[] vars = null;
        for (Def def: l.defs()){varsVector.add(def.lhs());argsVector.add(def.rhs());}
        vars = varsVector.toArray(new Variable[varsVector.size()]);
        args = argsVector.toArray(new AST[argsVector.size()]);
        JamClosure closure = new JamClosure(new Map(vars, l.body()), this.env);
        JamVal retVal = closure.accept(new JamFunVisitorImpl(args, this.copy()));
        System.out.println(" => " + "(Let: " + l + ": " + retVal);
        return retVal;
    }

}

class UnopVisitorImpl implements UnOpVisitor<JamVal>
{

    JamVal val;
    Class<?> cls;

    public UnopVisitorImpl(AST ast, InterpreterHelper eval) {
        val = ast.accept(eval);
    }

    @Override
    public JamVal forUnOpPlus(UnOpPlus op) {
        IntConstant intVal = (IntConstant) val.accept(new JamValVisitorImpl(IntConstant.class));
        return intVal;
    }

    @Override
    public JamVal forUnOpMinus(UnOpMinus op) {
        IntConstant intVal = (IntConstant) val.accept(new JamValVisitorImpl(IntConstant.class));
        return new IntConstant(-intVal.value());
    }

    @Override
    public JamVal forOpTilde(OpNot op) {
        BoolConstant boolVal = (BoolConstant) val.accept(new JamValVisitorImpl(BoolConstant.class));
        if (boolVal == BoolConstant.TRUE) return BoolConstant.FALSE;
        return BoolConstant.TRUE;
    }

}

class BinOpVisitorImpl implements BinOpVisitor<JamVal>
{

    AST ast1, ast2;
    InterpreterHelper eval;
    JamVal val1, val2;

    public BinOpVisitorImpl(AST ast1, AST ast2, InterpreterHelper eval) {
        this.ast1 = ast1;
        this.ast2 = ast2;
        this.eval = eval;
    }


    @Override
    public JamVal forBinOpPlus(BinOpPlus op) {
        IntConstant val1 = (IntConstant)(ast1.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        IntConstant val2 = (IntConstant)(ast2.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        return new IntConstant(val1.value() + val2.value());
    }

    @Override
    public JamVal forBinOpMinus(BinOpMinus op) {
        IntConstant val1 = (IntConstant)(ast1.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        IntConstant val2 = (IntConstant)(ast2.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        return new IntConstant(val1.value() - val2.value());
    }

    @Override
    public JamVal forOpTimes(OpTimes op) {
        IntConstant val1 = (IntConstant)(ast1.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        IntConstant val2 = (IntConstant)(ast2.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        return new IntConstant(val1.value() * val2.value());
    }

    @Override
    public JamVal forOpDivide(OpDivide op) {
        IntConstant val1 = (IntConstant)(ast1.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        IntConstant val2 = (IntConstant)(ast2.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        if(val2.value() == 0){
            throw new EvalException("Cannot divide by zero");
        }
        return new IntConstant(val1.value() / val2.value());
    }

    @Override
    public JamVal forOpEquals(OpEquals op) {
        JamVal val1 = ast1.accept(eval.copy());
        JamVal val2 = ast2.accept(eval.copy());
        return BoolConstant.toBoolConstant(val1.equals(val2));
    }

    @Override
    public JamVal forOpNotEquals(OpNotEquals op) {
        JamVal val1 = ast1.accept(eval.copy());
        JamVal val2 = ast2.accept(eval.copy());
        return BoolConstant.toBoolConstant(!val1.equals(val2));
    }

    @Override
    public JamVal forOpLessThan(OpLessThan op) {
        IntConstant val1 = (IntConstant)(ast1.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        IntConstant val2 = (IntConstant)(ast2.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        return BoolConstant.toBoolConstant(val1.value() < val2.value());
    }

    @Override
    public JamVal forOpGreaterThan(OpGreaterThan op) {
        IntConstant val1 = (IntConstant)(ast1.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        IntConstant val2 = (IntConstant)(ast2.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        return BoolConstant.toBoolConstant(val1.value() > val2.value());
    }

    @Override
    public JamVal forOpLessThanEquals(OpLessThanEquals op) {
        IntConstant val1 = (IntConstant)(ast1.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        IntConstant val2 = (IntConstant)(ast2.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        return BoolConstant.toBoolConstant(val1.value() <= val2.value());
    }

    @Override
    public JamVal forOpGreaterThanEquals(OpGreaterThanEquals op) {
        IntConstant val1 = (IntConstant)(ast1.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        IntConstant val2 = (IntConstant)(ast2.accept(eval.copy()).accept(new JamValVisitorImpl(IntConstant.class)));
        return BoolConstant.toBoolConstant(val1.value() >= val2.value());
    }



    @Override
    public JamVal forOpAnd(OpAnd op) {
        BoolConstant val1 = (BoolConstant)(ast1.accept(eval.copy()).accept(new JamValVisitorImpl(BoolConstant.class)));
        if (!val1.value()) return BoolConstant.FALSE;
        BoolConstant val2 = (BoolConstant)(ast2.accept(eval.copy()).accept(new JamValVisitorImpl(BoolConstant.class)));
        return val2;
    }

    @Override
    public JamVal forOpOr(OpOr op) {
        BoolConstant val1 = (BoolConstant)(ast1.accept(eval.copy()).accept(new JamValVisitorImpl(BoolConstant.class)));
        if (!val1.value()) return BoolConstant.TRUE;
        BoolConstant val2 = (BoolConstant)(ast2.accept(eval.copy()).accept(new JamValVisitorImpl(BoolConstant.class)));
        return val2;
    }

}

class JamValVisitorImpl implements JamValVisitor<JamVal>
{

    Class<?> cls;

    public JamValVisitorImpl(Class<?> cls) {
        this.cls = cls;
    }

    @Override
    public JamVal forIntConstant(IntConstant ji) {
        if (cls == IntConstant.class){
            return ji;
        }
        throw new EvalException("Expecting " + cls + ". But " + ji.toString());
    }

    @Override
    public JamVal forBoolConstant(BoolConstant jb) {
        if (cls == BoolConstant.class) return jb;

        throw new EvalException("Expecting " + cls + ". But " + jb.toString());
    }

    @Override
    public JamVal forJamList(JamList jl) {
        if (cls == JamList.class) return jl;
        throw new EvalException("Expecting " + cls + ". But " + jl.toString());
    }

    @Override
    public JamVal forJamFun(JamFun jf) {
        if (cls == JamFun.class) return jf;
        throw new EvalException("Expecting " + cls + ". But " + jf.toString());
    }

}

class JamFunVisitorImpl implements JamFunVisitor<JamVal>
{

    AST[] args;
    InterpreterHelper eval;

    public JamFunVisitorImpl(AST[] args, InterpreterHelper eval) {
        //Initializes the object.
        this.args = args;
        this.eval = eval;
    }

    @Override
    public JamVal forJamClosure(JamClosure c) {
        PureList<Binding> closureEnv = c.env();
        Variable [] vars = c.body().vars();
        if (vars.length != args.length){
            throw new EvalException(c.toString() + "gets " + vars.length + "arguments, but " + args.length + " arguments are given");
        }
        for (int i = 0 ; i < vars.length; i++){
            Binding bind = eval.callby.binding(vars[i], args[i], eval.copy());
            closureEnv = closureEnv.cons(bind);
        }
        JamVal retVal = c.body().body().accept(new InterpreterHelper(closureEnv,  eval.callby));
        System.out.println("  => (JamClosure: " + c + " ): " + retVal);
        return retVal;
    }

    @Override
    public JamVal forPrimFun(PrimFun pf) {
        return pf.accept(new PrimFunVisitorImpl(args, eval.copy()));
    }

}

class PrimFunVisitorImpl implements PrimFunVisitor<JamVal>
{

    AST[] args;
    InterpreterHelper eval;

    public PrimFunVisitorImpl(AST[] args, InterpreterHelper eval) {
        this.args = args;
        this.eval = eval;
    }

    @Override
    public JamVal forFunctionPPrim() {
        if (args.length != 1) {
            throw new EvalException(FunctionPPrim.ONLY + " gets one argument, but " + args.length + " arguments");
        }
        JamVal val = args[0].accept(eval.copy());
        if (val instanceof JamFun ) return BoolConstant.TRUE;
        return BoolConstant.FALSE;
    }

    @Override
    public JamVal forNumberPPrim() {
        if (args.length != 1) {
            throw new EvalException(NumberPPrim.ONLY + " gets one argument, but " + args.length + " arguments");
        }
        JamVal val = args[0].accept(eval.copy());
        if (val instanceof IntConstant ) return BoolConstant.TRUE;
        return BoolConstant.FALSE;
    }

    @Override
    public JamVal forListPPrim() {
        if (args.length != 1) {
            throw new EvalException(ListPPrim.ONLY + " gets one argument, but " + args.length + " arguments");
        }
        JamVal val = args[0].accept(eval.copy());
        if (val instanceof JamList ) return BoolConstant.TRUE;
        return BoolConstant.FALSE;
    }

    @Override
    public JamVal forConsPPrim() {
        if (args.length != 1) {
            throw new EvalException(ConsPPrim.ONLY + " gets one argument, but " + args.length + " arguments");
        }
        JamVal val = args[0].accept(eval.copy());
        if (val instanceof JamCons ) return BoolConstant.TRUE;
        return BoolConstant.FALSE;
    }

    @Override
    public JamVal forNullPPrim() {
        if (args.length != 1) {
            throw new EvalException(NullPPrim.ONLY + " gets one argument, but " + args.length + " arguments");
        }
        JamVal val = args[0].accept(eval.copy());
        if (val instanceof JamEmpty ) return BoolConstant.TRUE;
        return BoolConstant.FALSE;
    }

    @Override
    public JamVal forArityPrim() {
        if (args.length != 1) {
            throw new EvalException(ArityPrim.ONLY + " gets one argument, but " + args.length + " arguments");
        }
        JamVal val = args[0].accept(eval.copy());
        if (!(val instanceof JamFun)){
            throw new EvalException(ArityPrim.ONLY + " only accept function type as a argument");
        }
        if (val instanceof PrimFun){
            if (val instanceof ConsPrim ) return new IntConstant(2);
            else return new IntConstant(1);
        }

        // Closure
        JamClosure jamClosure = (JamClosure) val;
        return new IntConstant(jamClosure.body().vars().length);
    }

    @Override
    public JamVal forConsPrim() {
        if (args.length != 2) {
            throw new EvalException(ConsPrim.ONLY + " gets two arguments, but " + args.length + " arguments");
        }
        JamVal val1 = args[0].accept(eval.copy());
        JamVal val2 = args[1].accept(eval.copy());
        if (!(val2 instanceof JamList)){
            throw new EvalException(ConsPrim.ONLY + "'s second argument should be a list, but " + val2.toString() + " value");
        }
        return new JamCons(val1, (JamList)val2);
    }

    @Override
    public JamVal forFirstPrim() {
        if (args.length != 1) {
            throw new EvalException(FirstPrim.ONLY + " gets one argument, but " + args.length + " arguments");
        }
        JamVal val = args[0].accept(eval.copy());
        if (!(val instanceof JamCons)){
            throw new EvalException(FirstPrim.ONLY + " gets a list created by cons(), but " + val.toString() + " value");
        }
        return ((JamCons)val).first();
    }

    @Override
    public JamVal forRestPrim() {
        if (args.length != 1) {
            throw new EvalException(RestPrim.ONLY + " gets one argument, but " + args.length + " arguments");
        }
        JamVal val = args[0].accept(eval.copy());
        if (!(val instanceof JamCons)){
            throw new EvalException(RestPrim.ONLY + " gets a list created by cons(), but " + val.toString() + " value");
        }
        return ((JamCons)val).rest();
    }

}
