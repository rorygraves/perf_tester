package org.perftester.process.comms.input;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class InputCommand implements Serializable {
    public final InputType commandType;

    protected InputCommand(InputType commandType) {
        this.commandType = commandType;
    }

    public void writeTo(ObjectOutputStream outputStream) throws IOException {
        synchronized (outputStream) {
            outputStream.writeObject(this);
        }
    }
}


