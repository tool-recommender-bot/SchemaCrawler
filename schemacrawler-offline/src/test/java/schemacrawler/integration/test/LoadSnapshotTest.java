/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2019, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/
package schemacrawler.integration.test;


import static java.nio.file.Files.size;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static schemacrawler.utility.SchemaCrawlerUtility.getCatalog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.test.utility.BaseSchemaCrawlerTest;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;
import schemacrawler.tools.integration.serialization.XmlSerializedCatalog;
import sf.util.IOUtility;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
public class LoadSnapshotTest
  extends BaseSchemaCrawlerTest
{

  private Path serializedCatalogFile;

  @Test
  public void loadSnapshot()
    throws Exception
  {
    final FileInputStream inputFileStream = new FileInputStream(serializedCatalogFile
      .toFile());
    final XmlSerializedCatalog serializedCatalog = new XmlSerializedCatalog(inputFileStream);

    final Schema schema = serializedCatalog.lookupSchema("PUBLIC.BOOKS")
      .orElse(null);
    assertThat("Could not obtain schema", schema, notNullValue());
    assertThat("Unexpected number of tables in the schema",
               serializedCatalog.getTables(schema),
               hasSize(10));
  }

  @BeforeEach
  public void serializeCatalog(final Connection connection)
    throws SchemaCrawlerException, IOException
  {

    final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsWithMaximumSchemaInfoLevel();

    final Catalog catalog = getCatalog(connection, schemaCrawlerOptions);
    assertThat("Could not obtain catalog", catalog, notNullValue());
    assertThat("Could not find any schemas",
               catalog.getSchemas(),
               not(empty()));

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").orElse(null);
    assertThat("Could not obtain schema", schema, notNullValue());
    assertThat("Unexpected number of tables in the schema",
               catalog.getTables(schema),
               hasSize(10));

    serializedCatalogFile = IOUtility.createTempFilePath("schemacrawler",
                                                         "ser");

    final XmlSerializedCatalog serializedCatalog = new XmlSerializedCatalog(catalog);
    serializedCatalog
      .save(new FileOutputStream(serializedCatalogFile.toFile()));
    assertThat("Database was not serialized",
               size(serializedCatalogFile),
               greaterThan(0L));

  }

}
