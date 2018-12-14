import java.io.*;
import java.net.Socket;

public class PingThread implements Runnable {

    private Socket socket;
    private ClientThread parent;

    PingThread(ClientThread parent, Socket socket) {
        super();
        this.socket = socket;
        this.parent = parent;
    }

    @Override
    public void run() {
        longWait();
    }

    void longWait() {
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if(parent.isConnected()) {
                parent.setPingPong(false);
                OutputStream os = socket.getOutputStream();

                // Send message using the print writer.
                PrintWriter writer = new PrintWriter(os);

                writer.println("PING");
                // The flush method sends the messages from the print writer buffer to client.
                writer.flush();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!parent.isPingPong()) {
                    String response = "DSCN Pong timeout";
                    writer.println(response);
                    System.out.println(response);
                    // The flush method sends the messages from the print writer buffer to client.
                    writer.flush();
                    parent.setConnected(false);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
