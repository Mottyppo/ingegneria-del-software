package it.unibs.ingesw.ui;

import it.unibs.ingesw.controller.SystemManager;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.Proposal;

import java.util.List;

/**
 * Coordinates front-end workflows dedicated to fruitori.
 */
public class ParticipantInteractionManager {

    private final SystemManager manager;
    private final ParticipantInteraction interaction;

    public ParticipantInteractionManager(SystemManager manager) {
        this.manager = manager;
        this.interaction = new ParticipantInteraction();
    }

    /**
     * Starts the fruitore area and returns when the user asks to go back.
     */
    public void start() {
        manager.refreshProposalLifecycle();
        interaction.printFrontEndTitle();

        boolean back = false;
        while (!back) {
            int choice = interaction.chooseAccessMenu();
            switch (choice) {
                case 0 -> back = true;
                case 1 -> {
                    Participant participant = loginParticipant();
                    if (participant != null) {
                        participantMainMenu(participant);
                    }
                }
                case 2 -> signUpParticipant();
                default -> {
                    // Menu handles bounds; this branch is kept for defensive completeness.
                }
            }
        }
    }

    private void signUpParticipant() {
        String name = interaction.readSignUpName();
        String surname = interaction.readSignUpSurname();
        String username = interaction.readSignUpUsername();
        String password = interaction.readSignUpPassword();

        Participant created = manager.signUpParticipant(name, surname, username, password);
        if (created != null) {
            interaction.printSignUpSuccess();
            return;
        }
        interaction.printSignUpFailure();
    }

    private Participant loginParticipant() {
        String username = interaction.readLoginUsername();
        String password = interaction.readLoginPassword();
        Participant participant = manager.authenticateParticipant(username, password);
        if (participant == null) {
            interaction.printInvalidCredentials();
            return null;
        }
        manager.refreshProposalLifecycle();
        return participant;
    }

    private void participantMainMenu(Participant participant) {
        boolean logout = false;
        while (!logout) {
            int choice = interaction.chooseMainMenu();
            switch (choice) {
                case 0 -> logout = true;
                case 1 -> interaction.showBoardByCategory(manager.getBoardByCategory());
                case 2 -> subscribeToOpenProposal(participant);
                case 3 -> openPersonalSpace(participant);
                default -> {
                    // Menu handles bounds; this branch is kept for defensive completeness.
                }
            }
        }
    }

    private void subscribeToOpenProposal(Participant participant) {
        List<Proposal> openProposals = manager.getOpenProposals();
        int index = interaction.chooseOpenProposal(openProposals);
        if (index < 0) {
            return;
        }
        Proposal selected = openProposals.get(index);
        boolean subscribed = manager.subscribeParticipantToProposal(participant, selected.getId());
        interaction.printSubscriptionResult(subscribed);
    }

    private void openPersonalSpace(Participant participant) {
        List<Notification> notifications = manager.getParticipantNotifications(participant);
        interaction.showNotifications(notifications);

        if (notifications.isEmpty() || !interaction.askDeleteNotification()) {
            return;
        }

        int index = interaction.chooseNotificationToDelete(notifications);
        if (index < 0) {
            return;
        }

        boolean removed = manager.removeParticipantNotification(participant, index);
        interaction.printNotificationRemoveResult(removed);
    }
}
