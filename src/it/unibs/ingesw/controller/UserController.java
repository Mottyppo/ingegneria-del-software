package it.unibs.ingesw.controller;

import it.unibs.ingesw.application.ApplicationContext;
import it.unibs.ingesw.ui.UserInteraction;

/**
 * Coordinates the top-level access area of the application.
 *
 * <p>The controller routes the user to the configurator or participant flow,
 * while delegating all terminal rendering to the dedicated interaction classes.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Shows the application banner and title.</li>
 *   <li>Routes the user to the role-specific controllers.</li>
 *   <li>Prints the shutdown message at the end of the session.</li>
 * </ul>
 */
public class UserController {
    private final UserInteraction interaction;
    private final ConfiguratorController configuratorController;
    private final ParticipantController participantController;

    /**
     * Creates the entry-point controller bound to the given application context.
     *
     * @param context The application context used by downstream controllers.
     */
    public UserController(ApplicationContext context) {
        this.interaction = new UserInteraction();
        this.configuratorController = new ConfiguratorController(context);
        this.participantController = new ParticipantController(context);
    }

    /**
     * Starts the complete interactive flow by selecting a role-specific area.
     */
    public void start() {
        interaction.clearConsole();
        interaction.printBanner();
        interaction.printApplicationTitle();

        boolean exit = false;
        while (!exit) {
            int choice = interaction.chooseAccessArea();
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> configuratorController.start();
                case 2 -> participantController.start();
                default -> {
                    // Menu already validates options; defensive fallback.
                }
            }
        }
    }

    /**
     * Ends the interactive session by printing the closing message.
     */
    public void end() {
        interaction.printProgramClosure();
    }
}
