/**
 * Created by xiaozheng on 1/31/16.
 */
public class Interpreter {

    /** file Interpreter.java **/
    class EvalException extends RuntimeException {
        EvalException(String msg) { super(msg); }
    }

    class Interpreter {
        Interpreter(String fileName) throws IOException;
        Interpreter(Reader reader);

        public JamVal callByValue() { ... };
        public JamVal callByName()  { ... };
        public JamVal callByNeed()  { ... };
    }

}
