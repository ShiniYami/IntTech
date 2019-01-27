import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;


public class ClientThread implements Runnable {

    private Socket socket;
    private Thread pingThread;
    private String username;
    public Main parent;
    private boolean connected = false;
    private boolean pingPong = false;
    InputStream is = null;
    OutputStream os = null;
    private String publicKey;

    PrintWriter writer;

    ClientThread(Main parent, Socket socket) {
        super();
        this.socket = socket;
        this.parent = parent;
    }

    @Override
    public void run() {


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
        writer = new PrintWriter(os);
        while (connected) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            try {
                line = reader.readLine();
            }
            catch (SocketException ex){
                connected = endConnection();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            if (line.startsWith("BCST")) {
                String message = line.replace("BCST ", "");
                String returnMessage =  parent.broadcastMessage(username,message);
                sendReturnMessage(returnMessage);
            } else if (line.startsWith("WISP")){
                String message = line.replace("WISP ", "");
                String[] splitMessage = message.split(" ");
                String targetUsername = splitMessage[0];
                message = message.replace(targetUsername + " ", "");
                String returnMessage = parent.whisperMessage(username,message, targetUsername);
                sendReturnMessage(returnMessage);
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

                String returnMessage = parent.createGroup(groupname, this);
                sendReturnMessage(returnMessage);
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
                sendReturnMessage(returnMessage);
            } else if (line.startsWith("GRP")){
                String[] split = line.split(" ");
                String groupname = split[1];
                String message = line.replace("GRP " +groupname + " ", "");
                String returnMessage = parent.sendGroupMessage(groupname, this, message);
                sendReturnMessage(returnMessage);
            } else if (line.startsWith("LEVE ")) {
                String[] split = line.split(" ");
                String groupname = split[1];
                String returnMessage = parent.leaveGroup(groupname, this);
                sendReturnMessage(returnMessage);
            } else if (line.startsWith("KICK ")){
                String[] split = line.split(" ");
                String targetUsername = split[1];
                String groupname = split[2];
                String returnMessage = parent.kickFromGroup(groupname, targetUsername, this);
                sendReturnMessage(returnMessage);
            }else if (line.startsWith("FILE ")){
                String[] split = line.split(" ");
                String fileName = split[1];
                String fileTarget = split[2];
                startGatheringSocket(fileName, fileTarget);
            }else if(line.startsWith("KEY ")){
                String[] split = line.split(" ");
                publicKey = split[1];
            }else if(line.startsWith("ASK ")){
                String[] split = line.split(" ");
                String targetName = split[1];
                String targetKey = parent.getPublicKey(targetName);
                sendReturnMessage(targetKey);
            }
            else if (line.startsWith("PONG")) {
                pingPong = true;
                startPingThread();
            } else if (line.startsWith("QUIT")) {
                connected = endConnection();
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        parent.removeUser(this);
    }

    private void startGatheringSocket(String filename, String fileTarget){
        int port = parent.getNewPort();
        ServerSocket serverSocket;
        Socket transferSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            sendReturnMessage("SFILE " + filename + " " + port);
            transferSocket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileTransferThread transferThread = new FileTransferThread(this, transferSocket, filename, fileTarget);
        Thread t1 = new Thread(transferThread);
        t1.start();
    }

    private boolean endConnection(){
        System.out.println(username + " disconnected.");
        writer.println("+SUC Goodbye " + username);
        writer.flush();
        return false;
    }

    public void sendReturnMessage(String message) {
        writer.println(message);
        writer.flush();
    }

    private boolean connectToClient() {
        boolean connected = false;

        // Send message using the print writer.
        PrintWriter writer = new PrintWriter(os);
        writer.println("HELO");
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
        byte[] bytesOfMessage = new byte[0];
        if (line != null) {
            String response;
            String username = line.replace("HELO ", "");
            if(username.matches("^[a-zA-Z0-9_]*$")) {
                if (parent.isUniqueUsername(username)) {
                    this.username = username;
                    System.out.println(username + " connected.");

                    try {
                        bytesOfMessage = line.getBytes("UTF-8");
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
            // The flush method sends the messages from the print writer buffer to client.
            writer.flush();
        }


        return connected;
    }

    public String getPublicKey(){
        return publicKey;
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
        writer.println(message);
        writer.flush();
    }


}
