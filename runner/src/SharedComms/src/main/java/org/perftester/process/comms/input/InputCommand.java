package org.perftester.process.comms.input;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class InputCommand implements Serializable {
    public final InputType commandType;
    public final String id;

    protected InputCommand(InputType commandType) {
        this.commandType = commandType;
        id = UUID.randomUUID().toString();
    }

    public void writeTo(ObjectOutputStream outputStream) throws IOException {
        synchronized (outputStream) {
            outputStream.writeObject(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputCommand that = (InputCommand) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


