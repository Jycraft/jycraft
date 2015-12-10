package jycraft.plugin.json;


public class ExecuteMessage {
    private String type;
    private String command;
    private String result;
    private ExecuteException exception;
    private Status status;

    public ExecuteMessage(String type, String command, Status status) {
        this.type = type;
        this.command = command;
        this.status = status;
    }

    public ExecuteMessage(String type, Status status, String result) {
        this.type = type;
        this.result = result;
        this.status = status;
    }

    public ExecuteMessage(String type, ExecuteException exception, Status status){
        this.type = type;
        this.exception = exception;
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public ExecuteException getException() {
        return exception;
    }

    public void setException(ExecuteException exception) {
        this.exception = exception;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
