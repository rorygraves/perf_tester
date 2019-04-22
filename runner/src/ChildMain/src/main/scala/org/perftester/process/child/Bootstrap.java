package org.perftester.process.child;

public class Bootstrap {
    public static void main(String[] args) {
        System.out.println("Start bootstrap");
        ChildMainConfig cmd = new ChildMainConfig("localhost", Integer.parseInt(args[0]));
        new ChildMain(cmd);
        System.out.println("End bootstrap");
    }
}
