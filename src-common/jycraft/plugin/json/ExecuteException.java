package jycraft.plugin.json;

public class ExecuteException {
    private String exMessage;
    private String stacktrace;

    public ExecuteException (String exMessage, String stacktrace){
        this.exMessage = exMessage;
        this.stacktrace = stacktrace;

    }
}
