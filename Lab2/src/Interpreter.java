import java.io.IOException;
import java.io.Reader;

/**
 * Created by xiaozheng on 1/31/16.
 */

public class Interpreter {
	
	AST parsedExp = null;

    /** file Interpreter.java **/
    class EvalException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 5213575105993689031L;

		EvalException(String msg) { super(msg); }
    }

    Interpreter(String fileName) throws IOException{
    	Parser p = new Parser(fileName);
    	parsedExp = p.parse();
    	
    }
    Interpreter(Reader reader){
    	Parser p = new Parser(reader);
    	parsedExp = p.parse();
    }

    public JamVal callByValue() {
    	ASTVisitor<JamVal> valueVis = new ASTVisitor<JamVal>() {
    		  public JamVal forBoolConstant(BoolConstant b){
    			  return b;
    		  }
    		  public JamVal forIntConstant(IntConstant i){
    			  return i;
    		  }
    		  public JamVal forNullConstant(NullConstant n){
    			  return n;
    		  }
    		  public JamVal forJamEmpty(JamEmpty je){
    			  return je;
    		  }
    		  public JamVal forVariable(Variable v){
    			  return v;
    		  }
    		  public JamVal forPrimFun(PrimFun f){
    			  return f;
    		  }
    		  public JamVal forUnOpApp(UnOpApp u){
    			  return u;
    		  }
    		  public JamVal forBinOpApp(BinOpApp b){
    			  return b;
    		  }
    		  public JamVal forApp(App a){
    			  return a;
    		  }
    		  public JamVal forMap(Map m){
    			  return m;
    		  }
    		  public JamVal forIf(If i){
    			  return i;
    		  }
    		  public JamVal forLet(Let l){
    			  return l;
    		  }
    		};
    	AST result = parsedExp.accept(valueVis);
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
