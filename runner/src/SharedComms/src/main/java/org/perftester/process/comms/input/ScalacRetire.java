package org.perftester.process.comms.input;

public class ScalacRetire extends InputCommand {
    public final String id;

    public ScalacRetire(String id) {
        super(InputType.ScalacRetire);
        this.id = id;
    }
}
