/**
 * Created by xiaozheng on 2/11/16.
 */
class CallByNeedBinding extends Binding {
    AST ast;
    InterpreterHelper eval;
    CallByNeedBinding(Variable v, JamVal jv, AST astV, InterpreterHelper e) {
        super(v, jv);
        ast = astV;
        eval = e;
    }
    @Override
    public JamVal value() {
        if (this.value == null){
            value =  ast.accept(eval);
        }
        return value;
    }
}


class CallByNeed implements CallByInterface {

    @Override
    public JamVal lookup(PureList<Binding> env, Variable v) {
        return (JamVal)env.accept(new PureListLookupVisitor(v));
    }

    @Override
    public Binding binding(Variable variable, AST ast, InterpreterHelper eval) {
        return new CallByNeedBinding(variable, null, ast, eval);
    }

}