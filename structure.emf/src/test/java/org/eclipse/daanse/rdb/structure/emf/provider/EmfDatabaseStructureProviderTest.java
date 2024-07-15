/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.rdb.structure.emf.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.eclipse.daanse.rdb.structure.api.DatabaseStructureProvider;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseSchema;
import org.eclipse.daanse.rdb.structure.api.model.PhysicalTable;
import org.eclipse.daanse.rdb.structure.api.model.Table;
import org.eclipse.daanse.rdb.structure.emf.AnnotationHelper.SetupMappingProviderWithTestInstance;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@RequireEMF
@RequireConfigurationAdmin
@RequireServiceComponentRuntime
public class EmfDatabaseStructureProviderTest {

    private static String BASE_DIR = System.getProperty("basePath");

    @SetupMappingProviderWithTestInstance
    @Test
    public void loadSimpleFile(
            @InjectService(cardinality = 1, timeout = 2000) ServiceAware<DatabaseStructureProvider> saProvider)
            throws SQLException, InterruptedException, IOException {
        assertThat(saProvider.getServices()).hasSize(1);

        DatabaseStructureProvider provider = saProvider.getService();

        DatabaseCatalog databaseCatalog = provider.get();
        List<? extends DatabaseSchema> dbSchemas = databaseCatalog.getSchemas();

        assertThat(dbSchemas).hasSize(1);
        DatabaseSchema dbSchema = dbSchemas.get(0);
        assertThat(dbSchema).extracting(DatabaseSchema::getName).isEqualTo("s1n");

        List<? extends Table> tables = dbSchema.getTables();

        assertThat(tables).hasSize(2);

        Table pt1 = tables.get(0);
        assertThat(pt1).isInstanceOf(PhysicalTable.class).extracting(Table::getName).isEqualTo("pt1n");

        Table pt2 = tables.get(1);
        assertThat(pt2).isInstanceOf(PhysicalTable.class).extracting(Table::getName).isEqualTo("pt2n");

    }

}
