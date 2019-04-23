package org.perftester.process.comms.input;

public class Run extends InputCommand {
    public final String className;
    public final String[] args;

    public Run(String className, String[] args) {
        super(InputType.Run);
        this.className = className;
        this.args = args;
    }

    @Override
    public String toString() {
        return "Command - Run";
    }
}
