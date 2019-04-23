package org.perftester.process.comms.input;

public class Exit extends InputCommand {
    public Exit() {
        super(InputType.Exit);
    }
    @Override
    public String toString() {
        return "Command - Exit]";
    }
}
