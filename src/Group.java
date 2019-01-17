import java.util.ArrayList;

public class Group {

    private String groupname;
    private ClientThread groupHost;
    private ArrayList<ClientThread> groupMembers = new ArrayList<>();

    Group(String groupname, ClientThread groupHost){
        this.groupname = groupname;
        this.groupHost = groupHost;
        groupMembers.add(groupHost);
    }

    public String getGroupname() {
        return groupname;
    }

    public ClientThread getGroupHost() {
        return groupHost;
    }

    public ArrayList<ClientThread> getGroupMembers() {
        return groupMembers;
    }

    public void addGroupMember(ClientThread groupmember) {
        this.groupMembers.add(groupmember);
    }

    public void removeGroupMember(ClientThread Groupmember){
        this.groupMembers.remove(Groupmember);
    }
}
