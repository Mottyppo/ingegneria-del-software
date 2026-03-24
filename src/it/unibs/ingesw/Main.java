package it.unibs.ingesw;

import it.unibs.ingesw.controller.SystemManager;
import it.unibs.ingesw.ui.UserInteractionManager;

/**
 * Application entry point
 *
 * <p>The class initializes the core manager and starts the command-line interaction flow.</p>
 */
public class Main {
    /**
     * Starts the application lifecycle.
     */
    public static void main(String[] args) {
        SystemManager manager = new SystemManager();
        UserInteractionManager ui = new UserInteractionManager(manager);
        ui.start();
        ui.end();
    }
}
