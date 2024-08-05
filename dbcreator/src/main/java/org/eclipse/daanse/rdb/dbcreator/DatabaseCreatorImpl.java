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
package org.eclipse.daanse.rdb.dbcreator;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnReference;
import org.eclipse.daanse.jdbc.db.api.schema.DropImportedKey;
import org.eclipse.daanse.jdbc.db.api.schema.ImportedKey;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.api.sql.SqlStatement;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnDefinitionR;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnMetaDataR;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.DropImportedKeyR;
import org.eclipse.daanse.jdbc.db.record.schema.ImportedKeyR;
import org.eclipse.daanse.jdbc.db.record.schema.SchemaReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.TableDefinitionR;
import org.eclipse.daanse.jdbc.db.record.schema.TableReferenceR;
import org.eclipse.daanse.jdbc.db.record.sql.CreateConstraintStatementR;
import org.eclipse.daanse.jdbc.db.record.sql.CreateContainerSqlStatementR;
import org.eclipse.daanse.jdbc.db.record.sql.CreateSchemaSqlStatementR;
import org.eclipse.daanse.jdbc.db.record.sql.DropConstraintStatementR;
import org.eclipse.daanse.jdbc.db.record.sql.DropContainerSqlStatementR;
import org.eclipse.daanse.jdbc.db.record.sql.DropSchemaSqlStatementR;
import org.eclipse.daanse.rdb.structure.api.model.Column;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseSchema;
import org.eclipse.daanse.rdb.structure.api.model.Link;
import org.eclipse.daanse.rdb.structure.api.model.Table;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DatabaseCreatorImpl.class, scope = ServiceScope.SINGLETON)
public class DatabaseCreatorImpl implements DatabaseCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCreatorImpl.class);

    @Override
    public List<SqlStatement> createSqlStatements(DatabaseCatalog catalog) {
        List<SqlStatement> result = new LinkedList<>();
        if (catalog.getSchemas() != null) {
            result.addAll(catalog.getSchemas().stream().flatMap(s -> dropCreateSchema(s).stream()).toList());
            LOGGER.debug("create drop/create schema statements");
            result.addAll(catalog.getSchemas().stream().flatMap(s -> dropCreateTables(s).stream()).toList());
            LOGGER.debug("create drop/create tables statements");
        }
        if (catalog.getLinks() != null) {
            result.addAll(catalog.getLinks().stream().map(l -> dropLinks(l)).toList());
            LOGGER.debug("create drop links statements");
            result.addAll(catalog.getLinks().stream().map(l -> createLinks(l)).toList());
            LOGGER.debug("create create links statements");
        }
        return result;
    }

    private SqlStatement createLinks(Link l) {
        String name = generateLinkName(l);
        ColumnReference primaryKey = new ColumnReferenceR(Optional.of(new TableReferenceR(Optional.empty(),
            l.getPrimaryKey().getTable().getName())), l.getPrimaryKey().getName());
        ColumnReference foreignKey = new ColumnReferenceR(Optional.of(new TableReferenceR(Optional.empty(),
            l.getForeignKey().getTable().getName())), l.getForeignKey().getName());
        ImportedKey importedKey = new ImportedKeyR(primaryKey, foreignKey, name);
        return new CreateConstraintStatementR(importedKey);
    }

    private SqlStatement dropLinks(Link l) {
        String name = generateLinkName(l);
        Table foreignKeyTable = l.getForeignKey().getTable();
        DatabaseSchema databaseSchema = foreignKeyTable.getSchema();
        SchemaReference schemaReference = new SchemaReferenceR(databaseSchema.getName());
        TableReference table = new TableReferenceR(Optional.of(schemaReference), foreignKeyTable.getName());
        DropImportedKey dropImportedKey = new DropImportedKeyR(table, name);
        return  new DropConstraintStatementR(dropImportedKey, true);
    }

    private String generateLinkName(Link l) {
        //fk_table1_id1_table2_id2
        StringBuilder sb = new StringBuilder("fk_")
            .append(l.getForeignKey().getTable().getName())
            .append("_")
            .append(l.getForeignKey().getName())
            .append("_")
            .append(l.getPrimaryKey().getTable().getName())
            .append("_")
            .append(l.getPrimaryKey().getName());
        return sb.toString();
    }

    private Collection<SqlStatement> dropCreateTables(DatabaseSchema s) {
        List<SqlStatement> result = new LinkedList<>();
        if (s.getTables() != null) {
            Optional<SchemaReference> schema = Optional.of(new SchemaReferenceR(s.getName()));
            result.addAll(s.getTables().stream().flatMap(t -> dropCreateTable(schema, t).stream()).toList());
        }
        return result;
    }

    private Collection<SqlStatement> dropCreateTable(Optional<SchemaReference> schema, Table t) {
        List<SqlStatement> result = new LinkedList<>();
        TableReference container = new TableReferenceR(schema, t.getName());
        TableDefinitionR tableDefinition = new TableDefinitionR(container);
        List<ColumnDefinition> columnDefinitions = getColumnDefinitions(schema, t);
        result.add(new DropContainerSqlStatementR(container, true));
        result.add(new CreateContainerSqlStatementR(tableDefinition, columnDefinitions, true));
        return result;
    }

    private List<ColumnDefinition> getColumnDefinitions(Optional<SchemaReference> schema, Table t) {
        List<ColumnDefinition> result = new ArrayList<>();
        if (t.getColumns() != null) {
            Optional<TableReference> table = Optional.of(new TableReferenceR(schema, t.getName()));
            result.addAll(t.getColumns().stream().map(c -> getColumnDefinition(table, c)).toList());
        }
        return result;
    }

    private ColumnDefinition getColumnDefinition(Optional<TableReference> table, Column c) {

        ColumnReference column = new ColumnReferenceR(table, c.getName());
        JDBCType dataType = JDBCType.valueOf(c.getType().toUpperCase());
        Optional<Integer> columnSize = Optional.empty();
        Optional<Integer> decimalDigits = Optional.empty();
        if (c.getTypeQualifiers() != null) {
            if (c.getTypeQualifiers().size() > 0) {
                columnSize = Optional.ofNullable(Integer.parseInt(c.getTypeQualifiers().get(0)));
            }
            if (c.getTypeQualifiers().size() > 1) {
                decimalDigits = Optional.ofNullable(Integer.parseInt(c.getTypeQualifiers().get(1)));
            }
        }
        ColumnMetaDataR columnType = new ColumnMetaDataR(dataType, columnSize, decimalDigits, Optional.empty());
        return new ColumnDefinitionR(column, columnType);
    }

    private List<SqlStatement> dropCreateSchema(DatabaseSchema s) {
        List<SqlStatement> result = new LinkedList<>();
        SchemaReference schema = new SchemaReferenceR(s.getName());
        result.add(new DropSchemaSqlStatementR(schema, true));
        result.add(new CreateSchemaSqlStatementR(schema, true));
        return result;
    }


}
