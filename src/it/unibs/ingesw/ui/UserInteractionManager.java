package it.unibs.ingesw.ui;

import it.unibs.ingesw.console.format.Alignment;
import it.unibs.ingesw.console.menu.Menu;
import it.unibs.ingesw.controller.SystemManager;

import java.util.List;

/**
 * Coordinates entry-point interaction by routing users to configurator or fruitore areas.
 */
public class UserInteractionManager {

    private static final String ACCESS_MENU_TITLE = "Area di Accesso";
    private static final List<String> ACCESS_MENU_ENTRIES = List.of(
            "Backend Configuratore",
            "Frontend Fruitore"
    );

    private final UserInteraction interaction;
    private final ConfiguratorInteractionManager configuratorInteractionManager;
    private final ParticipantInteractionManager participantInteractionManager;

    /**
     * Creates a coordinator bound to the given system manager.
     *
     * @param manager The system manager that executes business operations.
     */
    public UserInteractionManager(SystemManager manager) {
        this.interaction = new UserInteraction();
        this.configuratorInteractionManager = new ConfiguratorInteractionManager(manager);
        this.participantInteractionManager = new ParticipantInteractionManager(manager);
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
            int choice = chooseAccessArea();
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> configuratorInteractionManager.start();
                case 2 -> participantInteractionManager.start();
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

    private int chooseAccessArea() {
        return new Menu(ACCESS_MENU_TITLE, ACCESS_MENU_ENTRIES, true, Alignment.CENTER, true).choose();
    }
}
