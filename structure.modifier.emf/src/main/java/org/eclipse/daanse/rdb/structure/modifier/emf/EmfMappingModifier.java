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
package org.eclipse.daanse.rdb.structure.modifier.emf;

import java.util.Collection;
import java.util.List;

import org.eclipse.daanse.rdb.modifier.AbstractMappingModifier;
import org.eclipse.daanse.rdb.structure.api.model.Column;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseSchema;
import org.eclipse.daanse.rdb.structure.api.model.Link;
import org.eclipse.daanse.rdb.structure.api.model.PhysicalTable;
import org.eclipse.daanse.rdb.structure.api.model.SystemTable;
import org.eclipse.daanse.rdb.structure.api.model.Table;
import org.eclipse.daanse.rdb.structure.api.model.ViewTable;
import org.eclipse.daanse.rdb.structure.emf.rdbstructure.RelationalDatabaseFactory;

public class EmfMappingModifier extends AbstractMappingModifier {

    protected EmfMappingModifier(DatabaseCatalog catalog) {
        super(catalog);
    }

    @Override
    protected Link createLink(Column primaryKey, Column foreignKey) {
        org.eclipse.daanse.rdb.structure.emf.rdbstructure.Link link = RelationalDatabaseFactory.eINSTANCE.createLink();
        link.setPrimaryKey((org.eclipse.daanse.rdb.structure.emf.rdbstructure.Column) primaryKey);
        link.setForeignKey((org.eclipse.daanse.rdb.structure.emf.rdbstructure.Column) foreignKey);
        return link;
    }

    @Override
    protected Column createColumn(String name, Table table, String type, List<String> typeQualifiers,
            String description) {
        org.eclipse.daanse.rdb.structure.emf.rdbstructure.Column column = RelationalDatabaseFactory.eINSTANCE
                .createColumn();
        column.setName(name);
        column.setTable((org.eclipse.daanse.rdb.structure.emf.rdbstructure.Table) table);
        column.setType(type);
        column.getTypeQualifiers().addAll(typeQualifiers);
        column.setDescription(description);
        return column;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ViewTable createViewTable(String name, List<? extends Column> columns, DatabaseSchema schema,
            String description) {
        org.eclipse.daanse.rdb.structure.emf.rdbstructure.ViewTable viewTable = RelationalDatabaseFactory.eINSTANCE
                .createViewTable();
        viewTable.setName(name);
        viewTable.getColumns()
                .addAll((Collection<? extends org.eclipse.daanse.rdb.structure.emf.rdbstructure.Column>) columns);
        viewTable.setSchema((org.eclipse.daanse.rdb.structure.emf.rdbstructure.DatabaseSchema) schema);
        viewTable.setDescription(description);
        return viewTable;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SystemTable createSystemTable(String name, List<? extends Column> columns, DatabaseSchema schema,
            String description) {
        org.eclipse.daanse.rdb.structure.emf.rdbstructure.SystemTable systemTable = RelationalDatabaseFactory.eINSTANCE
                .createSystemTable();
        systemTable.setName(name);
        systemTable.getColumns()
                .addAll((Collection<? extends org.eclipse.daanse.rdb.structure.emf.rdbstructure.Column>) columns);
        systemTable.setSchema((org.eclipse.daanse.rdb.structure.emf.rdbstructure.DatabaseSchema) schema);
        systemTable.setDescription(description);
        return systemTable;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected PhysicalTable createPhysicalTable(String name, List<? extends Column> columns, DatabaseSchema schema,
            String description) {
        org.eclipse.daanse.rdb.structure.emf.rdbstructure.PhysicalTable physicalTable = RelationalDatabaseFactory.eINSTANCE
                .createPhysicalTable();
        physicalTable.setName(name);
        physicalTable.getColumns()
                .addAll((Collection<? extends org.eclipse.daanse.rdb.structure.emf.rdbstructure.Column>) columns);
        physicalTable.setSchema((org.eclipse.daanse.rdb.structure.emf.rdbstructure.DatabaseSchema) schema);
        physicalTable.setDescription(description);
        return physicalTable;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected DatabaseSchema createDatabaseSchema(List<? extends Table> tables, String name, String id) {
        org.eclipse.daanse.rdb.structure.emf.rdbstructure.DatabaseSchema databaseSchema = RelationalDatabaseFactory.eINSTANCE
                .createDatabaseSchema();
        databaseSchema.getTables()
                .addAll((Collection<? extends org.eclipse.daanse.rdb.structure.emf.rdbstructure.Table>) tables);
        databaseSchema.setName(name);
        databaseSchema.setId(id);
        return databaseSchema;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected DatabaseCatalog createCatalog(List<? extends DatabaseSchema> schemas, List<? extends Link> links) {
        org.eclipse.daanse.rdb.structure.emf.rdbstructure.DatabaseCatalog databaseCatalog = RelationalDatabaseFactory.eINSTANCE
                .createDatabaseCatalog();
        databaseCatalog.getSchemas()
                .addAll((Collection<? extends org.eclipse.daanse.rdb.structure.emf.rdbstructure.DatabaseSchema>) schemas);
        databaseCatalog.getLinks()
                .addAll((Collection<? extends org.eclipse.daanse.rdb.structure.emf.rdbstructure.Link>) links);
        return databaseCatalog;
    }

}
