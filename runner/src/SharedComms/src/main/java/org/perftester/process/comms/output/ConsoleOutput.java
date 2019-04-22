package org.perftester.process.comms.output;

public class ConsoleOutput extends OutputCommand {
   public final boolean err;
   public final byte[] text;

    public ConsoleOutput(boolean err, byte[] text) {
        super(OutputType.ConsoleOutput);
        this.err = err;
        this.text = text;
    }
}
