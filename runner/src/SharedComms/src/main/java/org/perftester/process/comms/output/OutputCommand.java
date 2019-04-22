package org.perftester.process.comms.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class OutputCommand implements Serializable {
    final OutputType commandType;

    protected OutputCommand(OutputType commandType) {
        this.commandType = commandType;
    }

    public void writeTo(ObjectOutputStream outputStream) throws IOException {
        synchronized (outputStream) {
            outputStream.writeObject(this);
        }
    }
}


