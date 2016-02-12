/**
 * Created by xiaozheng on 2/12/16.
 */
//Binding Implementation
class CallByValueBinding extends Binding {
    CallByValueBinding(Variable v, JamVal jv) {
        super(v, jv);
    }
}

 public class CallByValue implements CallByInterface {

    @Override
    public JamVal lookup(PureList<Binding> env, Variable v) {
        return env.accept(new PureListLookupVisitor(v));
    }

    @Override
    public Binding binding(Variable variable, AST ast, InterpreterHelper eval) {
        return new CallByValueBinding(variable, ast.accept(eval.copy()));
    }
}


