import junit.framework.TestCase;
import org.junit.Test;

import java.io.StringReader;

/**
 * Created by xiaozheng on 2/1/16.
 */
public class InterpreterTest extends TestCase {


    /**
     * The following 3 check methods create an interpreter object with the
     * specified String as the program, invoke the respective evaluation
     * method (callByValue, callByName, callByNeed), and check that the
     * result matches the (given) expected output.  If the test fails,
     * the method prints a report as to which test failed and how many
     * points should be deducted.
     */

    private void valueCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-value " + name, answer, interp.callByValue().toString());
    }

    private void nameCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-name " + name, answer, interp.callByName().toString());
    }

    private void needCheck(String name, String answer, String program) {
        Interpreter interp = new Interpreter(new StringReader(program));
        assertEquals("by-need " + name, answer, interp.callByNeed().toString());
    }

    private void allCheck(String name, String answer, String program) {
        valueCheck(name, answer, program);
        nameCheck(name, answer, program);
        needCheck(name, answer, program);
    }


    @Test
    public void testIntConstant() {
        try {
            String output = "2";
            String input = "2";
            valueCheck("IntConstant", output, input);
        } catch (Exception e) {
            //e.printStackTrace();
            fail("IntConstant threw " + e);
        }


    } //end of func

    @Test
    public void testNullConstant() {
        try {
            String output = "()";
            String input = "null";
            valueCheck("NullConstant", output, input);
        } catch (Exception e) {
            //e.printStackTrace();
            fail("NullConstant threw " + e);
        }
    } //end of func

    @Test
    public void testBoolConst() {
        try {
            String output = "false";
            String input = "false";
            valueCheck("boolConst", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("boolConst threw " + e);
        }
    }



//    @Test
//    public void testNullP() {
//        try {
//            String output = "null?";
//            String input = "null?";
//            valueCheck("nullP", output, input);
//
//        } catch (Exception e) {
//            //e.printStackTrace();
//            fail("nullP threw " + e);
//        }
//    }


    @Test
    public void testConsP() {
        try {
            String output = "con?";
            String input = "con?";
            valueCheck("append", output, input);
            //allCheck("conP", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("append threw " + e);
        }
    } //end of func

    @Test
    public void testNullP() {
        try {
            String output = "null?";
            String input = "null?";
            //allCheck("nullP", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("nullP threw " + e);
        }
    }

    @Test
    public void testNumberP() {
        try {
            String output = "number?";
            String input = "number?";
            allCheck("numberP", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("numberP threw " + e);
        }
    } //end of func

    @Test
    public void testFunctionP() {
        try {
            String output = "function?";
            String input = "function?";
            //allCheck("functionP", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("functionP threw " + e);
        }
    } //end of func

    @Test
    public void testArityP() {
        try {

            String output = "arity";
            String input = "arity";
            valueCheck("arity", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("arityP threw " + e);
        }
    }


    @Test
    public void testListP() {
        try {
            String output = "list?";
            String input = "list?";
            //allCheck("listP", output, input);

            valueCheck("nullP", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("listP threw " + e);
        }
    }

    @Test
    public void testList() {
        try {
            String output = "list";
            String input = "list";
            allCheck("list", output, input);
        } catch (Exception e) {
            //e.printStackTrace();
            fail("listP threw " + e);
        }
    }



    @Test
    public void testFirst() {
        try {
            String output = "first";
            String input = "first";
            //allCheck("first", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("Rest threw " + e);
        }
    }

    @Test
    public void testRest() {
        try {
            String output = "rest";
            String input = "rest";
            allCheck("Rest", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("Rest threw " + e);
        }
    }


    @Test
    public void testMathOp1() {
        try {
            String output = "0";
            String input = "1 - 1";
            //valueCheck("mathOp", output, input );

            Interpreter interp = new Interpreter(new StringReader(input));
            String actual = interp.callByValue().toString();
            //System.out.println("actual: " + actual);
            assertEquals("by-value " + "mathOp", output, actual);


        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp1 threw " + e);
        }
    }

    @Test
    public void testMathOp2() {
        try {
            String output = "18";
            String input = "2 * 3 + 12";
            valueCheck("mathOp2", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("mathOp2 threw " + e);
        }
    } //end of func

    @Test
    public void testBinOpEqual() {
        try {
            String output = "true";
            String input = " 5 = 5 ";
            allCheck("BinOpEqual", output, input );

        } catch (Exception e) {
            //e.printStackTrace();
            fail("BinOpEqual threw " + e);

        }
    } //end of func

    @Test
    public void testBinOpNonEqual() {
        try {
            String output = "true";
            String input = " 5 != 6 ";

            allCheck("BinOpNonEqual", output, input );
            valueCheck("NonEqualOp", output, input );


        } catch (Exception e) {
            //e.printStackTrace();
            fail("BinOpNonEqual threw " + e);
        }
    }

    @Test
    public void testBinOpGreater() {
        try {
            String output = "true";
            String input = " 5 < 6 ";
            allCheck("BinOpGreater", output, input );
        } catch (Exception e) {
            //e.printStackTrace();
            fail("BinOpGreater threw " + e);
        }
    }

    @Test
    public void testBinOpLesser() {
        try {
            String output = "false";
            String input = " 5 > 6 ";
            allCheck("BinOpLesser", output, input );

        } catch (Exception e) {
            //e.printStackTrace();
            fail("BinOpLesser threw " + e);
        }
    }

    @Test
    public void testBinAnd() {
        try {
            String output = "false";
            String input = " true & false";
            allCheck("BinAnd", output, input );

        } catch (Exception e) {
            //e.printStackTrace();
            fail("BinAnd threw " + e);
        }
    }

    @Test
    public void testBinOr() {
        try {
            String output = "true";
            String input = " true | false";
            allCheck("BinOr", output, input );

        } catch (Exception e) {
            //e.printStackTrace();
            fail("BinOr threw " + e);
        }
    }

    @Test
    public void testAppend() {
        try {
            String output = "if x = 11 then 3 + 2 else 5 + 1";
            String input = "if x = 3 + 8 then 3 + 2 else 5 + 1";
            allCheck("If", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("If threw " + e);
        }
    } //end of func

    @Test
    public void testIf() {
        try {
            String output = "(1 2 3 1 2 3)";

            String input = "let Y    := map f to              let g := map x to f(map z1,z2 to (x(x))(z1,z2));     in g(g);  APPEND := map ap to            map x,y to               if x = null then y else cons(first(x), ap(rest(x), y)); l      := cons(1,cons(2,cons(3,null))); in (Y(APPEND))(l,l)";
            valueCheck("append", output, input);
        } catch (Exception e) {
            //e.printStackTrace();
            fail("append threw " + e);
        }

    }

    @Test
    public void testIn1_Value() {
        try {
            String output = "6";
            String input = "let Y    := map f to \n" +
                    "              let g := map x to f(map z to (x(x))(z));\n" +
                    "\t    in g(g);\n" +
                    "    FACT := map f to \n" +
                    "\t      map n to if n = 0 then 1 else n * f(n - 1);\n" +
                    "in (Y(FACT))(3)";
            valueCheck("append", output, input);

            //allCheck("append", output, input );

        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println();
            fail("append threw " + e);
        }
    } //end of func

    @Test
    public void testIn2_Value() {
        try {
            String output = "(1 2 3 1 2 3)";
            String input = "let Y    := map f to \n" +
                    "              let g := map x to f(map z1,z2 to (x(x))(z1,z2));\n" +
                    "\t    in g(g);\n" +
                    "    APPEND := map ap to \n" +
                    "\t        map x,y to \n" +
                    "                  if x = null then y else cons(first(x), ap(rest(x), y));\n" +
                    "    l      := cons(1,cons(2,cons(3,null)));\t\n" +
                    "in (Y(APPEND))(l,l)";
            valueCheck("append", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("append threw " + e);
        }
    }

    @Test
    public void testIn1_Name() {
        try {
            String output = "6";
            String input = "let Y    := map f to \n" +
                    "              let g := map x to f(x(x));\n" +
                    "\t    in g(g);\n" +
                    "    FACT := map f to \n" +
                    "\t      map n to if n = 0 then 1 else n * f(n - 1);\n" +
                    "in (Y(FACT))(3)";
            nameCheck("append", output, input);
            needCheck("append", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("append threw " + e);
        }
    }

    @Test
    public void testIn2_Name() {
        try {
            String output = "(1 2 3 1 2 3)";
            String input = "let Y    := map f to \n" +
                    "              let g := map x to f(x(x));\n" +
                    "\t    in g(g);\n" +
                    "    APPEND := map ap to \n" +
                    "\t        map x,y to \n" +
                    "                  if x = null then y else cons(first(x), ap(rest(x), y));\n" +
                    "    l      := cons(1,cons(2,cons(3,null)));\t\n" +
                    "in (Y(APPEND))(l,l)";
            nameCheck("append", output, input);
            needCheck("append", output, input);

        } catch (Exception e) {
            //e.printStackTrace();
            fail("append threw " + e);
        }
    }




}
