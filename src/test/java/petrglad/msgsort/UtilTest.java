package petrglad.msgsort;

import static org.junit.Assert.assertEquals;

public class UtilTest {

    @org.junit.Test
    public void testMax() throws Exception {
        assertEquals(new Integer(2), Util.max(1, 2));
        assertEquals(new Integer(2), Util.max(2, 1));
        assertEquals(new Integer(1), Util.max(1, 1));
    }
}