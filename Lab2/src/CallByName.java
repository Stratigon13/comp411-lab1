/**
 * Created by xiaozheng on 2/12/16.
 */
class CallByNameBinding extends Binding {
    AST ast;
    InterpreterHelper eval;
    CallByNameBinding(Variable v, JamVal jv, AST astV, InterpreterHelper e) {
        super(v, jv);
        ast = astV;
        eval = e;
    }
    @Override
    public JamVal value() {
        return ast.accept(eval);

    }
}



class CallByNameProcessor implements CallByInterface {

    @Override
    public JamVal lookup(PureList<Binding> env, Variable v) {
        return env.accept(new PureListLookupVisitor(v));
    }

    @Override
    public Binding binding(Variable variable, AST ast, InterpreterHelper eval) {
        return new CallByNameBinding(variable, null, ast, eval);
    }

}