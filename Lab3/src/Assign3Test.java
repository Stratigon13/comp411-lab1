import java.util.StringTokenizer;
import junit.framework.TestCase;
import java.io.*;

public class Assign3Test extends TestCase {

  public Assign3Test (String name) {
    super(name);
  }
  
  /**
   * The following 9 check methods create an interpreter object with the
   * specified String as the program, invoke the respective evaluation
   * method (valueValue, valueName, valueNeed, etc.), and check that the 
   * result matches the (given) expected output.  
   */
 
  private void valueValueCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value-value " + name, answer, interp.valueValue().toString());
  }

  private void valueNameCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value-name " + name, answer, interp.valueName().toString());
  }
   
  private void valueNeedCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value-need " + name, answer, interp.valueNeed().toString());
  }

  private void nameValueCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value " + name, answer, interp.nameValue().toString());
  }

  private void nameNameCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-name " + name, answer, interp.nameName().toString());
  }
   
  private void nameNeedCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-need " + name, answer, interp.nameNeed().toString());
  }

  private void needValueCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value " + name, answer, interp.needValue().toString());
  }

  private void needNameCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-name " + name, answer, interp.needName().toString());
  }
   
  private void needNeedCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-need " + name, answer, interp.needNeed().toString());
  }

  private void allCheck(String name, String answer, String program) {
    valueValueCheck(name, answer, program);
    valueNameCheck(name, answer, program);
    valueNeedCheck(name, answer, program);
    nameValueCheck(name, answer, program);
    nameNameCheck(name, answer, program);
    nameNeedCheck(name, answer, program);
    needValueCheck(name, answer, program);
    needNameCheck(name, answer, program);
    needNeedCheck(name, answer, program);
  }

  private void noNameCheck(String name, String answer, String program) {
    valueValueCheck(name, answer, program);
    valueNameCheck(name, answer, program);
    valueNeedCheck(name, answer, program);
    needValueCheck(name, answer, program);
    needNameCheck(name, answer, program);
    needNeedCheck(name, answer, program);
  }

  private void needCheck(String name, String answer, String program) {
    needValueCheck(name, answer, program);
    needNeedCheck(name, answer, program);
  }


  private void lazyCheck(String name, String answer, String program) {
    valueNameCheck(name, answer, program);
    valueNeedCheck(name, answer, program);
    nameNameCheck(name, answer, program);
    nameNeedCheck(name, answer, program);
    needNameCheck(name, answer, program);
    needNeedCheck(name, answer, program);
  }

  

  public void testNumberP() {
    try {
      String output = "number?";
      String input = "number?";
      allCheck("numberP", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("numberP threw " + e);
    }
  } //end of func
  

  public void testMathOp() {
    try {
	String output = "18";
      String input = "2 * 3 + 12";
      allCheck("mathOp", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("mathOp threw " + e);
    }
  } //end of func
  

  public void testParseException() {
    try {
      String output = "haha";
      String input = " 1 +";
      allCheck("parseException", output, input );

         fail("parseException did not throw ParseException exception");
      } catch (ParseException e) {   
         //e.printStackTrace();
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("parseException threw " + e);
    }
  } //end of func
  

  public void testEvalException() {
    try {
      String output = "mojo";
      String input = "1 + number?";
      allCheck("evalException", output, input );

         fail("evalException did not throw EvalException exception");
      } catch (EvalException e) {   
         //e.printStackTrace();
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("evalException threw " + e);
    }
  } //end of func
  
  public void testSyntaxException1() {
	    try {
	      String output = "mojo";
	      String input = "map x to y";
	      allCheck("syntaxException1", output, input );

	         fail("syntaxException1 did not throw SyntaxException exception");
	      } catch (SyntaxException e) {   
	         //e.printStackTrace();
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	      fail("evalException threw " + e);
	    }
	  } //end of func
  
  public void testSyntaxException2() {
	    try {
	      String output = "mojo";
	      String input = "map x,x to 1";
	      allCheck("syntaxException2", output, input );

	         fail("syntaxException2 did not throw SyntaxException exception");
	      } catch (SyntaxException e) {   
	         //e.printStackTrace();
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	      fail("evalException threw " + e);
	    }
	  } //end of func
  
  public void testSyntaxException3() {
	    try {
	      String output = "mojo";
	      String input = "let x:=1; x:=2; in x";
	      allCheck("syntaxException 3", output, input );

	         fail("syntaxException3 did not throw SyntaxException exception");
	      } catch (SyntaxException e) {   
	         //e.printStackTrace();
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	      fail("evalException threw " + e);
	    }
	  } //end of func
  

  public void testAppend() {
    try {
      String output = "(1 2 3 1 2 3)";
      String input = "let Y    := map f to              let g := map x to f(map z1,z2 to (x(x))(z1,z2));     in g(g);  APPEND := map ap to            map x,y to               if x = null then y else cons(first(x), ap(rest(x), y)); l      := cons(1,cons(2,cons(3,null))); in (Y(APPEND))(l,l)";
      allCheck("append", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("append threw " + e);
    }
  } //end of func
  

  public void testLetRec() {
    try {
      String output = "(1 2 3 1 2 3)";
      String input = "let append :=       map x,y to          if x = null then y else cons(first(x), append(rest(x), y));    l      := cons(1,cons(2,cons(3,null))); in append(l,l)";
      allCheck("letRec", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("letRec threw " + e);
    }
  } //end of func
  

  public void testLazyCons() {
    try {
      String output = "1";
      String input = "let zeroes := cons(0,zeroes);in first(rest(zeroes))";
      lazyCheck("lazyCons", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("lazyCons threw " + e);
    }
  } //end of func
  
  public void testLazyCons2() {
	    try {
	      String output = "()";
	      String input = "let f:= map x to cons(first(x)+1,rest(x)); l := cons(0,f(l)); in f(cons(0,null))";
	      lazyCheck("lazyCons2", output, input );

	    } catch (Exception e) {
	      e.printStackTrace();
	      fail("lazyCons2 threw " + e);
	    }
	  } //end of func
  
  public void testCBNFib() {
	    try {
	      String output = "((0 1) (1 1) (2 2) (3 3) (4 5) (5 8) (6 13) (7 21) (8 34) (9 55) (10 89) (11 144) (12 233) (13 377) (14 610) (15 987) (16 1597) (17 2584) (18 4181) (19 6765) (20 10946) (21 17711) (22 28657) (23 46368) (24 75025) (25 121393))";
	      String input = "let Y    := map f to              let g := map x to f(x(x));             in g(g);    FIB  := map fib to              map n to if n <= 1 then 1 else fib(n - 1) + fib(n - 2); FIBHELP := map fibhelp to              map k,fn,fnm1 to                if k = 0 then fn                else fibhelp(k - 1, fn + fnm1, fn);    pair := map x,y to cons(x, cons(y, null));in let FFIB := map ffib to                map n to if n = 0 then 1 else (Y(FIBHELP))(n - 1,1,1);  in let ffib := Y(FFIB);      in let FIBS := map fibs to                       map k,l to                         let fibk := ffib(k);                         in if 0 <= k then                              fibs(k - 1, cons(pair(k,fibk), l))                            else l;         in (Y(FIBS))(25, null)";
	      needCheck("CBNFib", output, input );

	    } catch (Exception e) {
	      e.printStackTrace();
	      fail("CBNFib threw " + e);
	    }
	  } //end of func
  
  public void testSieve() {
	    try {
	      String output = "(3 5 7 11 13 17 19 23 29 31 37 41 43 47 53 59 61 67 71 73)";
	      String input = "let susp? := map l to cons?(l) & function?(first(l)); makeSusp := map f to cons(f, null); in let block2 := map x,y to y;           fo := map prom to if susp?(prom) then (first(prom))() else prom;           Y := map f to                   let g := map x to f(x(x));                   in g(g);   in let MAPSTREAM := map mapStream to                          map f,l to let fol := fo(l);                                     in if (fol = null) then null                                     else cons(f(first(fol)), makeSusp(map  to mapStream(f, rest(fol))));             FILTER := map filter to                          map p,l to let fol := fo(l);                                     in if (fol = null) then null                                        else if p(first(fol)) then filter(p, rest(fol))                                        else cons(first(fol), makeSusp(map  to filter(p, rest(fol))));            divides := map a,b to (((b / a) * a) = b);            INITSEG := map initSeg to                          map l,n to if (n <= 0) then null                                     else let fol := fo(l);                                          in cons(first(fol), initSeg(rest(fol), (n - 1)));       in let PRIMES := map primes to                          map l to let fol := fo(l);                                   in let l1 := (Y(FILTER))(map x to divides(first(fol), x), rest(fol));                                      in cons(first(fol), makeSusp(map  to primes(l1)));             ODDNUMS := map oddNums to                           map  to cons(3, makeSusp(map  to (Y(MAPSTREAM))(map i to (i + 2), oddNums())));          in (Y(INITSEG))(((Y(PRIMES))((Y(ODDNUMS))())), 20)";
	      needCheck("sieve", output, input );
	    } catch (Exception e) {
		      e.printStackTrace();
		      fail("sieve threw " + e);
		    }
	  } //end of func
  
  
}





