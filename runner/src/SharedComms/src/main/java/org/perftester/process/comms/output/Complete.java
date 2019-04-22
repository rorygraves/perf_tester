package org.perftester.process.comms.output;

import org.perftester.process.comms.input.InputCommand;

public class Complete extends OutputCommand {
    public final InputCommand input;
    public final long duration;
    public final boolean failed;
    public final String failMessage;

    public Complete(InputCommand input, long duration, boolean failed, String failMessage) {
        super(OutputType.Complete);
        this.input = input;
        this.duration = duration;
        this.failed = failed;
        this.failMessage = failMessage;
    }
}
