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
package org.eclipse.daanse.rdb.structure.reader;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.eclipse.daanse.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.rdb.structure.api.model.Column;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseSchema;
import org.eclipse.daanse.rdb.structure.api.model.Table;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

class DatabaseServiceImplH2Test {

    private String catalogName = UUID.randomUUID().toString().toUpperCase();

    private DataSource ds() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:memFS:" + catalogName);
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }

    private void setupData(Connection connection) throws SQLException {
        String sql = "Create table test (ID int primary key, name varchar(50), val numeric(10,3) NOT NULL, birthday date NOT NULL, t time NOT NULL)";

        Statement statement = connection.createStatement();

        statement.execute(sql);

        System.out.println("Created test table.");

        sql = "Insert into test (ID, name, val, birthday, t) values (1, 'name', 13.3, '1973-01-07', '18:20:59')";

        int rows = statement.executeUpdate(sql);

        if (rows > 0) {
            System.out.println("Inserted a new row.");
        }
        connection.commit();
    }

    @Test
    void test(@InjectService DatabaseService databaseService) throws SQLException {
        DataSource ds = ds();
        try (Connection connection = ds.getConnection()) {
            setupData(connection);
            MetaInfo metaInfo = databaseService.createMetaInfo(ds.getConnection());
            EmfRdbProvider emfRdbProvider = new EmfRdbProvider(metaInfo);
            DatabaseCatalog databaseCatalog = emfRdbProvider.get();
            assertThat(databaseCatalog).isNotNull();
            assertThat(databaseCatalog.getSchemas()).isNotNull().hasSize(2);
            //PIBLIC
            Optional<? extends DatabaseSchema> oSchema = databaseCatalog.getSchemas().stream().filter(s -> "PUBLIC".equals(s.getName())).findAny();
            assertThat(oSchema).isNotNull().isPresent();
            DatabaseSchema schema = oSchema.get();
            assertThat(schema).isNotNull();
            assertThat(schema.getTables()).isNotNull().hasSize(1);

            Optional<? extends Table> oTable = schema.getTables().stream().filter(t -> "TEST".equals(t.getName())).findAny();
            assertThat(oTable).isNotNull().isPresent();
            Table table = oTable.get();
            assertThat(table).isNotNull();
            assertThat(table.getColumns()).isNotNull().hasSize(5);

            Optional<? extends Column> oColumn = table.getColumns().stream().filter(c -> "ID".equals(c.getName())).findAny();
            assertThat(oColumn).isNotNull().isPresent();
            Column column = oColumn.get();
            assertThat(column).isNotNull();
            assertThat(column.getType()).isEqualTo("integer");
            assertThat(column.getColumnSize()).isEqualTo(32);
            assertThat(column.getDecimalDigits()).isEqualTo(0);
            assertThat(column.getCharOctetLength()).isEqualTo(32);
            assertThat(column.getNullable()).isEqualTo(false);

            oColumn = table.getColumns().stream().filter(c -> "NAME".equals(c.getName())).findAny();
            assertThat(oColumn).isNotNull().isPresent();
            column = oColumn.get();
            assertThat(column).isNotNull();
            assertThat(column.getType()).isEqualTo("varchar");
            assertThat(column.getColumnSize()).isEqualTo(50);
            assertThat(column.getDecimalDigits()).isEqualTo(0);
            assertThat(column.getCharOctetLength()).isEqualTo(50);
            assertThat(column.getNullable()).isEqualTo(true);

            oColumn = table.getColumns().stream().filter(c -> "VAL".equals(c.getName())).findAny();
            assertThat(oColumn).isNotNull().isPresent();
            column = oColumn.get();
            assertThat(column).isNotNull();
            assertThat(column.getType()).isEqualTo("numeric");
            assertThat(column.getColumnSize()).isEqualTo(10);
            assertThat(column.getDecimalDigits()).isEqualTo(3);
            assertThat(column.getCharOctetLength()).isEqualTo(10);
            assertThat(column.getNullable()).isEqualTo(false);

            oColumn = table.getColumns().stream().filter(c -> "BIRTHDAY".equals(c.getName())).findAny();
            assertThat(oColumn).isNotNull().isPresent();
            column = oColumn.get();
            assertThat(column).isNotNull();
            assertThat(column.getType()).isEqualTo("date");
            assertThat(column.getColumnSize()).isEqualTo(10);
            assertThat(column.getDecimalDigits()).isEqualTo(0);
            assertThat(column.getCharOctetLength()).isEqualTo(10);
            assertThat(column.getNullable()).isEqualTo(false);

            oColumn = table.getColumns().stream().filter(c -> "T".equals(c.getName())).findAny();
            assertThat(oColumn).isNotNull().isPresent();
            column = oColumn.get();
            assertThat(column).isNotNull();
            assertThat(column.getType()).isEqualTo("time");
            assertThat(column.getColumnSize()).isEqualTo(8);
            assertThat(column.getDecimalDigits()).isEqualTo(0);
            assertThat(column.getCharOctetLength()).isEqualTo(8);
            assertThat(column.getNullable()).isEqualTo(false);

        }
    }

}
