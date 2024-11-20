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

import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.sql.RowSetMetaData;

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.api.meta.StructureInfo;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.ImportedKey;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableDefinition;
import org.eclipse.daanse.rdb.structure.emf.rdbstructure.Column;
import org.eclipse.daanse.rdb.structure.emf.rdbstructure.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.emf.rdbstructure.DatabaseSchema;
import org.eclipse.daanse.rdb.structure.emf.rdbstructure.Link;
import org.eclipse.daanse.rdb.structure.emf.rdbstructure.RelationalDatabaseFactory;
import org.eclipse.daanse.rdb.structure.emf.rdbstructure.Table;

public class EmfRdbProvider implements Supplier<DatabaseCatalog> {

    private final StructureInfo structureInfo;
    private AtomicInteger counterSchema = new AtomicInteger();

    public EmfRdbProvider(MetaInfo metaInfo) throws SQLException {
        structureInfo = metaInfo.structureInfo();
    }

    @Override
    public DatabaseCatalog get() {
        return getDatabaseCatalog();
    }

    private DatabaseCatalog getDatabaseCatalog() {
        try {
            DatabaseCatalog databaseCatalog = RelationalDatabaseFactory.eINSTANCE.createDatabaseCatalog();
            Collection<? extends DatabaseSchema> schemas = getSchemas();
            Collection<? extends Link> links = getLinks(schemas);
            databaseCatalog.getSchemas().addAll(schemas);
            databaseCatalog.getLinks().addAll(links);
            return databaseCatalog;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Collection<? extends Link> getLinks(Collection<? extends DatabaseSchema> schemas) throws SQLException {
        List<Link> links = new ArrayList<>();
        for (ImportedKey importedKey : structureInfo.importedKeys()) {
            if (importedKey.primaryKeyColumn() != null && importedKey.primaryKeyColumn().table() != null
                    && importedKey.primaryKeyColumn().table().isPresent()
                    && importedKey.primaryKeyColumn().table().get().schema().isPresent()
                    && importedKey.foreignKeyColumn() != null && importedKey.foreignKeyColumn().table() != null
                    && importedKey.foreignKeyColumn().table().isPresent()) {
                SchemaReference schema = importedKey.primaryKeyColumn().table().get().schema().get();
                Optional<? extends DatabaseSchema> oSchema = schemas.stream()
                        .filter(s -> s.getName().equals(schema.name())).findFirst();
                if (oSchema.isPresent()) {
                    final String tableNamePk = importedKey.primaryKeyColumn().table().get().name();
                    final String columNamePk = importedKey.primaryKeyColumn().name();

                    final String tableNameFk = importedKey.foreignKeyColumn().table().get().name();
                    final String columNameFk = importedKey.foreignKeyColumn().name();

                    Optional<Column> oPrimaryKey = getColumn(oSchema.get(), tableNamePk, columNamePk);
                    Optional<Column> oForeignKey = getColumn(oSchema.get(), tableNameFk, columNameFk);

                    if (oPrimaryKey.isPresent() && oForeignKey.isPresent()) {
                        Link link = RelationalDatabaseFactory.eINSTANCE.createLink();
                        link.setPrimaryKey(oPrimaryKey.get());
                        link.setForeignKey(oForeignKey.get());
                        links.add(link);
                    }
                }
            }
        }
        return links;
    }

    private Optional<Column> getColumn(DatabaseSchema schema, String tableNamePk, String columNamePk) {
        Optional<Table> oTable = schema.getTables().stream().filter(t -> t.getName().equals(tableNamePk)).findFirst();
        if (oTable.isPresent()) {
            Table t = oTable.get();
            List<Column> columns = t.getColumns();
            if (columns != null) {
                return columns.stream().filter(c -> c.getName().equals(columNamePk)).findFirst();
            }
        }
        return Optional.empty();
    }

    private Collection<? extends DatabaseSchema> getSchemas() {
        List<DatabaseSchema> schemas = new ArrayList<>();
        for (SchemaReference schemaReference : structureInfo.schemas()) {
            String catalogName = null;
            if (schemaReference.catalog().isPresent()) {
                catalogName = schemaReference.catalog().get().name();
            }
            final String schemaName = schemaReference.name();
            DatabaseSchema databaseSchema = RelationalDatabaseFactory.eINSTANCE.createDatabaseSchema();
            databaseSchema.setId("s_" + counterSchema.incrementAndGet());
            databaseSchema.setName(schemaName);
            databaseSchema.getTables().addAll(getTables(catalogName, schemaName, databaseSchema));
            schemas.add(databaseSchema);
        }
        return schemas;
    }

    private Collection<? extends Table> getTables(String catalog, String schemaName, DatabaseSchema databaseSchema) {
        List<Table> tabes = new ArrayList<>();
        for (TableDefinition tableDefinition : structureInfo.tables()) {
            if (tableDefinition.table().schema().isPresent()
                    && tableDefinition.table().schema().get().name().equals(schemaName)) {
                final String tableName = tableDefinition.table().name();
                final String tableType = tableDefinition.table().type();
                if (tableType.equals("TABLE") || tableType.equals("BASE TABLE")) {
                    Table table = RelationalDatabaseFactory.eINSTANCE.createPhysicalTable();
                    Collection<? extends Column> columns = getColumns(catalog, schemaName, tableName, table);
                    table.setName(tableName);
                    table.getColumns().addAll(columns);
                    table.setSchema(databaseSchema);
                    table.setDescription("table " + tableName);
                    tabes.add(table);
                }
                if (tableType.equals("VIEW")) {
                    Table table = RelationalDatabaseFactory.eINSTANCE.createViewTable();
                    Collection<? extends Column> columns = getColumns(catalog, schemaName, tableName, table);
                    table.setName(tableName);
                    table.getColumns().addAll(columns);
                    table.setSchema(databaseSchema);
                    table.setDescription("view " + tableName);
                    tabes.add(table);
                }
                if (tableType.equals("SYSTEM TABLE")) {
                    Table table = RelationalDatabaseFactory.eINSTANCE.createSystemTable();
                    Collection<? extends Column> columns = getColumns(catalog, schemaName, tableName, table);
                    table.setName(tableName);
                    table.getColumns().addAll(columns);
                    table.setSchema(databaseSchema);
                    table.setDescription("system table " + tableName);
                    tabes.add(table);
                }
            }
        }

        return tabes;
    }

    public List<Column> getColumns(String catalog, String schema, String tableName, Table table) {
        List<Column> columns = new ArrayList<>();
        for (ColumnDefinition columnDefinition : structureInfo.columns()) {
            if (columnDefinition.column().table() != null && columnDefinition.column().table().isPresent()
                    && columnDefinition.column().table().get().name().equals(tableName)
                    //&& columnDefinition.column().table().get().schema().isPresent()
                    //&& columnDefinition.column().table().get().schema().get().name().equals(schema)
            ) {
                final String columName = columnDefinition.column().name();
                JDBCType jdbcType = columnDefinition.columnMetaData().dataType();
                final OptionalInt columnSize = columnDefinition.columnMetaData().columnSize();
                final OptionalInt decimalDigits = columnDefinition.columnMetaData().decimalDigits();
                final OptionalInt numPrecRadix = columnDefinition.columnMetaData().numPrecRadix();
                final OptionalInt nullable = columnDefinition.columnMetaData().nullable();
                final OptionalInt charOctetLength = columnDefinition.columnMetaData().charOctetLength();

                Column column = RelationalDatabaseFactory.eINSTANCE.createColumn();
                column.setName(columName);
                column.setType(jdbcType.getName().toLowerCase());
                column.setTable(table);
                decimalDigits.ifPresent(i -> column.setDecimalDigits(i));
                columnSize.ifPresent(i -> column.setColumnSize(i));
                numPrecRadix.ifPresent(i -> column.setNumPrecRadix(i));
                charOctetLength.ifPresent(i -> column.setCharOctetLength(i));
                nullable.ifPresent(i -> column.setNullable(i == RowSetMetaData.columnNullable));
                column.setDescription(columnDefinition.columnMetaData().remarks().orElse(null));
                columns.add(column);

            }
        }
        return columns;
    }

}
