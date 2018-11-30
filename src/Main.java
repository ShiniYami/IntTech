import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected");
                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                clientSocket = serverSocket.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
