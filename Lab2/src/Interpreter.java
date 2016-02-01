import java.io.IOException;
import java.io.Reader;

/**
 * Created by xiaozheng on 1/31/16.
 */

public class Interpreter {

    /** file Interpreter.java **/
    class EvalException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 5213575105993689031L;

		EvalException(String msg) { super(msg); }
    }

    Interpreter(String fileName) throws IOException{
    	//TODO
    }
    Interpreter(Reader reader){
    	//TODO
    }

    public JamVal callByValue() {
    	//TODO
    	return new JamVal();
    }
    public JamVal callByName()  {
    	//TODO
    	return new JamVal();
    }
    public JamVal callByNeed()  {
    	//TODO
    	return new JamVal();
    }

}
