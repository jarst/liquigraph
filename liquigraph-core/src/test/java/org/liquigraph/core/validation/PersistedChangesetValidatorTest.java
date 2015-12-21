package org.liquigraph.core.validation;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.liquigraph.core.model.Changeset;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.model.Checksums.checksum;

public class PersistedChangesetValidatorTest {

    private PersistedChangesetValidator validator = new PersistedChangesetValidator();

    @Test
    public void passes_if_nothing_persisted_yet() {
        Collection<String> errors = validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m")),
            Lists.<Changeset>newArrayList()
        );

        assertThat(errors).isEmpty();
    }

    @Test
    public void passes_if_all_existing_changesets_have_not_changed_checksum() {
        Collection<String> errors = validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m")),
            newArrayList(changeset("identifier", "author2", "MATCH m RETURN m"))
        );

        assertThat(errors).isEmpty();
    }

    @Test
    public void passes_if_changesets_have_modified_checksums_but_run_on_change() {
        Collection<String> errors = validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m2 RETURN m2", true)),
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m"))
        );

        assertThat(errors).isEmpty();
    }

    @Test
    public void fails_if_changesets_with_same_id_have_different_checksums() throws Exception {
        Collection<String> errors = validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m")),
            newArrayList(changeset("identifier", "author2", "MATCH (m)-->(z) RETURN m, z"))
        );

        assertThat(errors).containsExactly(
            format(
                "Changeset with ID <identifier> has conflicted checksums.\n" +
                "\t - Declared: <%s>\n" +
                "\t - Persisted: <%s>.",
                checksum("MATCH m RETURN m"),
                checksum("MATCH (m)-->(z) RETURN m, z")
            )
        );
    }

    private Changeset changeset(String identifier, String author, String query, boolean runOnChange) {
        Changeset changeset = changeset(identifier, author, query);
        changeset.setRunOnChange(true);
        return changeset;
    }

    private Changeset changeset(String id, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(id);
        changeset.setAuthor(author);
        changeset.setQuery(query);
        return changeset;
    }
}