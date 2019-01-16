import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    private static int port = 1337;
    private ServerSocket serverSocket;
    private ArrayList<ClientThread> users = new ArrayList<>();

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Connected");
        while (true) {
            System.out.println("Looperino");
            // Wait for an incoming client-connection request (blocking).
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("not here");
            // Your code here:
            // TODO: Start a message processing thread for each connecting client.
            ClientThread client = new ClientThread(this, socket);
            users.add(client);
            Thread t1 = new Thread(client);
            t1.start();
            // TODO: Start a ping thread for each connecting client.
        }


    }

    void broadcastMessage(String username, String message) {
        for (ClientThread user : users) {
            if (user.getUsername() != null) {
                if (!user.getUsername().equals(username)) {
                    user.giveMessage("BCST " + username + ": " + message);
                }
            }
        }
    }

    boolean whisperMessage(String username, String message, String targetUsername){
        for(ClientThread user : users){
            if(user.getUsername().equals(targetUsername)){
                user.giveMessage("WISP " + username +"(to: "+ targetUsername + "): " + message);
                return true;
            }
        }
        return false;
    }

    boolean isUniqueUsername(String username) {
        boolean unique = true;
        for (ClientThread user : users) {
            if (user.getUsername() != null) {
                if (user.getUsername().equals(username)) {
                    unique = false;
                }
            }
        }
        return unique;
    }

    public ArrayList<ClientThread> getUsers() {
        return users;
    }
}
