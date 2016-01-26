import com.sun.org.apache.xpath.internal.operations.Bool;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Set;

//import static org.junit.Assert.*;

/**
 * Created by xiaozheng on 1/25/16.
 */
public class ParserFileTest extends TestCase {

    protected void checkString(String name, String answer, String program) {
        Parser p = new Parser(new StringReader(program));
        String actual = p.parse().toString();
        System.out.println("");
        System.out.println("** " + name + " **");
        System.out.println("Expected: " + answer);
        System.out.println("Actual: " + actual);
        try{
            assertEquals(name, answer, actual);
        } catch (ParseException e){
            System.err.println(e);
        }finally{System.out.flush();}
    }



    @Test
    public void testParseFile00() throws Exception {
        try {
            String output = "doh!";
            String input = "null + 3 -";
            checkString("ParseException:", output, input );

            fail("parseException did not throw ParseException exception");

        } catch (Exception e) {
            System.out.println("Caught the exception");
        }
    }


    @Test
    public void testParseFile01() throws Exception {
        try {
            String output = "function?()";
            String input = "function? ()";
            checkString("prim  ", output, input);

        } catch (Exception e) {
            fail("prim   threw " + e);
        }
    }

    @Test
    public void testParseFileFun() throws Exception {
        try {
            String output = "(f(x) + (x * 12))";
            String input = "f(x) + (x * 12))";
            checkString("prim  ", output, input);

        } catch (Exception e) {
            fail("prim   threw " + e);
        }

    }

    @Test
    public void testParseFileIn() throws Exception {
        try {
            String output = "let f := map n to if (n = 0) then 1 else (n * f((n - 1))); " +
                            "in f(3)";
            String input = "let f :=  map n to if n = 0 then 1 else n * f(n - 1);\n" +
                           "in f(3)";
            checkString("let  ", output, input);

        } catch (Exception e) {
            fail("let   threw " + e);
        }
    }


    @Test
    public void testParseFileIn1() throws Exception {
        try {
            String output = "let Y := map f to " +
                    "let g := map x to f(map z to (x(x))(z)); " +
                    "in g(g); " +
                    "FACT := map f to " +
                    "map n to if (n = 0) then 1 else (n * f((n - 1))); " +
                    "in (Y(FACT))(3)";
            String input = "let Y    := map f to \n" +
                    "              let g := map x to f(map z to (x(x))(z));\n" +
                    "\t    in g(g);\n" +
                    "    FACT := map f to \n" +
                    "\t      map n to if n = 0 then 1 else n * f(n - 1);\n" +
                    "in (Y(FACT))(3)";
            checkString("let  ", output, input);

        } catch (Exception e) {
            fail("let   threw " + e);
        }
    }

    @Test
    public void testParseFileIn2() throws Exception {
        try {
            String output = "let Y := map f to " +
                    "let g := map x to f(map z1,z2 to (x(x))(z1, z2)); " +
                    "in g(g); " +
                    "APPEND := map ap to " +
                    "map x,y to " +
                    "if (x = null) then y else cons(first(x), ap(rest(x), y)); " +
                    "l := cons(1, cons(2, cons(3, null))); " +
                    "in (Y(APPEND))(l, l)";
            String input = "let Y    := map f to \n" +
                    "              let g := map x to f(map z1,z2 to (x(x))(z1,z2));\n" +
                    "\t    in g(g);\n" +
                    "    APPEND := map ap to \n" +
                    "\t        map x,y to \n" +
                    "                  if x = null then y else cons(first(x), ap(rest(x), y));\n" +
                    "    l      := cons(1,cons(2,cons(3,null)));\t\n" +
                    "in (Y(APPEND))(l,l)";
            checkString("let  ", output, input);

        } catch (Exception e) {
            fail("let   threw " + e);
        }
    }




    HashMap<String, Boolean> inputFileMap = new HashMap<String, Boolean>() {
        {
            put("src/00good.jam", true);
            put("src/01good.jam", true);
            put("src/02good.jam", true);
            put("src/03bad.jam",  false);

            put("src/04bad.jam",  false);
            put("src/05good.jam", true);
            put("src/06good.jam", true);

            put("src/07bad.jam", false);
            put("src/08bad.jam", false);
            put("src/09good.jam", true);


            put("src/10bad.jam", false);
            put("src/12good.jam", true);
            put("src/14bad.jam",  false);
            put("src/15good.jam",  true);
            put("src/16bad.jam", false);
            put("src/17good.jam", true);
        }
    };


    protected void checkFile(String programFilename) {
        try {
            Parser p = new Parser(programFilename);
            p.parse().toString();
        } catch (IOException e) {
            fail("Critical error: IOException caught while reading input file");
            e.printStackTrace();
        } catch (ParseException e) {
            boolean condition = inputFileMap.get(programFilename);
            assertEquals(condition, false);
        }
    }

    @Test
    public void testFiles() throws Exception {
        String Filename =  "";
        try {
            Set<String> keysets = inputFileMap.keySet();


            for(String programFilename : keysets) {
                Filename = programFilename;
                checkFile(programFilename);
            }

            System.out.println(Filename);

        } catch (Exception e) {
            fail(Filename + " threw " + e);
        }
    }










}