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

import org.eclipse.daanse.rdb.guard.api.EmptyStatementGuardException;
import org.eclipse.daanse.rdb.guard.api.GuardException;
import org.eclipse.daanse.rdb.guard.api.SqlGuard;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseSchema;
import org.eclipse.daanse.rdb.structure.api.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.starlake.transpiler.CatalogNotFoundException;
import ai.starlake.transpiler.ColumnNotFoundException;
import ai.starlake.transpiler.JSQLColumResolver;
import ai.starlake.transpiler.JSQLResolver;
import ai.starlake.transpiler.SchemaNotFoundException;
import ai.starlake.transpiler.TableNotDeclaredException;
import ai.starlake.transpiler.TableNotFoundException;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class TranspilerSqlGuard implements SqlGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranspilerSqlGuard.class);
    private JdbcMetaData jdbcMetaDataToCopy;

    public TranspilerSqlGuard(String currentCatalogName, String currentSchemaName, DatabaseCatalog databaseCatalog) {

        jdbcMetaDataToCopy = calculateMetaData(currentCatalogName, currentSchemaName, databaseCatalog);

    }

    @Override
    public String guard(String sqlStr) throws GuardException {

        StringBuilder builder = new StringBuilder();
        StatementDeParser deParser = new StatementDeParser(builder);

        JSQLResolver resolver = new JSQLResolver(jdbcMetaDataToCopy);

        // this does not really work, when there are comments
        // @todo: apply a Regex for SQL Comments
        if (sqlStr == null || sqlStr.trim().isEmpty()) {
            throw new EmptyStatementGuardException();
        }

        try {
            Statement st = CCJSqlParserUtil.parse(sqlStr);

            // we can test for SELECT, though in practise it won't protect us from harmful statements
            if (st instanceof Select) {
                resolver.resolve(st);

                // select columns should not be empty
                final List<JdbcColumn> selectColumns = resolver.getSelectColumns();
                if (selectColumns.isEmpty()) {
                    throw new RuntimeException("Nothing was selected.");
                }

                // any delete columns must be empty
                final List<JdbcColumn> deleteColumns = resolver.getDeleteColumns();
                if (!deleteColumns.isEmpty()) {
                    throw new RuntimeException("DELETE is not permitted.");
                }

                // any update columns must be empty
                final List<JdbcColumn> updateColumns = resolver.getUpdateColumns();
                if (!updateColumns.isEmpty()) {
                    throw new RuntimeException("UPDATE is not permitted.");
                }

                // any insert columns must be empty
                final List<JdbcColumn> insertColumns = resolver.getInsertColumns();
                if (!insertColumns.isEmpty()) {
                    throw new RuntimeException("INSERT is not permitted.");
                }

                // any insert columns must be empty
//            final List<Function> allFunctions = resolver.getFunctions();
                // TODO: Check Functions

                // we can finally resolve for the actually returned columns
                JSQLColumResolver columResolver = new JSQLColumResolver(jdbcMetaDataToCopy);
                columResolver.setErrorMode(JdbcMetaData.ErrorMode.STRICT);

                String rewritten = columResolver.getResolvedStatementText(sqlStr);
                // TODO: get it as object and access to AST that we do not have to reparse
                Statement stResolveds = CCJSqlParserUtil.parse(rewritten);

                System.out.println(rewritten);
                System.out.println(st);

                deParser.visit((Select) st);// or rewritten

                return rewritten;

            } else {
                throw new RuntimeException(st.getClass().getSimpleName().toUpperCase() + " is not permitted.");
            }

        } catch (CatalogNotFoundException | ColumnNotFoundException | SchemaNotFoundException
                | TableNotDeclaredException | TableNotFoundException | JSQLParserException ex) {
            throw new RuntimeException("Unresolvable Statement", ex);
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
