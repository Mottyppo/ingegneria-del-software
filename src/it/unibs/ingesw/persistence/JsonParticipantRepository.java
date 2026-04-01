package it.unibs.ingesw.persistence;

import com.google.gson.reflect.TypeToken;
import it.unibs.ingesw.model.Participant;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON-backed implementation of {@link ParticipantRepository}.
 */
public class JsonParticipantRepository extends JsonRepositorySupport implements ParticipantRepository {
    private static final String PARTICIPANTS_FILE = "participants.json";

    @Override
    public List<Participant> readAll() {
        Type listType = new TypeToken<List<Participant>>() {
        }.getType();
        List<Participant> participants = readJson(resolve(PARTICIPANTS_FILE), listType, new ArrayList<>());
        return participants == null ? new ArrayList<>() : participants;
    }

    @Override
    public void writeAll(List<Participant> participants) {
        writeJson(resolve(PARTICIPANTS_FILE), participants);
    }
}
