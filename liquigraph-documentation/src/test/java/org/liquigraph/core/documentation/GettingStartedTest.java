/**
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.liquigraph.core.documentation;

import com.google.common.base.Charsets;
import com.tngtech.jgiven.junit.SimpleScenarioTest;
import org.junit.Test;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.liquigraph.core.documentation.xml.XmlMarshaller;
import org.liquigraph.core.model.Changeset;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static java.nio.file.Files.readAllBytes;
import static org.assertj.core.api.Assertions.assertThat;

public class GettingStartedTest extends SimpleScenarioTest<GettingStartedTest.FundamentalsSteps> {

    @Test @GettingStarted
    public void _1_彡_a_changeset_is_a_migration_defined_in_Cypher() throws Exception {
        given().a_Cypher_migration("MATCH (n:Hello) SET n.contents = 'World' RETURN n");

        when().defined_with_Liquigraph();

        then().it_is_written_as_such(
            "<changeset author=\"author\" contexts=\"\" id=\"id\" run-always=\"false\" run-on-change=\"false\">\n" +
            "    <query>MATCH (n:Hello) SET n.contents = 'World' RETURN n</query>\n" +
            "</changeset>");
    }

    @Test @GettingStarted
    public void _2_彡_a_changeset_can_define_several_queries() throws Exception {
        given().several_Cypher_migrations(
                "MATCH (n:Bonjour) SET n.contents = 'Monde' RETURN n",
                "MATCH (n:Hello) SET n.contents = 'World' RETURN n"
        );

        when().defined_with_Liquigraph();

        then().it_is_written_as_such(
                "<changeset author=\"author\" contexts=\"\" id=\"id\" run-always=\"false\" run-on-change=\"false\">\n" +
                "    <query>MATCH (n:Bonjour) SET n.contents = 'Monde' RETURN n</query>\n" +
                "    <query>MATCH (n:Hello) SET n.contents = 'World' RETURN n</query>\n" +
                "</changeset>");
    }

    @Test @GettingStarted
    public void _3_彡_a_changelog_file_is_Liquigraph_entry_point_and_contains_changesets() throws Exception {
        given().a_changelog_file("getting_started/changelog.xml");

        when().it_contains(
            "<changelog>\n" +
            "    <changeset id=\"id\" author=\"author\">\n" +
            "        <query>MATCH (n:Hello) SET n.contents = 'World' RETURN n</query>\n" +
            "    </changeset>\n" +
            "</changelog>");

        then().it_can_be_run_by_Liquigraph();
    }

    //TODO: schema

    public static class FundamentalsSteps {

        private final XmlMarshaller marshaller = new XmlMarshaller();
        private Collection<String> queries;
        private Changeset changeset;
        private String changelogPath;
        private File changelogFile;

        public FundamentalsSteps a_Cypher_migration(String query) {
            this.queries = Collections.singletonList(query);
            return this;
        }

        public FundamentalsSteps several_Cypher_migrations(String... queries) {
            this.queries = Arrays.asList(queries);
            return this;
        }

        public FundamentalsSteps defined_with_Liquigraph() {
            changeset = new Changeset();
            changeset.setId("id");
            changeset.setAuthor("author");
            changeset.setQueries(queries);
            return this;
        }

        public void it_is_written_as_such(String expectedChangesetXml) throws Exception {
            String output = marshaller.serialize(changeset, "changeset", false);

            assertThat(output).isXmlEqualTo(expectedChangesetXml);
        }

        public void a_changelog_file(String changelogPath) throws URISyntaxException {
            this.changelogPath = changelogPath;
        }

        public void it_contains(String contents) throws URISyntaxException, IOException {
            changelogFile = new File(this.getClass().getResource("/" + changelogPath).toURI());
            String fileContents = new String(readAllBytes(changelogFile.toPath()), Charsets.UTF_8);

            assertThat(fileContents).contains(contents);
        }

        public void it_can_be_run_by_Liquigraph() throws IOException {
            new ConfigurationBuilder()
                    .withClassLoader(changelogClassloader())
                    .withMasterChangelogLocation(changelogPath)
                    .withDryRunMode(Files.createTempDirectory("liquigraph-doc"))
                    .withUsername("neo4j")
                    .withPassword("secret")
                    .withUri("jdbc:neo4j://localhost:7474")
                    .build();
        }

        private URLClassLoader changelogClassloader() throws MalformedURLException {
            return new URLClassLoader(new URL[]{changelogFile.toURI().toURL()}, this.getClass().getClassLoader());
        }
    }
}
