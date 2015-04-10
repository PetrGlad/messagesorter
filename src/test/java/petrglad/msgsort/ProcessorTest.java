package petrglad.msgsort;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ProcessorTest {
    @Test
    public void testBatch() throws Exception {
        List<Message> calls = new ArrayList<Message>(0);

        Processor p = new Processor(3, message -> {
            calls.add(message);
        });

        assertEquals(0, calls.size());
        p.add(new Message("2015-04-10T14:01:06.771;-170267704"));
        assertEquals(0, calls.size());
        p.add(new Message("2015-04-10T14:00:58.522;607166987"));
        assertEquals(0, calls.size());
        p.add(new Message("2015-04-10T14:00:02.49;-672649217"));
        assertEquals(0, calls.size());
        p.add(new Message("2015-04-10T14:01:23.174;-1282533670"));
        assertEquals(0, calls.size());
        p.add(new Message("2015-04-10T13:59:43.997;-1447715468"));
        assertEquals(0, calls.size());

        p.run();
        assertEquals(2, calls.size());

        p.add(new Message("2015-04-10T10:59:59.902;1105561162")); // Too old one - should be discarded

        p.shutdown();
        assertEquals(5, calls.size());

        Message[] calls2 = calls.toArray(new Message[calls.size()]);
        Arrays.sort(calls2, (a, b) -> a.timestamp.compareTo(b.timestamp));
        assertArrayEquals(calls2, calls.toArray(new Message[calls.size()]));
    }
}