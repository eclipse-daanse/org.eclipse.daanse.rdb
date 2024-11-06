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

package org.eclipse.daanse.rdb.guard.jsqltranspiler;

import java.util.List;
import java.util.Set;

import org.eclipse.daanse.rdb.guard.api.EmptyStatementGuardException;
import org.eclipse.daanse.rdb.guard.api.GuardException;
import org.eclipse.daanse.rdb.guard.api.SqlGuard;
import org.eclipse.daanse.rdb.guard.api.UnallowedStatementTypeGuardException;
import org.eclipse.daanse.rdb.guard.api.UnparsableStatementGuardException;
import org.eclipse.daanse.rdb.guard.api.UnresolvableObjectsGuardException;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseSchema;
import org.eclipse.daanse.rdb.structure.api.model.Table;

import ai.starlake.transpiler.JSQLColumResolver;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcMetaData.ErrorMode;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class TranspilerSqlGuard implements SqlGuard {

    private JdbcMetaData jdbcMetaDataToCopy;

    public TranspilerSqlGuard(String currentCatalogName, String currentSchemaName, DatabaseCatalog databaseCatalog) {

        jdbcMetaDataToCopy = calculateMetaData(currentCatalogName, currentSchemaName, databaseCatalog);

    }

    @Override
    public String guard(String sqlStr) throws GuardException {

        JSQLColumResolver resolver = new JSQLColumResolver(JdbcMetaData.copyOf(jdbcMetaDataToCopy));

        if (sqlStr == null || sqlStr.trim().isEmpty()) {
            throw new EmptyStatementGuardException();
        }

        StringBuilder builder = new StringBuilder();
        StatementDeParser deParser = new StatementDeParser(builder);

        Statement st;
        try {
            st = CCJSqlParserUtil.parse(sqlStr);
            if (st instanceof Select) {
                PlainSelect select = (PlainSelect) st;

                // do not fail direct. so we have access to all unresolved fields
                // but do not add this fields als pseudo into the statement.
                resolver.setErrorMode(ErrorMode.IGNORE);

                // Resolves any AllColumns "*" or AllTableColumns "t.*" expression resolved into the actual columns
                JdbcResultSetMetaData s = select.accept((SelectVisitor<JdbcResultSetMetaData>) resolver,
                        JdbcMetaData.copyOf(jdbcMetaDataToCopy));


                // TODO: a visitor thatlooks up all functions


                // TODO: lookup all used columns if they are of type system table in rdb

                System.out.println(s);
                Set<String> unresolvedObjects = resolver.getUnresolvedObjects();

                if (!unresolvedObjects.isEmpty()) {
                    throw new UnresolvableObjectsGuardException(unresolvedObjects);
                }

                st.accept(deParser);
                return builder.toString();

            } else {
                throw new UnallowedStatementTypeGuardException("Only Select statements allowed");
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
            throw new UnparsableStatementGuardException();
        }

    }

    private static JdbcMetaData calculateMetaData(String currentCatalogName, String currentSchemaName,
            DatabaseCatalog databaseCatalog) {
        JdbcMetaData jdbcMetaData = new JdbcMetaData(currentCatalogName, currentSchemaName);

        for (DatabaseSchema schema : databaseCatalog.getSchemas()) {
            for (Table table : schema.getTables()) {

                List<JdbcColumn> jdbcColumns = table.getColumns().parallelStream().map(c -> new JdbcColumn(c.getName()))
                        .toList();

                jdbcMetaData.addTable(schema.getName(), table.getName(), jdbcColumns);
            }
        }
        return jdbcMetaData;
    }

}
