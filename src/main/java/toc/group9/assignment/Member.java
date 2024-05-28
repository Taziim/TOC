package toc.group9.assignment;

public class Member {
    
    private String role;
    private String name;
    private String id;
    private String participant;
    private String task;

    public Member() {}

    public Member(String role, String name, String id, String participant, String task) {
        this.role = role;
        this.name = name;
        this.id = id;
        this.participant = participant;
        this.task = task;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getParticipant() {
        return participant;
    }

    public String getTask() {
        return task;
    }
}
