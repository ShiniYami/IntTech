import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    private static int port = 1337;
    private ServerSocket serverSocket;
    private ArrayList<ClientThread> users = new ArrayList<>();
    private ArrayList<Group> groups = new ArrayList<>();

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

    boolean whisperMessage(String username, String message, String targetUsername) {
        for (ClientThread user : users) {
            if (user.getUsername().equals(targetUsername)) {
                user.giveMessage("WISP " + username + "(to: YOU): " + message);
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

    public void createGroup(String groupname, ClientThread currentUser) {
        Group group = new Group(groupname, currentUser);
        groups.add(group);
    }

    ArrayList<String> getUsernames() {
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
                return "+OK Successfully joined group.";
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
                        return "+OK Successfully left group.";
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
                            return "+OK Successfully kicked " + targetUsername+" from " + groupname + ".";
                        }
                    }
                    return "-ERR User is not in this group.";
                }
                return "-ERR You are not the group host.";

            }
        }
        return "-ERR No such group exists.";
    }

    public void sendGroupMessage(String groupname, ClientThread currentUser, String message) {
        for (Group group : groups) {
            if (group.getGroupname().equals(groupname)) {
                ArrayList<ClientThread> users = group.getGroupMembers();
                for (ClientThread user : users) {
                    if (user.getUsername().equals(currentUser.getUsername())) {
                        for (ClientThread user2 : users) {
                            if (!user2.getUsername().equals(currentUser.getUsername())) {
                                user2.giveMessage("GRP " + currentUser.getUsername() +"(to: "+groupname+"): " + message);
                            }
                        }
                    }
                }
            }
        }
    }

    public ArrayList<ClientThread> getUsers() {
        return users;
    }
}
