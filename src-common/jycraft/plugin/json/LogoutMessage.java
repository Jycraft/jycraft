package jycraft.plugin.json;


public class LogoutMessage {
    private String type;
    private Status status;

    public LogoutMessage(String type, Status status) {
        this.type = type;
        this.status = status;
    }
}
