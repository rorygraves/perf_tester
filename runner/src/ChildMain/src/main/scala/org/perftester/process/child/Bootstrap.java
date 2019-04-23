package org.perftester.process.child;

import java.io.File;

public class Bootstrap {
    public static void main(String[] args) {
        System.out.println("Start bootstrap");
        for (String part : System.getProperty("java.class.path").split(File.pathSeparator)) {
            System.out.println(part);
        }

        ChildMainConfig cmd = new ChildMainConfig("localhost", Integer.parseInt(args[0]));
        new ChildMain(cmd);
        System.out.println("End bootstrap");
    }
}
