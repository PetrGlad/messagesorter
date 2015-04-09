package petrglad.msgsort.stub;

import static spark.Spark.*;

public class DestinationStub {
    public static void main(String[] args) {
        port(9200);
        get("/incoming", (req, res) -> {
                System.out.println("MSG " + req.queryString());
                return "OK"; });
    }
}

