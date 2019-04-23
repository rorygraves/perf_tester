package org.perftester.process.comms.input;

public class Gc extends InputCommand {
    public Gc() {
        super(InputType.Gc);
    }
    @Override
    public String toString() {
        return "Command - Gc";
    }

}
