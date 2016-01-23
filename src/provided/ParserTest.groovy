package provided

import junit.framework.TestCase
import org.junit.Test
import provided.ParseException;
import provided.Parser;
import junit.framework.*;
import java.io.*;

/**
 * Created by xiaozheng on 1/23/16.
 */
public class ParserTest extends TestCase{
    @Test
    public void testParseFactor() throws Exception {
        Parser parser = new Parser("src/provided/tests/simple/01good");
        AST ast = parser.parse();
        assertEquals(ast.toString(), "function?");
        assertEquals(ast.class, PrimFun);
    }

    @Test
    public void testParseTerm() throws Exception {
        Parser parser = new Parser("src/provided/tests/simple/00bad");
        AST ast = parser.parse();
        assertEquals(ast.toString(), "null");
        assertEquals(ast.class, NullConstant);
    }

    @Test
    public void testParseExp() throws Exception {
        Parser parser = new Parser("src/provided/tests/in");
        AST ast = parser.parse();
        assertEquals(ast.class, KeyWord);

    }

}
