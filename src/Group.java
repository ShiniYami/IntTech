import java.util.ArrayList;

public class Group {

    private String groupname;
    private String groupHost;
    private ArrayList<String> groupMembers = new ArrayList<>();

    Group(String groupname, String groupHost){
        this.groupname = groupname;
        this.groupHost = groupHost;
        groupMembers.add(groupHost);
    }

    public String getGroupname() {
        return groupname;
    }

    public String getGroupHost() {
        return groupHost;
    }

    public ArrayList<String> getGroupMembers() {
        return groupMembers;
    }

    public void addGroupMember(String groupmember) {
        this.groupMembers.add(groupmember);
    }
}
