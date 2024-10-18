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
 *   SmartCity Jena, Stefan Bischof - initial
 *
 */
package org.eclipse.daanse.rdb.structure.modifier.base;

import java.util.List;

import org.eclipse.daanse.rdb.structure.api.DatabaseStructureProvider;
import org.eclipse.daanse.rdb.structure.api.model.Column;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseSchema;
import org.eclipse.daanse.rdb.structure.api.model.Link;
import org.eclipse.daanse.rdb.structure.api.model.PhysicalTable;
import org.eclipse.daanse.rdb.structure.api.model.Query;
import org.eclipse.daanse.rdb.structure.api.model.SystemTable;
import org.eclipse.daanse.rdb.structure.api.model.Table;
import org.eclipse.daanse.rdb.structure.api.model.ViewTable;

public abstract class AbstractMappingModifier implements DatabaseStructureProvider {

    protected DatabaseCatalog catalog;

    protected AbstractMappingModifier(DatabaseCatalog catalog) {
        super();
        this.catalog = catalog;
    }

    public DatabaseCatalog get() {
        return modifyCatalog(catalog);
    }

    protected DatabaseCatalog modifyCatalog(DatabaseCatalog catalog2) {
        if (catalog2 != null) {
            List<? extends DatabaseSchema> schemas = databaseCatalogSchemas(catalog2);
            List<? extends Link> links = databaseCatalogLinks(catalog2);
            return createCatalog(schemas, links);
        }
        return null;
    }

    protected List<? extends Link> databaseCatalogLinks(DatabaseCatalog catalog2) {
        return links(catalog2.getLinks());
    }

    protected List<? extends Link> links(List<? extends Link> links) {
        if (links != null) {
            return links.stream().map(this::link).toList();
        }
        return null;
    }

    protected Link link(Link link) {
        if (link != null) {
            Column primaryKey = linkPrimaryKey(link);
            Column foreignKey = linkForeignKey(link);
            return createLink(primaryKey, foreignKey);
        }
        return null;
    }

    protected abstract Link createLink(Column primaryKey, Column foreignKey);

    protected Column linkForeignKey(Link link) {
        return column(link.getForeignKey());
    }

    protected Column column(Column column) {
        if (column != null) {
            String name = columnName(column);
            Table table = columnTable(column);
            String type = columnType(column);
            List<String> typeQualifiers = columnTypeQualifiers(column);
            String description = columnDescription(column);
            return createColumn(name, table, type, typeQualifiers, description);
        }
        return null;
    }

    protected abstract Column createColumn(
        String name, Table table, String type, List<String> typeQualifiers,
        String description
    );

    protected String columnDescription(Column column) {
        return column.getDescription();
    }

    protected List<String> columnTypeQualifiers(Column column) {
        return typeQualifiers(column.getTypeQualifiers());
    }

    protected List<String> typeQualifiers(List<String> typeQualifiers) {
        if (typeQualifiers != null) {
            return typeQualifiers.stream().map(t -> t).toList();
        }
        return List.of();
    }

    protected String columnType(Column column) {
        return column.getType();
    }

    protected Table columnTable(Column column) {
        return table(column.getTable());
    }

    protected Table table(Table table) {
        if (table != null) {
            String name = tableName(table);
            List<? extends Column> columns = tableColumns(table);
            DatabaseSchema schema = tableSchema(table);
            String description = tableDescription(table);
            if (table instanceof PhysicalTable) {
                return createPhysicalTable(name, columns, schema, description);
            }
            if (table instanceof SystemTable) {
                return createSystemTable(name, columns, schema, description);
            }
            if (table instanceof ViewTable) {
                return createViewTable(name, columns, schema, description);
            }
        }
        return null;
    }

    protected abstract ViewTable createViewTable(
        String name, List<? extends Column> columns, DatabaseSchema schema,
        String description
    );

    protected abstract SystemTable createSystemTable(
        String name, List<? extends Column> columns, DatabaseSchema schema,
        String description
    );

    protected abstract PhysicalTable createPhysicalTable(
        String name, List<? extends Column> columns, DatabaseSchema schema,
        String description
    );

    protected String tableDescription(Query table) {
        return table.getDescription();
    }

    protected DatabaseSchema tableSchema(Query table) {
        return databaseSchema(table.getSchema());
    }

    protected DatabaseSchema databaseSchema(DatabaseSchema schema) {
        if (schema != null) {
            List<? extends Table> tables = schemaTables(schema);
            String name = schemaName(schema);
            String id = schemaId(schema);
            return createDatabaseSchema(tables, name, id);
        }
        return null;
    }

    protected String schemaId(DatabaseSchema schema) {
        return schema.getId();
    }

    protected String schemaName(DatabaseSchema schema) {
        return schema.getName();
    }

    protected List<? extends Table> schemaTables(DatabaseSchema schema) {
        return tables(schema.getTables());
    }

    protected List<? extends Table> tables(List<? extends Table> tables) {
        if (tables != null) {
            return tables.stream().map(this::table).toList();
        }
        return null;
    }

    protected abstract DatabaseSchema createDatabaseSchema(List<? extends Table> tables, String name, String id);

    protected List<? extends Column> tableColumns(Query table) {
        return columns(table.getColumns());
    }

    protected List<? extends Column> columns(List<? extends Column> columns) {
        if (columns != null) {
            return columns.stream().map(this::column).toList();
        }
        return null;
    }

    protected String tableName(Table table) {
        return table.getName();
    }

    protected String columnName(Column column) {
        return column.getName();
    }

    protected Column linkPrimaryKey(Link link) {
        return column(link.getPrimaryKey());
    }

    protected abstract DatabaseCatalog createCatalog(
        List<? extends DatabaseSchema> schemas,
        List<? extends Link> links
    );

    protected List<? extends DatabaseSchema> databaseCatalogSchemas(DatabaseCatalog catalog2) {
        return databaseSchemas(catalog2.getSchemas());
    }

    protected List<? extends DatabaseSchema> databaseSchemas(List<? extends DatabaseSchema> schemas) {
        if (schemas != null) {
            return schemas.stream().map(this::databaseSchema).toList();
        }
        return null;
    }


}
