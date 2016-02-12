/**
 * Created by xiaozheng on 2/11/16.
 */
interface CallByInterface {

    JamVal lookup(PureList<Binding> env, Variable v);

    Binding binding(Variable variable, AST ast, InterpreterHelper eval);

}




