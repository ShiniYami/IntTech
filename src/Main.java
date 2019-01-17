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

    public void createGroup(String groupname, String username) {
        Group group = new Group(groupname, username);
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

    public String joinGroup(String groupname, String username) {
        for (Group group : groups) {
            if (group.getGroupname().equals(groupname)) {
                ArrayList<String> users = group.getGroupMembers();
                for (String user : users) {
                    if (user.equals(username)) {
                        return "-ERR Already in this group.";
                    }
                }
                group.addGroupMember(username);
                return "+OK Successfully joined group.";
            }
        }
        return "-ERR No such group exists.";
    }

    public void sendGroupMessage(String groupname, String username, String message) {
        for (Group group : groups) {
            if (group.getGroupname().equals(groupname)) {
                ArrayList<String> usernames = group.getGroupMembers();
                for (String userName : usernames) {
                    if (userName.equals(username)) {
                        for (ClientThread user : users) {
                            if (!user.getUsername().equals(username)) {
                                user.giveMessage("GRP " + username +"(to: "+groupname+"): " + message);
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
