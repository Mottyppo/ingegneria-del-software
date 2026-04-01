package it.unibs.ingesw.controller;

import it.unibs.ingesw.application.ApplicationContext;
import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.service.AuthenticationService;
import it.unibs.ingesw.service.ProposalLifecycleService;
import it.unibs.ingesw.service.ProposalService;
import it.unibs.ingesw.ui.ParticipantInteraction;

import java.util.List;

/**
 * Coordinates CLI workflows dedicated to participants.
 *
 * <p>The controller manages the participant flow while keeping all terminal I/O
 * inside {@link ParticipantInteraction}.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Handles participant login and sign-up.</li>
 *   <li>Shows the board and subscriptions menu.</li>
 *   <li>Handles personal-space notification management.</li>
 * </ul>
 */
public class ParticipantController {
    private final AuthenticationService authenticationService;
    private final ProposalService proposalService;
    private final ProposalLifecycleService proposalLifecycleService;
    private final ParticipantInteraction interaction;

    /**
     * Creates a participant controller bound to the given application context.
     *
     * @param context The application context used to execute use cases.
     */
    public ParticipantController(ApplicationContext context) {
        this.authenticationService = context.getAuthenticationService();
        this.proposalService = context.getProposalService();
        this.proposalLifecycleService = context.getProposalLifecycleService();
        this.interaction = new ParticipantInteraction();
    }

    /**
     * Starts the participant area and returns when the user chooses to go back.
     */
    public void start() {
        proposalLifecycleService.refreshProposalLifecycle();
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

    /**
     * Handles participant sign-up.
     */
    private void signUpParticipant() {
        String name = interaction.readSignUpName();
        String surname = interaction.readSignUpSurname();
        String username = interaction.readSignUpUsername();
        String password = interaction.readSignUpPassword();

        Participant created = authenticationService.signUpParticipant(name, surname, username, password);
        if (created != null) {
            interaction.printSignUpSuccess();
            return;
        }
        interaction.printSignUpFailure();
    }

    /**
     * Handles participant login.
     *
     * @return The authenticated participant, or {@code null} if credentials are invalid.
     */
    private Participant loginParticipant() {
        String username = interaction.readLoginUsername();
        String password = interaction.readLoginPassword();
        Participant participant = authenticationService.authenticateParticipant(username, password);
        if (participant == null) {
            interaction.printInvalidCredentials();
            return null;
        }
        proposalLifecycleService.refreshProposalLifecycle();
        return participant;
    }

    /**
     * Displays and handles the participant main menu.
     *
     * @param participant The authenticated participant.
     */
    private void participantMainMenu(Participant participant) {
        boolean logout = false;
        while (!logout) {
            int choice = interaction.chooseMainMenu();
            switch (choice) {
                case 0 -> logout = true;
                case 1 -> {
                    proposalLifecycleService.refreshProposalLifecycle();
                    interaction.showBoard(proposalService.getBoardByCategory());
                }
                case 2 -> subscribeToOpenProposal(participant);
                case 3 -> openPersonalSpace(participant);
                default -> {
                    // Menu handles bounds; this branch is kept for defensive completeness.
                }
            }
        }
    }

    /**
     * Handles subscription to an open proposal.
     *
     * @param participant The participant subscribing to a proposal.
     */
    private void subscribeToOpenProposal(Participant participant) {
        proposalLifecycleService.refreshProposalLifecycle();
        List<Proposal> openProposals = proposalService.getOpenProposals();
        int index = interaction.chooseOpenProposal(openProposals);
        if (index < 0) {
            return;
        }

        Proposal selected = openProposals.get(index);
        boolean subscribed = proposalService.subscribeParticipantToProposal(participant, selected.getId());
        interaction.printSubscriptionResult(subscribed);
    }

    /**
     * Displays and handles the participant personal space.
     *
     * @param participant The participant owner of the personal space.
     */
    private void openPersonalSpace(Participant participant) {
        proposalLifecycleService.refreshProposalLifecycle();
        List<Notification> notifications = proposalService.getParticipantNotifications(participant);
        interaction.showNotifications(notifications);

        if (notifications.isEmpty() || !interaction.askDeleteNotification()) {
            return;
        }

        int index = interaction.chooseNotificationToDelete(notifications);
        if (index < 0) {
            return;
        }

        boolean removed = proposalService.removeParticipantNotification(participant, index);
        interaction.printNotificationRemoveResult(removed);
    }
}
