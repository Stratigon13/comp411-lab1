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

    public void testNumberP() {
        try {
            String output = "number?";
            String input = "number?";
            allCheck("numberP", output, input);

        } catch (Exception e) {
            e.printStackTrace();
            fail("numberP threw " + e);
        }
    } //end of func

    public void testFunctionP() {
        try {
            String output = "function?";
            String input = "function?";
            allCheck("functionP", output, input);

        } catch (Exception e) {
            e.printStackTrace();
            fail("functionP threw " + e);
        }
    } //end of func

    public void testConsP() {
        try {
            String output = "con?";
            String input = "con?";
            allCheck("conP", output, input);

        } catch (Exception e) {
            e.printStackTrace();
            fail("functionP threw " + e);
        }
    } //end of func

    public void testNullP() {
        try {
            String output = "null?";
            String input = "null?";
            allCheck("nullP", output, input);

        } catch (Exception e) {
            e.printStackTrace();
            fail("nullP threw " + e);
        }
    }

    public void testlistP() {
        try {
            String output = "list?";
            String input = "list?";
            allCheck("listP", output, input);

        } catch (Exception e) {
            e.printStackTrace();
            fail("listP threw " + e);
        }
    }








}
