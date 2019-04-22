package org.perftester.process.comms.input;

public class ScalacRun extends InputCommand {
    public final String id;

    public ScalacRun(String id) {
        super(InputType.ScalacRun);
        this.id = id;
    }
}
