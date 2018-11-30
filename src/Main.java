import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private int port = 1337;
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        new Main().run();
    }
    public void run(){
        {



            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Connected");
                while (true) {
                    System.out.println("Looperino");
                    // Wait for an incoming client-connection request (blocking).
                    Socket socket = serverSocket.accept();
                    System.out.println("not here");
                    // Your code here:
                    // TODO: Start a message processing thread for each connecting client.
                    ClientThread client = new ClientThread(socket);
                    Thread t1 = new Thread(client);
                    t1.start();
//                    PingThread ping = new PingThread(socket);
//                    Thread p1 = new Thread(ping);
//                    p1.start();
                    // TODO: Start a ping thread for each connecting client.
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
