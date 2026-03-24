package it.unibs.ingesw;

import it.unibs.ingesw.controller.SystemManager;
import it.unibs.ingesw.io.UserInteraction;

public class Main {
    public static void main(String[] args) {
        SystemManager manager = new SystemManager();
        UserInteraction ui = new UserInteraction(manager);
        ui.start();
        ui.end();
    }
}
