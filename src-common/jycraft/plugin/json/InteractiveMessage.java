package jycraft.plugin.json;


public class InteractiveMessage extends Message {
    private String result;

    public InteractiveMessage(String type, Status status, String result) {
        super(type, status);
        this.result = result;
    }

    public InteractiveMessage(String type, Status status){
        super(type, status);
    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString(){
        String s;
        s = "{\"type\":\"" + this.type + "\", \"command\":\"" + this.command + "\", \"result\":\"" + this.result + "\"}";
        return s;
    }

}
