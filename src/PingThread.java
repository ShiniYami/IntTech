import java.io.*;
import java.net.Socket;

public class PingThread implements Runnable {

    private Socket socket;

    PingThread(Socket socket) {
        super();
        this.socket = socket;
    }

    @Override
    public void run() {
        boolean connected = true;

        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            // Send message using the print writer.
            PrintWriter writer = new PrintWriter(os);


            while (connected) {
                writer.println("PING");
                System.out.println("PING");
                // The flush method sends the messages from the print writer buffer to client.
                writer.flush();

                // Block thread until socket input has been read.
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();

                if (line.equals("PONG")) {
                    System.out.println("PONG");
                } else {
                    connected = false;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
