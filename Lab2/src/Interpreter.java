import java.io.IOException;
import java.io.Reader;
import java.lang.String;

/**
 * Created by xiaozheng on 1/31/16.
 */

public class Interpreter {

	PureList<Binding> nextEnv = new Empty<Binding>();
	AST nextAST = null;

    /** file Interpreter.java **/
    class EvalException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 5213575105993689031L;

		EvalException(String msg) { super(msg); }
    }
    
    class ValueBinding extends Binding{
    	public ValueBinding(Variable variable, JamVal val) {
    		super(variable, val);
    	}
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
				return (JamVal) b;
			};
			@Override
    		public JamVal forIntConstant(IntConstant i){
				return (JamVal) i;
			}
			@Override
    		public JamVal forNullConstant(NullConstant n){
				return (JamList) JamEmpty.ONLY;
			}
			@Override
    		public JamVal forJamEmpty(JamEmpty je){
				return (JamList) JamEmpty.ONLY;
			}
			@Override
			public JamVal forVariable(Variable v){
				//TODO
				return (JamVal) JamEmpty.ONLY;
    		}
			@Override
			public JamVal forPrimFun(PrimFun f){
				final PrimFun ff = f;
				return f.accept(new PrimFunVisitor<JamVal>() {
					@Override
					public JamVal forArityPrim() {
						//TODO
						return null;
					}

					@Override
					public JamVal forConsPPrim() {
						Boolean res = ff.accept(new JamValVisitor<Boolean>() {

							@Override
							public Boolean forIntConstant(IntConstant ji) {
								return false;
							}

							@Override
							public Boolean forBoolConstant(BoolConstant jb) {
								return false;
							}

							@Override
							public Boolean forJamList(JamList jl) {
								return !(jl.equals(JamEmpty.ONLY));
							}

							@Override
							public Boolean forJamFun(JamFun jf) {
								return false;
							}
						
						});
						if (res)
							return (JamVal) BoolConstant.TRUE;
						else
							return (JamVal) BoolConstant.FALSE;
					}

					@Override
					public JamVal forConsPrim() {
						//TODO
						return null;
					}

					@Override
					public JamVal forFirstPrim() {
						//TODO
						return null;
					}

					@Override
					public JamVal forFunctionPPrim() {
						//TODO
						return null;
					}

					@Override
					public JamVal forListPPrim() {
						ff.accept(new JamValVisitor<Boolean>(){

							@Override
							public Boolean forIntConstant(IntConstant ji) {
								return false;
							}

							@Override
							public Boolean forBoolConstant(BoolConstant jb) {
								return false;
							}

							@Override
							public Boolean forJamList(JamList jl) {
								return true;
							}

							@Override
							public Boolean forJamFun(JamFun jf) {
								return false;
							}
							
						});
						if (ff.equals(JamEmpty.ONLY)){
							return true;
						}
						return null;
					}

					@Override
					public JamVal forNullPPrim() {
						Boolean res = ff.accept(new JamFunVisitor<Boolean>() {
							@Override
							public Boolean forJamClosure(JamClosure c) {
								return (c.env().accept(new PureListVisitor<Binding, Boolean>() {
									@Override
									public Boolean forEmpty(Empty<Binding> e) {
										return true;
									}

									@Override
									public Boolean forCons(Cons<Binding> c) {
										return false;
									}
								}));
							}

							@Override
							public Boolean forPrimFun(PrimFun pf) {
								throw new EvalException("null prim got to prim fun");
								//TODO?????
							}
						});
						if (res)
							return (JamVal) BoolConstant.TRUE;
						else
							return (JamVal) BoolConstant.FALSE;
					}

					@Override
					public JamVal forNumberPPrim() {
						//TODO
						return null;
					}

					@Override
					public JamVal forRestPrim() {
						//TODO
						return null;
					}
				});
    		}

			@Override
			public JamVal forUnOpApp(UnOpApp u){
				final UnOp op = u.rator();
				nextAST = u.arg();
				JamVal argVal = callByValue();
				argVal.accept(new JamValVisitor<Boolean>() {
					@Override
					public Boolean forBoolConstant(BoolConstant jb) {
						if (op.name == "/"){
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
				throw new EvalException("unreachable code in unop app");
    		}

			@Override
			public JamVal forBinOpApp(BinOpApp b) {
				JamVal res = null;
    			final BinOp op = b.rator();
				nextAST = b.arg1();

                System.out.println("binary operator: " + op.toString());


				final JamVal arg1Val = callByValue();
				System.out.println("arg1: " + ((IntConstant) arg1Val).value());
				nextAST = b.arg2();
				final JamVal arg2Val = callByValue();
				System.out.println("arg2: " + ((IntConstant) arg2Val).value());
				System.out.println("op: " + op.name);
				switch (op.name) {
					case "+":
					case "-":
					case "*":
					case "/":
						return arg1Val.accept(new JamValVisitor<IntConstant>() {
							@Override
							public IntConstant forBoolConstant(BoolConstant jb) {
								throw new EvalException("binop " + op.toString() + " was given bool " + jb.toString());
							}

							@Override
							public IntConstant forIntConstant(IntConstant ji) {
								int a = ji.value();
								System.err.println(arg2Val+":"+arg2Val.getClass().toString());
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
								else{
									throw new EvalException("unknown operator" + op.name);
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
					case "=":
					case "!=":
					case "<":
					case "<=":
					case ">":
					case ">=":
					case "&":
					case "|":
						return arg1Val.accept(new JamValVisitor<BoolConstant>() {
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
								System.out.println("a"+a);
								int b = ((IntConstant) arg2Val).value();
								System.out.println("b"+b);
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
				default:
					throw new EvalException("unrecognized op");
				}
			}

			@Override
    		public JamVal forApp(App a){
				JamList list = JamEmpty.ONLY;	
				nextAST = a.rator();
				System.out.println("Rator: "+a.rator().toString());
				JamVal fac = callByValue();
				System.out.println("Fac: "+fac.toString());
				JamValVisitor<Integer> pullOutInt = new JamValVisitor<Integer>(){

					@Override
					public Integer forIntConstant(IntConstant ji) {
						return ji.value();
					}

					@Override
					public Integer forBoolConstant(BoolConstant jb) {
						throw new EvalException("Expected int in list: "+jb.toString());
					}

					@Override
					public Integer forJamList(JamList jl) {
						throw new EvalException("Expected int in list: "+jl.toString());
					}

					@Override
					public Integer forJamFun(JamFun jf) {
						throw new EvalException("Expected int in list: "+jf.toString());
					}
					
				};
				int facVal = fac.accept(pullOutInt);
				for (int i = 1; i <= a.args().length; i++){
					nextAST = a.args()[a.args().length-i];
					JamVal res = callByValue();
					list.cons((JamVal)new IntConstant(facVal*res.accept(pullOutInt)));
				}
				return (JamVal) list;
			}

			/*
			 * Maps check if any variables are free and then substitute
			 */
			@Override
    		public JamVal forMap(Map m){
				for (int i = 0; i < m.vars().length; i++){
					final int ii = i;
					nextEnv.accept(new PureListVisitor<Binding,Boolean>(){

						@Override
						public Boolean forEmpty(Empty<Binding> e) {
							throw new EvalException("empty bindings for vars:"+m.vars().toString());
						}

						@Override
						public Boolean forCons(Cons<Binding> c) {
							Cons<Binding> next = c;
							while(!next.equals(c.empty())){
								if (next.first.var.equals(m.vars()[ii])){
									return true;
								}
								next = (Cons<Binding>) next.rest;
							}
							throw new EvalException("variable "+m.vars()[ii].toString()+" is not bounded.");
						}
						
					});
				}
				m.body();
				m.vars();
				return null;
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
				
				Def[] defs = l.defs();
				PureList<Binding> binds = new Empty<Binding>();
				for (int i = 1; i <= defs.length; i++) {
					
					nextAST = defs[defs.length-i].rhs();
					JamVal rhs = callByValue();
					binds = binds.cons(new ValueBinding(defs[i].lhs(),rhs));
				}
				JamClosure closure = new JamClosure(new Map(new Variable[0],l.body()),binds);
				
				return (JamVal) JamEmpty.ONLY;
			}
		};
		JamVal result = nextAST.accept(valueVis);
		return result;
	}

    public JamVal callByName()  {
        ASTVisitor<JamVal> valueVis = new ASTVisitor<JamVal>() {
            @Override
            public JamVal forBoolConstant(BoolConstant b) {
                return (JamVal) b;
            }

            ;

            @Override
            public JamVal forIntConstant(IntConstant i) {
                return (JamVal) i;
            }

            @Override
            public JamVal forNullConstant(NullConstant n) {
                return (JamList) JamEmpty.ONLY;
            }

            @Override
            public JamVal forJamEmpty(JamEmpty je) {
                return (JamList) JamEmpty.ONLY;
            }

            @Override
            public JamVal forVariable(Variable v) {
                //TODO
                return (JamVal) JamEmpty.ONLY;
            }

            @Override
            public JamVal forPrimFun(PrimFun f) {
                final PrimFun ff = f;
                return f.accept(new PrimFunVisitor<JamVal>() {
                    @Override
                    public JamVal forArityPrim() {
                        //TODO
                        return null;
                    }

                    @Override
                    public JamVal forConsPPrim() {
                        Boolean res = ff.accept(new JamValVisitor<Boolean>() {

                            @Override
                            public Boolean forIntConstant(IntConstant ji) {
                                return false;
                            }

                            @Override
                            public Boolean forBoolConstant(BoolConstant jb) {
                                return false;
                            }

                            @Override
                            public Boolean forJamList(JamList jl) {
                                return !(jl.equals(JamEmpty.ONLY));
                            }

                            @Override
                            public Boolean forJamFun(JamFun jf) {
                                return false;
                            }

                        });
                        if (res)
                            return (JamVal) BoolConstant.TRUE;
                        else
                            return (JamVal) BoolConstant.FALSE;
                    }

                    @Override
                    public JamVal forConsPrim() {
                        //TODO
                        return null;
                    }

                    @Override
                    public JamVal forFirstPrim() {
                        //TODO
                        return null;
                    }

                    @Override
                    public JamVal forFunctionPPrim() {
                        //TODO
                        return null;
                    }

                    @Override
                    public JamVal forListPPrim() {
                        ff.accept(new JamValVisitor<Boolean>() {

                            @Override
                            public Boolean forIntConstant(IntConstant ji) {
                                return false;
                            }

                            @Override
                            public Boolean forBoolConstant(BoolConstant jb) {
                                return false;
                            }

                            @Override
                            public Boolean forJamList(JamList jl) {
                                return true;
                            }

                            @Override
                            public Boolean forJamFun(JamFun jf) {
                                return false;
                            }

                        });
                        if (ff.equals(JamEmpty.ONLY)) {
                            return true;
                        }
                        return null;
                    }

                    @Override
                    public JamVal forNullPPrim() {
                        Boolean res = ff.accept(new JamFunVisitor<Boolean>() {
                            @Override
                            public Boolean forJamClosure(JamClosure c) {
                                return (c.env().accept(new PureListVisitor<Binding, Boolean>() {
                                    @Override
                                    public Boolean forEmpty(Empty<Binding> e) {
                                        return true;
                                    }

                                    @Override
                                    public Boolean forCons(Cons<Binding> c) {
                                        return false;
                                    }
                                }));
                            }

                            @Override
                            public Boolean forPrimFun(PrimFun pf) {
                                throw new EvalException("null prim got to prim fun");
                                //TODO?????
                            }
                        });
                        if (res)
                            return (JamVal) BoolConstant.TRUE;
                        else
                            return (JamVal) BoolConstant.FALSE;
                    }

                    @Override
                    public JamVal forNumberPPrim() {
                        //TODO
                        return null;
                    }

                    @Override
                    public JamVal forRestPrim() {
                        //TODO
                        return null;
                    }
                });
            }

            @Override
            public JamVal forUnOpApp(UnOpApp u) {
                final UnOp op = u.rator();
                nextAST = u.arg();
                JamVal argVal = callByValue();
                argVal.accept(new JamValVisitor<Boolean>() {
                    @Override
                    public Boolean forBoolConstant(BoolConstant jb) {
                        if (op.name == "/") {
                            if (jb.value())
                                return false;
                            else
                                return true;
                        } else {
                            throw new EvalException("unop got " + op.toString() + " on a boolean");
                        }
                    }

                    @Override
                    public Boolean forIntConstant(IntConstant ji) {
                        if (op.name == "+") {
                            return (ji.value() > 0);
                        } else if (op.name == "-") {
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
                throw new EvalException("unreachable code in unop app");
            }

        }



            return (JamVal) JamEmpty.ONLY;
    }

    public JamVal callByNeed()  {
    	//TODO
    	return (JamVal) JamEmpty.ONLY;
    }

}
