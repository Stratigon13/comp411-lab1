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
				final IntConstant ii = i;
				JamVal result = new JamVal() {
					@Override
					public <JamVal> JamVal accept(JamValVisitor<JamVal> jvv) {
						return ii.accept(jvv);
					}
				};
				return result;
			}
			@Override
    		public JamVal forNullConstant(NullConstant n){
				return (JamVal) JamEmpty.ONLY;
			}
			@Override
    		public JamVal forJamEmpty(JamEmpty je){
				return (JamVal) JamEmpty.ONLY;
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
				final UnOp op = u.rator();
				nextAST = u.arg();
				JamVal argVal = callByValue();
				argVal.accept(new JamValVisitor<Boolean>() {
					@Override
					public Boolean forBoolConstant(BoolConstant jb) {
						if (op.name == "~"){
							if (jb.value())
								return false;
							else
								return true;
						} else{
							throw new EvalException("unop got " + op.toString() + " on a boolean");
						}
					}

					@Override
					public Boolean forIntConstant(IntConstant ji) {
						if (op.name == "+"){
							return (ji.value() > 0);
						} else if (op.name == "-"){
							return (ji.value() < 0);
						} else {
							throw new EvalException("unop got " + op.toString() + "on an int");
						}
					}

					@Override
					public Boolean forJamFun(JamFun jf) {
						throw new EvalException("unop was given fun " + jf.toString());
					}

					@Override
					public Boolean forJamList(JamList jl) {
						throw new EvalException("unop was given list " + jl.toString());
					}
				});


    		}
			@Override
			public JamVal forBinOpApp(BinOpApp b){
    			final BinOp op = b.rator();
				nextAST = b.arg1();
				final JamVal arg1Val = callByValue();
				nextAST = b.arg2();
				final JamVal arg2Val = callByValue(){
				}
				switch (op.name) {
					case "+":
					case "-":
					case "*":
					case "/":
						arg1Val.accept(new JamValVisitor<IntConstant>() {
							@Override
							public IntConstant forBoolConstant(BoolConstant jb) {
								throw new EvalException("binop " + op.toString() + " was given bool " + jb.toString());
							}

							@Override
							public IntConstant forIntConstant(IntConstant ji) {
								int a = ji.value();
								int b = ((IntConstant) arg2Val).value();
								if (op.name == "+") {
									return new IntConstant(a + b);
								} else if (op.name == "-") {
									return new IntConstant(a - b);
								} else if (op.name == "*") {
									return new IntConstant(a * b);
								} else if (op.name == "/") {
									if (b == 0) {
										throw new EvalException("divide by zero");
									}
									return new IntConstant(a / b);
								}
							}

							@Override
							public IntConstant forJamFun(JamFun jf) {
								throw new EvalException("binop " + op.toString() + " was given fun " + jf.toString());
							}

							@Override
							public IntConstant forJamList(JamList jl) {
								throw new EvalException("binop " + op.toString() + " was given list " + jl.toString());
							}
						});
						break;
					case "=":
					case "!=":
					case "<":
					case "<=":
					case ">":
					case ">=":
					case "&":
					case "|":
						arg1Val.accept(new JamValVisitor<BoolConstant>() {
							@Override
							public BoolConstant forBoolConstant(BoolConstant jb) {
								Boolean a = jb.value();
								Boolean b = ((BoolConstant) arg2Val).value();
								if (op.name == "="){
									if (a == b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								}else if (op.name == "!="){
									if (a != b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								}else if (op.name == "&"){
									if (a && b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								}else if (op.name == "|"){
									if (a || b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								} else{
									throw new EvalException("binop " + op.toString() + " was given bool " + jb.toString());
								}
							}

							@Override
							public BoolConstant forIntConstant(IntConstant ji) {
								int a = ji.value();
								int b = ((IntConstant) arg2Val).value();
								if (op.name == "="){
									if (a == b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								}else if (op.name == "!=") {
									if (a != b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								}else if (op.name == "<") {
									if (a < b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								}else if (op.name == "<=") {
									if (a <= b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								}else if (op.name == ">") {
									if (a > b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								}else if (op.name == ">=") {
									if (a >= b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								}else{
									throw new EvalException("binop " + op.toString() + " was given int " + ji.toString());
								}
							}
							@Override
							public BoolConstant forJamFun(JamFun jf) {
								int a = jf.hashCode();
								int b = ((JamFun) arg2Val).hashCode();
								if (op.name == "="){
									if (a == b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								} else {
									throw new EvalException("binop " + op.toString() + " was given fun " + jf.toString());
								}

							}

							@Override
							public BoolConstant forJamList(JamList jl) {
								int a = jl.hashCode();
								int b = ((JamList) arg2Val).hashCode();
								if (op.name == "="){
									if (a == b)
										return BoolConstant.TRUE;
									else
										return BoolConstant.FALSE;
								} else {
									throw new EvalException("binop " + op.toString() + " was given list " + jl.toString());
								}

							}
						});
				}
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
