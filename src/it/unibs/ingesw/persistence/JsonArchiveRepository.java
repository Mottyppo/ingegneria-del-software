package it.unibs.ingesw.persistence;

import it.unibs.ingesw.model.Archive;

/**
 * JSON-backed implementation of {@link ArchiveRepository}.
 */
public class JsonArchiveRepository extends JsonRepositorySupport implements ArchiveRepository {
    private static final String PROPOSALS_FILE = "proposals.json";

    @Override
    public Archive read() {
        Archive archive = readJson(resolve(PROPOSALS_FILE), Archive.class, new Archive());
        return archive == null ? new Archive() : archive;
    }

    @Override
    public void write(Archive archive) {
        writeJson(resolve(PROPOSALS_FILE), archive);
    }
}
