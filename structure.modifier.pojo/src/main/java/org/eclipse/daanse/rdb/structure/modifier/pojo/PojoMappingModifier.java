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
 *
 */
package org.eclipse.daanse.rdb.structure.modifier.pojo;

import java.util.List;

import org.eclipse.daanse.rdb.structure.api.model.Column;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseSchema;
import org.eclipse.daanse.rdb.structure.api.model.Link;
import org.eclipse.daanse.rdb.structure.api.model.PhysicalTable;
import org.eclipse.daanse.rdb.structure.api.model.SystemTable;
import org.eclipse.daanse.rdb.structure.api.model.Table;
import org.eclipse.daanse.rdb.structure.api.model.ViewTable;
import org.eclipse.daanse.rdb.structure.modifier.base.AbstractMappingModifier;
import org.eclipse.daanse.rdb.structure.pojo.AbstractTable;
import org.eclipse.daanse.rdb.structure.pojo.ColumnImpl;
import org.eclipse.daanse.rdb.structure.pojo.DatabaseCatalogImpl;
import org.eclipse.daanse.rdb.structure.pojo.DatabaseSchemaImpl;
import org.eclipse.daanse.rdb.structure.pojo.LinkImpl;
import org.eclipse.daanse.rdb.structure.pojo.PhysicalTableImpl;
import org.eclipse.daanse.rdb.structure.pojo.SystemTableImpl;
import org.eclipse.daanse.rdb.structure.pojo.ViewTableImpl;

public class PojoMappingModifier extends AbstractMappingModifier {

    protected PojoMappingModifier(DatabaseCatalog catalog) {
        super(catalog);
    }

    @Override
    protected Link createLink(Column primaryKey, Column foreignKey) {
        LinkImpl link = new LinkImpl();
        link.setPrimaryKey((ColumnImpl) primaryKey);
        link.setForeignKey((ColumnImpl) foreignKey);
        return link;
    }

    @Override
    protected Column createColumn(
        String name, Table table, String type, List<String> typeQualifiers,
        String description
    ) {
        ColumnImpl column = new ColumnImpl();
        column.setName(name);
        column.setTable(table);
        column.setType(type);
        column.setTypeQualifiers(typeQualifiers);
        column.setDescription(description);
        return column;

    }

    @SuppressWarnings("unchecked")
    @Override
    protected PhysicalTable createPhysicalTable(
        String name, List<? extends Column> columns, DatabaseSchema schema,
        String description
    ) {
        PhysicalTableImpl table = new PhysicalTableImpl();
        table.setName(name);
        table.setColumns((List<ColumnImpl>) columns);
        table.setSchema((DatabaseSchemaImpl) schema);
        table.setDescription(description);
        return table;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SystemTable createSystemTable(
        String name, List<? extends Column> columns, DatabaseSchema schema,
        String description
    ) {
        SystemTableImpl table = new SystemTableImpl();
        table.setName(name);
        table.setColumns((List<ColumnImpl>) columns);
        table.setSchema((DatabaseSchemaImpl) schema);
        table.setDescription(description);
        return table;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ViewTable createViewTable(
        String name, List<? extends Column> columns, DatabaseSchema schema,
        String description
    ) {
        ViewTableImpl table = new ViewTableImpl();
        table.setName(name);
        table.setColumns((List<ColumnImpl>) columns);
        table.setSchema((DatabaseSchemaImpl) schema);
        table.setDescription(description);
        return table;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected DatabaseSchema createDatabaseSchema(List<? extends Table> tables, String name, String id) {
        DatabaseSchemaImpl databaseSchema = new DatabaseSchemaImpl();
        databaseSchema.setTables((List<AbstractTable>) tables);
        databaseSchema.setName(name);
        databaseSchema.setId(id);
        return databaseSchema;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected DatabaseCatalog createCatalog(List<? extends DatabaseSchema> schemas, List<? extends Link> links) {
        DatabaseCatalogImpl databaseCatalog = new DatabaseCatalogImpl();
        databaseCatalog.setSchemas((List<DatabaseSchemaImpl>) schemas);
        databaseCatalog.setLinks((List<LinkImpl>) links);
        return databaseCatalog;
    }

}
