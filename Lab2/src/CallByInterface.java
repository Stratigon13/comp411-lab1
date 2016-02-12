/**
 * Created by xiaozheng on 2/11/16.
 */
interface CallByInterface {

    public JamVal lookup(PureList<Binding> env, Variable v);

    public Binding binding(Variable variable, AST ast, InterpreterHelper eval);

}




