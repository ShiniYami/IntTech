

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;


public class ClientThread implements Runnable {

    private Socket socket;
    private Thread pingThread;
    private String username;
    private Main parent;
    private boolean connected = false;
    private boolean pingPong = false;
    InputStream is = null;
    OutputStream os = null;

    ClientThread(Main parent, Socket socket) {
        super();
        this.socket = socket;
        this.parent = parent;
    }

    @Override
    public void run() {
        System.out.println("Hello fellow clients :)");

        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connected = connectToClient();

        if (connected) {
            pingPong = startPingThread();
        }
        PrintWriter writer = new PrintWriter(os);
        while (connected) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            try {
                line = reader.readLine();
            }
            catch (SocketException ex){
                ex.printStackTrace();
                try {
                    socket.close();
                    connected = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connected = false;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            if (line.startsWith("BCST")) {
                String message = line.replace("BCST ", "");
                parent.broadcastMessage(username,message);
            } else if (line.startsWith("WISP")){
                String message = line.replace("WISP ", "");
                String[] splitMessage = message.split(" ");
                String targetUsername = splitMessage[0];
                message = message.replace(targetUsername + " ", "");
                boolean succeeded = parent.whisperMessage(username,message, targetUsername);
                if(!succeeded){
                    sendErrorMessage(writer, "Username not found");
                }
            } else if (line.startsWith("USRS")) {
                ArrayList<String> usernames = parent.getUsernames();
                String message = "";
                for (String username : usernames) {
                    message = message + username + " ";
                }
                writer.println("USRS " + message);
                writer.flush();
            } else if (line.startsWith("CRTE ")){
                String[] split = line.split(" ");
                String groupname = split[1];

                parent.createGroup(groupname, this);
            } else if (line.startsWith("GRPS")){
                ArrayList<String> groups = parent.getGroupNames();
                String message = "";
                for (String group : groups) {
                    message = message + group + " ";
                }
                writer.println("GRPS " + message);
                writer.flush();
            } else if (line.startsWith("JOIN ")){
                String[] split = line.split(" ");
                String groupname = split[1];
                String returnMessage = parent.joinGroup(groupname, this);
                writer.println(returnMessage);
                writer.flush();
            } else if (line.startsWith("GRP")){
                String[] split = line.split(" ");
                String groupname = split[1];
                String message = line.replace("GRP " +groupname + " ", "");
                parent.sendGroupMessage(groupname, this, message);
            } else if (line.startsWith("LEVE ")) {
                String[] split = line.split(" ");
                String groupname = split[1];
                String returnMessage = parent.leaveGroup(groupname, this);
                writer.println(returnMessage);
                writer.flush();
            } else if (line.startsWith("KICK ")){
                String[] split = line.split(" ");
                String targetUsername = split[1];
                String groupname = split[2];
                String returnMessage = parent.kickFromGroup(groupname, targetUsername, this);
                writer.println(returnMessage);
                writer.flush();
            } else if (line.startsWith("PONG")) {
                pingPong = true;
                startPingThread();
            } else if (line.startsWith("QUIT")) {
                System.out.println(username + " disconnected.");
                writer.println("+OK Goodbye");
                writer.flush();
                connected = false;
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        parent.getUsers().remove(this);
    }

    private void sendErrorMessage(PrintWriter writer, String message) {
        writer.println("-ERR " + message);
        writer.flush();
    }

    private boolean connectToClient() {
        boolean connected = false;

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
            String response;
            String username = line.replace("HELO ", "");
            if(username.matches("^[a-zA-Z0-9_]*$")) {
                if (parent.isUniqueUsername(username)) {
                    this.username = username;
                    System.out.println(username);

                    try {
                        bytesOfMessage = line.getBytes("UTF-8");
                        System.out.println("Message size: " + bytesOfMessage + " bytes");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
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

                    response = "+OK " + encoded;
                    connected = true;
                } else {
                    response = "-ERR user already logged in";
                    this.connected = false;
                }
            }
            else{
                response = "-ERR username has an invalid format";
                this.connected = false;
            }

            writer.println(response);
            System.out.println(response);
            // The flush method sends the messages from the print writer buffer to client.
            writer.flush();
        }


        return connected;
    }

    public boolean startPingThread() {
        PingThread ping = new PingThread(this, socket);
        pingThread = new Thread(ping);
        pingThread.start();
        return true;
    }

    public String getUsername() {
        return username;
    }

    public boolean isPingPong() {
        return pingPong;
    }

    public void setPingPong(boolean pingPong) {
        this.pingPong = pingPong;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

    public void giveMessage(String message){
        PrintWriter writer = new PrintWriter(os);
        writer.println(message);
        writer.flush();
    }
}
