package it.unibs.ingesw;

import it.unibs.ingesw.application.ApplicationContext;
import it.unibs.ingesw.controller.UserController;

/**
 * Application entry point
 *
 * <p>The class initializes the application context and starts the command-line
 * interaction flow.</p>
 */
public class Main {
    /**
     * Starts the application lifecycle.
     */
    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContext();
        UserController controller = new UserController(context);
        controller.start();
        controller.end();
    }
}
