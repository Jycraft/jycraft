package jycraft.plugin.json;

import java.util.HashSet;

public class Message {
    protected String type;
    protected Status status;
    protected String command;
    protected String password;
    protected String result;
    protected String filePath;

    private static final RuntimeTypeAdapterFactory<Message> adapter =
            RuntimeTypeAdapterFactory.of(Message.class);
    private static final HashSet<Class<?>> registeredClasses= new HashSet<Class<?>>();

    static {
        GsonUtils.registerType(adapter);
    }


    private synchronized void registerClass() {
        try {
            if (!this.registeredClasses.contains(this.getClass())) {
                adapter.registerSubtype(this.getClass());
            }
        } catch (IllegalArgumentException iae) {
            System.out.printf("Exception while registering a new class in type adapter %s", iae.getMessage());
        }
    }

    public Message(String type, Status status){
        registerClass();
        this.type = type;
        this.status = status;
    }

    public Message(String type, Status status, String password){
        registerClass();
        this.type = type;
        this.status = status;
        this.password = password;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String flePath) {
        this.filePath = flePath;
    }

    @Override
    public String toString(){
        String s;
        s = "{\"type\":\"" + this.type + "\", \"status\":\"\"" + this.status + "\"\", \"command\":\""+ this.command +"\"}";
        return  s;
    }


}
