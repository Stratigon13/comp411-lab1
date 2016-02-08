import java.io.IOException;
import java.io.Reader;

/**
 * Created by xiaozheng on 1/31/16.
 */

public class Interpreter {

	AST nextAST = null;

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
		nextAST = p.parse();
    }

    Interpreter(Reader reader){
    	Parser p = new Parser(reader);
		nextAST = p.parse();
    }

    public JamVal callByValue() {
    	ASTVisitor<JamVal> valueVis = new ASTVisitor<JamVal>() {
			@Override
			public JamVal forBoolConstant(BoolConstant b) {
				final BoolConstant bb = b;
				JamVal result = new JamVal() {
					@Override
					public <JamVal> JamVal accept(JamValVisitor<JamVal> jvv) {
						return bb.accept(jvv);
					}
				};
				return result;
			};
			@Override
    		public JamVal forIntConstant(IntConstant i){
    			  return i;
    		  }
			@Override
    		public JamVal forNullConstant(NullConstant n){
    			return n;
			}
			@Override
    		public JamVal forJamEmpty(JamEmpty je){
    			  return je;
    		  }
			@Override
			public JamVal forVariable(Variable v){
    			  return v;
    		  }
			@Override
			public JamVal forPrimFun(PrimFun f){
    			  return f;
    		  }
			@Override
			public JamVal forUnOpApp(UnOpApp u){
    			  return u;
    		  }
			@Override
			public JamVal forBinOpApp(BinOpApp b){
    			  return b;
    		  }
			@Override
    		public JamVal forApp(App a){
    			  return a;
    		  }
			@Override
    		public JamVal forMap(Map m){
    			  return m;
    		  }
			@Override
    		public JamVal forIf(If i){
				nextAST = i.test();
				JamVal testVal = callByValue();
				Boolean res = testVal.accept(new JamValVisitor<Boolean>() {
					@Override
					public Boolean forBoolConstant(BoolConstant jb) {
						return jb.value();
					}

					@Override
					public Boolean forIntConstant(IntConstant ji) {
						throw new EvalException("if was given int " + ji.toString());
					}

					@Override
					public Boolean forJamFun(JamFun jf) {
						throw new EvalException("if was given fun " + jf.toString());
					}

					@Override
					public Boolean forJamList(JamList jl) {
						throw new EvalException("if was given list " + jl.toString());
					}
				});
				if (res) {
					nextAST = i.conseq();
					return callByValue();
				} else {
					nextAST = i.alt();
					return callByValue();
				}
			}
			@Override
    		public JamVal forLet(Let l){
    			return l;
			}
		};
		JamVal result = nextAST.accept(valueVis);
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
