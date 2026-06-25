package it.unibs.ingesw.service.proposal;

import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.PersonalSpace;
import it.unibs.ingesw.persistence.ParticipantRepository;

import java.util.List;

/**
 * Handles participant personal-space notification use cases.
 */
class PersonalSpaceService {
    private final List<Participant> participants;
    private final ParticipantRepository participantRepository;

    PersonalSpaceService(List<Participant> participants, ParticipantRepository participantRepository) {
        this.participants = participants;
        this.participantRepository = participantRepository;
    }

    List<Notification> getParticipantNotifications(Participant participant) {
        if (participant == null) {
            return List.of();
        }
        PersonalSpace personalSpace = participant.getPersonalSpace();
        return personalSpace.getNotifications();
    }

    boolean removeParticipantNotification(Participant participant, int index) {
        if (participant == null) {
            return false;
        }

        boolean removed = participant.getPersonalSpace().removeNotification(index);
        if (removed) {
            participantRepository.writeAll(participants);
        }
        return removed;
    }
}
