

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class ClientThread implements Runnable {

    private Socket socket;

    ClientThread(Socket socket) {
        super();
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Hello fellow clients :)");
        try {
            InputStream is = socket.getInputStream();

            OutputStream os = socket.getOutputStream();
            connectToClient(is, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean connectToClient(InputStream is, OutputStream os) {

        // Send message using the print writer.
        PrintWriter writer = new PrintWriter(os);
        writer.println("HELO");
        System.out.println("HELO");
        // The flush method sends the messages from the print writer buffer to client.
        writer.flush();

        // Block thread until socket input has been read.
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //start encoding
        System.out.println(line);
        byte[] bytesOfMessage = new byte[0];
        if (line != null) {
            String username = line.replace("HELO ", "");
            System.out.println(username);

            try {
                bytesOfMessage = line.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String encoded = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] MD5Bytes = md.digest(bytesOfMessage);
            encoded = Base64.getEncoder().encodeToString(MD5Bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //end encoding

        String response = "+OK " + encoded;
        writer.println(response);
        System.out.println(response);
        // The flush method sends the messages from the print writer buffer to client.
        writer.flush();

        return true;
    }
}
