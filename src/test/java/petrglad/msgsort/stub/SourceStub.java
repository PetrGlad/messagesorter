package petrglad.msgsort.stub;

import petrglad.msgsort.Messages;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Testing helper: simulates incoming messages source
 */
public class SourceStub {
    public static void main(String[] args) throws IOException {
        Random randValue = new Random();
        Random randDelay = new Random();

        final Supplier<Integer> getPort = getPortSupplier();
        while (true) {
            Integer port = getPort.get();
            try {
                final String message = Messages.formatMessage(
                        LocalDateTime.now().plusSeconds(randValue.nextInt(21) - 10),
                        randValue.nextInt());
                try (Socket s = new Socket("127.0.0.1", port)) {
                    s.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
                }
                System.out.println("SENT " + port + ": " + message.trim());
            } catch (ConnectException e) {
                System.err.println("FAIL " + port + ": " + e.getMessage());
            }
            try {
                Thread.sleep(500 + randDelay.nextInt(1000));
            } catch (InterruptedException e) {
            }
        }
    }

    private static Supplier<Integer> getPortSupplier() {
        int[] ports = new int[]{9100, 9101};
        Random randPort = new Random();
        return () -> ports[randPort.nextInt(ports.length)];
    }
}
