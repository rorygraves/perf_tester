package org.perftester.process.comms.input;

public class ScalacGlobalConfig extends InputCommand {
    public final String id;
    //may be null
    public final String outputDirectory;
    //may be null
    public final String[] classPath;
    //may be null
    public final String[] otherParams;
    //may be null
    public final String[] files;

    public ScalacGlobalConfig(String id, String outputDirectory, String[] classPath, String[] otherParams, String[] files) {
        super(InputType.ScalacGlobalConfig);
        this.id = id;
        this.outputDirectory = outputDirectory;
        this.classPath = classPath;
        this.otherParams = otherParams;
        this.files = files;
    }
}
