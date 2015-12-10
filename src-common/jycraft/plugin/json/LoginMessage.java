package jycraft.plugin.json;


public class LoginMessage {
    private String type;
    private String password;
    private Status status;

    public LoginMessage(String type, Status status) {
        this.type = type;
        this.status = status;
    }
}
