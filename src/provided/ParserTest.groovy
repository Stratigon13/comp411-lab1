package provided

import junit.framework.TestCase
import org.junit.Test


/**
 * Created by xiaozheng on 1/23/16.
 */
public class ParserTest extends TestCase{

    protected void checkString(String name, String answer, String program) {
        Parser p = new Parser(new StringReader(program));
        try{
            assertEquals(name, answer, p.parse().toString());
        } catch (ParseException e){
            System.err.println(e);
        }
    }

    @Test
    public void testParseFactorPrim() throws Exception {
        try {
            String output = "function?";
            String input = "function?";
            checkString("prim  ", output, input );

        } catch (Exception e) {
            fail("prim   threw " + e);
        }
    }


    @Test
    public void testParseFactorId() throws Exception {
        try {
            String output = "car";
            String input = "car";
            checkString("id  ", output, input );

        } catch (Exception e) {
            fail("id   threw " + e);
        }
    }

    @Test
    public void testParseTermNull() throws Exception {
        try {
            String output = "null";
            String input = "null";
            checkString("nul  ", output, input );

        } catch (Exception e) {
            fail("null   threw " + e);
        }
    }

    @Test
    public void testParseTermInt() throws Exception {
        try {
            String output = "3";
            String input = "3";
            checkString("Int  ", output, input );

        } catch (Exception e) {
            fail("Int   threw " + e);
        }
    }

    @Test
    public void testParseTermBool() throws Exception {
        try {
            String output = "false";
            String input = "false";
            checkString("Bool  ", output, input );

        } catch (Exception e) {
            fail("Bool   threw " + e);
        }
    }

    @Test
    public void testParseTermUnop() throws Exception {
        try {
            String output = "- 3";
            String input = "- 3";
            checkString("Unop  ", output, input );

        } catch (Exception e) {
            fail("Unop   threw " + e);
        }
    }


    @Test
    public void testParseTerm_Factor_ExpList() throws Exception {
        try {
            String output = "(a + a), (b + b), (c + c)";
            String input = "(a + a), (b + b), (c + c)";
            checkString("Factor  ", output, input );

        } catch (Exception e) {
            fail("Factor   threw " + e);
        }
    }

    @Test
    public void testParseExpLet() throws Exception {
        try {
            String output = "let a := 3; in (a + a)";
            String input = "let a:=3; in a + a";
            checkString("let", output, input );

        } catch (Exception e) {
            fail("let threw " + e);
        }
    }


    @Test
    public void testParseExpMap() throws Exception {
        try {
            String output = "map f to (map x to f(x(x)))(map x to f(x(x)))";
            String input = "map f to (map x to f( x( x ) ) ) (map x to f(x(x)))";
            checkString("map", output, input );

        } catch (Exception e) {
            fail("map threw " + e);
        }
    }

    @Test
    public void testParseExpIf() throws Exception {
        try {
            String output = "if a = 3; then b = 2; else c = 3";
            String input = "if a = 3; then b = 2; else c = 3";
            checkString("if", output, input );

        } catch (Exception e) {
            fail("if threw " + e);
        }
    }

    @Test
    public void testParseIdList() throws Exception {
        try {
            String output = "a, b, c";
            String input = "a, b, c";
            checkString("id", output, input );

        } catch (Exception e) {
            fail("id threw " + e);
        }
    }

    @Test
    public void testParseException1() {
        try {
            String output = "doh!";
            String input = "null + 3 -";
            checkString("parseException", output, input );

            fail("parseException did not throw ParseException exception");
            //} catch (ParseException e) {
            //e.printStackTrace();

        } catch (Exception e) {
            fail("parseException threw " + e);
        }
    } //end of func


}
