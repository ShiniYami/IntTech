import java.util.ArrayList;

public class UserData {
    private static UserData ourInstance = new UserData();
    private ArrayList<ClientThread> clients = new ArrayList<>();

    public static UserData getInstance() {
        return ourInstance;
    }

    private UserData() {

    }

    public boolean addClient(ClientThread client){
        boolean userExtists = false;
        for (ClientThread user:clients
             ) {
            if (user.getUsername().equals(client.getUsername())){
                userExtists = true;
            }
        }
        if(!userExtists){
            clients.add(client);
            return true;
        }
        return false;
    }

    public boolean removeClient(String userName){
        for (int i = 0;i<clients.size();i++){
            if(userName.equals(clients.get(i).getUsername())){
                clients.remove(i);
                return true;
            }
        }
        return false;
    }
}
