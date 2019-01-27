import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    private static int port = 1337;
    private ServerSocket serverSocket;
    private ArrayList<ClientThread> users = new ArrayList<>();
    private ArrayList<Group> groups = new ArrayList<>();
    private int portGen = 1338;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server Online.");
        while (true) {
            // Wait for an incoming client-connection request (blocking).
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ClientThread client = new ClientThread(this, socket);
            users.add(client);
            Thread t1 = new Thread(client);
            t1.start();
        }


    }

    public String broadcastMessage(String username, String message) {
        boolean sent = false;
        for (ClientThread user : users) {
            if (user.getUsername() != null) {
                if (!user.getUsername().equals(username)) {
                    user.giveMessage("BCST " + username + ": " + message);
                    sent = true;
                }
            }
        }
        if(sent){
            return "SUC " +username+ ": " + message;
        }
        else {
            return "-ERR No other users available.";
        }
    }

    public boolean isUniqueUsername(String username) {
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

    public String createGroup(String groupname, ClientThread currentUser) {
        Group group = new Group(groupname, currentUser);
        groups.add(group);
        return "SUC Group '" + groupname +  "' created.";
    }

    public ArrayList<String> getUsernames() {
        ArrayList<String> usernames = new ArrayList<>();
        for (ClientThread user : users) {
            usernames.add(user.getUsername());
        }
        return usernames;
    }

    public ArrayList<String> getGroupNames() {
        ArrayList<String> groupnames = new ArrayList<>();
        for (Group group : groups) {
            groupnames.add(group.getGroupname());
        }
        return groupnames;
    }

    public String joinGroup(String groupname, ClientThread currentUser) {
        for (Group group : groups) {
            if (group.getGroupname().equals(groupname)) {
                ArrayList<ClientThread> users = group.getGroupMembers();
                for (ClientThread user : users) {
                    if (user.getUsername().equals(currentUser.getUsername())) {
                        return "-ERR Already in this group.";
                    }
                }
                group.addGroupMember(currentUser);
                return "SUC Successfully joined group.";
            }
        }
        return "-ERR No such group exists.";
    }

    public String leaveGroup(String groupname, ClientThread currentUser){
        for (Group group : groups) {
            if (group.getGroupname().equals(groupname)) {
                ArrayList<ClientThread> users = group.getGroupMembers();
                for (ClientThread user : users) {
                    if (user.getUsername().equals(currentUser.getUsername())) {
                        group.removeGroupMember(currentUser);
                        return "SUC Successfully left group.";
                    }
                }
                return "-ERR You are not in this group.";

            }
        }
        return "-ERR No such group exists.";
    }

    public String kickFromGroup(String groupname, String targetUsername, ClientThread currentUser){
        for (Group group : groups) {
            if (group.getGroupname().equals(groupname)) {
                if(group.getGroupHost().equals(currentUser)) {
                    ArrayList<ClientThread> users = group.getGroupMembers();
                    for (ClientThread user : users) {
                        if (user.getUsername().equals(targetUsername)) {
                            group.removeGroupMember(user);
                            user.giveMessage("SUC You got kicked from: " + groupname);
                            return "SUC Successfully kicked " + targetUsername+" from " + groupname + ".";
                        }
                    }
                    return "-ERR User is not in this group.";
                }
                return "-ERR You are not the group host.";

            }
        }
        return "-ERR No such group exists.";
    }

    public String sendGroupMessage(String groupname, ClientThread currentUser, String message) {
        boolean sent = false;
        for (Group group : groups) {
            if (group.getGroupname().equals(groupname)) {
                ArrayList<ClientThread> users = group.getGroupMembers();
                for (ClientThread user : users) {
                    if (user.getUsername().equals(currentUser.getUsername())) {
                        for (ClientThread user2 : users) {
                            if (!user2.getUsername().equals(currentUser.getUsername())) {
                                user2.giveMessage("GRP " + currentUser.getUsername() +"(to: "+groupname+"): " + message);
                                sent = true;
                            }
                        }
                        if(sent){
                            return "SUC " +currentUser.getUsername()+ " to " + groupname + ": " + message;
                        }
                        else{
                            return "-ERR No user got your message.";
                        }
                    }
                }
                return "-ERR You are not in this group.";
            }
        }
        return "-ERR No such group exists.";
    }

    public String sendFilePort(int port, String targetUser, String filename) {
        for (ClientThread user: users) {
            if(user.getUsername().equals(targetUser)){
                user.giveMessage("RFILE "+ filename + " " + port);
                return "SUC File successfully sent.";
            }
        }
        return "-ERR No such user exists.";
    }

    public void sendWhisperPort(int port, String targetUser){
        for (ClientThread user: users) {
            if(user.getUsername().equals(targetUser)){
                user.giveMessage("WISPP " + port);
            }
        }
    }

    synchronized int getNewPort(){
        int port = portGen;
        portGen++;
        if(portGen >= 9999){
            portGen = 1338;
        }
        return port;
    }

    public byte[] getPublicKey(String name){

        for (ClientThread user: users) {
            if(user.getUsername().equals(name)){
                return user.getPublicKey();
            }
        }

        return null;
    }

    public void removeUser(ClientThread user){
        users.remove(user);
    }


    public ArrayList<ClientThread> getUsers() {
        return users;
    }
}
