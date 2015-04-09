package petrglad.msgsort.stub;

import static spark.Spark.*;

/**
 * Testing helper: simulates destination server for outgoing messages.
 */
public class DestinationStub {
    public static void main(String[] args) {
        port(9200);
        get("/incoming", (req, res) -> {
                System.out.println("MSG " + req.queryString());
                return "OK"; });
    }
}

