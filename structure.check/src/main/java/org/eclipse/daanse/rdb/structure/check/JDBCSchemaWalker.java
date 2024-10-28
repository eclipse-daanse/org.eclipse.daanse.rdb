/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.rdb.structure.check;

import org.eclipse.daanse.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnReference;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.SchemaReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.TableReferenceR;
import org.eclipse.daanse.rdb.structure.api.model.Column;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseSchema;
import org.eclipse.daanse.rdb.structure.api.model.InlineTable;
import org.eclipse.daanse.rdb.structure.api.model.PhysicalTable;
import org.eclipse.daanse.rdb.structure.api.model.Row;
import org.eclipse.daanse.rdb.structure.api.model.RowValue;
import org.eclipse.daanse.rdb.structure.api.model.SqlStatement;
import org.eclipse.daanse.rdb.structure.api.model.SqlView;
import org.eclipse.daanse.rdb.structure.api.model.SystemTable;
import org.eclipse.daanse.rdb.structure.api.model.Table;
import org.eclipse.daanse.rdb.structure.api.model.ViewTable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.eclipse.daanse.rdb.structure.check.Cause.DATABASE;
import static org.eclipse.daanse.rdb.structure.check.Level.ERROR;
import static org.eclipse.daanse.rdb.structure.check.Level.WARNING;

public class JDBCSchemaWalker {

    public static final String TABLE = "Table";
    public static final String SQL = "Sql";
    public static final String SCHEMA = "Schema";
    public static final String TABLE_S_DOES_NOT_EXIST_IN_DATABASE = "Table %s does not exist in database";
    public static final String COULD_NOT_CHECK_EXISTANCE_OF_SCHEMA_S = "could not check existence of Schema %s";
    public static final String COULD_NOCH_CHECK_EXISTANCE_OF_TABLE_S = "could noch check existence of Table %s";
    public static final String SCHEMA_S_DOES_NOT_EXIST = "Schema %s does not exist";
    public static final String INLINE_TABLE_S_COLUMNS_NOT_DEFINED = "Inline table %s columns not defined";
    public static final String INLINE_TABLE_S_ROWS_NOT_DEFINED = "Inline table %s rows not defined";
    public static final String INLINE_TABLE_ROW_VALUES_NOT_DEFINED = "Inline table row values not defined";
    public static final String INLINE_TABLE_ROW_VALUES_SIZE_NOT_CORRECT = "Inline table %s row values not defined";
    public static final String TABLE_S_COLUMN_NAME_NOT_DEFINED = "Table %s column name not defined";
    public static final String TABLE_S_COLUMN_TYPE_NOT_DEFINED = "Table %s column type not defined";
    public static final String COLUMN_S_DOES_NOT_EXIST_IN_TABLE_S = "Column %s does not exist in table %s";
    public static final String SQL_VIEW_TABLE_S_STATEMENTS_NOT_DEFINED = "Sql view table %s statements not defined";
    public static final String COULD_NOT_LOOKUP_EXISTANCE_OF_COLUMN_S_DEFINED_IN_TABLE_S = "Could not lookup " +
        "existance of columns %s defined in tabe %s";

    private DatabaseService databaseService;
    private DatabaseCheckConfig config;
    private DatabaseMetaData databaseMetaData;
    private String dialect;
    protected List<VerificationResult> results = new ArrayList<>();

    public JDBCSchemaWalker(
        DatabaseCheckConfig config,
        DatabaseService databaseService,
        DatabaseMetaData databaseMetaData,
        String dialect
    ) {
        this.config = config;
        this.databaseService = databaseService;
        this.databaseMetaData = databaseMetaData;
        this.dialect = dialect;
    }

    protected void checkSqlStatements(List<SqlStatement> list) {
        if (list != null && dialect != null) {
            List<SqlStatement> sqls =
                list.stream().filter(sql -> sql.getDialects().stream().anyMatch(d -> dialect.equals(d))).toList();
            if (!sqls.isEmpty()) {
                checkSqlStatement(sqls.get(0));
            } else {
                sqls =
                    list.stream().filter(sql -> sql.getDialects().stream().anyMatch(d -> "generic".equals(d))).toList();
                if (!sqls.isEmpty()) {
                    checkSqlStatement(sqls.get(0));
                }
            }
        } else {
            List<SqlStatement> sqls =
                list.stream().filter(sql -> sql.getDialects().stream().anyMatch(d -> "generic".equals(d))).toList();
            if (!sqls.isEmpty()) {
                checkSqlStatement(sqls.get(0));
            }
        }
    }

    protected void checkSqlStatement(SqlStatement sql) {
        if (sql != null && sql.getSql() != null) {
            try {
                Connection con = databaseMetaData.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql.getSql());
            } catch (SQLException e) {
                results.add(new VerificationResultR(SQL, e.getMessage().replace("\n", ""), ERROR, DATABASE));
            }
        }
    }

    private TableReference getTableReference(String schemaName, String tableName) {
        return new TableReferenceR(Optional.ofNullable(schemaName != null ? new SchemaReferenceR(schemaName) : null),
            tableName);
    }

    public List<VerificationResult> checkCatalog(DatabaseCatalog catalog) {
        if (catalog.getSchemas() != null) {
            catalog.getSchemas().forEach(s -> checkSchema(s));
            catalog.getSchemas().forEach(s -> checkTables(s));
        }
        return results;
    }

    private void checkTables(DatabaseSchema s) {
        if (s != null && s.getTables() != null) {
            s.getTables().forEach(t -> checkTable(s, t));
        }
    }

    protected void checkPhysicalOrSystemTable(DatabaseSchema s, Table table) {
        String tableName = table.getName();
        try {
            TableReference tableReference = getTableReference(s.getName(), tableName);
            if (!databaseService.tableExists(databaseMetaData, tableReference)) {
                String msg = String.format(TABLE_S_DOES_NOT_EXIST_IN_DATABASE, tableName);
                results.add(new VerificationResultR(TABLE, msg, ERROR, DATABASE));
            } else {
                if (table.getColumns() != null && !table.getColumns().isEmpty()) {
                    table.getColumns().forEach(c -> checkColumnAttrebutes(table, c));
                    table.getColumns().forEach(c -> checkColumn(table, c));
                }
            }
        } catch (SQLException e) {
            String message = String.format(COULD_NOCH_CHECK_EXISTANCE_OF_TABLE_S, tableName);
            results.add(new VerificationResultR(TABLE, message, ERROR, DATABASE));
        }
    }

    private void checkColumn(Table table, Column column) {
        try {
            String schemaName = table.getSchema() != null ? table.getSchema().getName() : null;
            TableReference tableReference = getTableReference(schemaName, table.getName());
            ColumnReference columnReference = new ColumnReferenceR(Optional.of(tableReference), column.getName());
            if (!databaseService.columnExists(databaseMetaData, columnReference)) {
                String msg =
                    String.format(COLUMN_S_DOES_NOT_EXIST_IN_TABLE_S, column.getName(), table.getName());
                results.add(new VerificationResultR(TABLE, msg, ERROR, DATABASE));
            }
        } catch (SQLException e) {
            String msg =
                String.format(COULD_NOT_LOOKUP_EXISTANCE_OF_COLUMN_S_DEFINED_IN_TABLE_S,
                    column.getName(), table.getName());
            results.add(new VerificationResultR(TABLE, msg, ERROR, DATABASE));
        }
    }

    private void checkSqlView(DatabaseSchema s, SqlView sv) {
        if (sv.getSqlStatements() != null && !sv.getSqlStatements().isEmpty()) {

        } else {
            String message = String.format(SQL_VIEW_TABLE_S_STATEMENTS_NOT_DEFINED, sv.getName());
            results.add(new VerificationResultR(TABLE, message, ERROR, DATABASE));
        }
    }

    private void checkInlineTable(DatabaseSchema s, InlineTable it) {
        if (it != null) {
            if (it.getColumns() != null && !it.getColumns().isEmpty()) {
                it.getColumns().forEach(c -> checkColumnAttrebutes(it, c));
                checkInlineTableRows(it, it.getRows(), it.getColumns());
            } else {
                String message = String.format(INLINE_TABLE_S_COLUMNS_NOT_DEFINED, it.getName());
                results.add(new VerificationResultR(TABLE, message, ERROR, DATABASE));
            }
        }
    }

    private void checkInlineTableRows(InlineTable it, List<? extends Row> rows, List<? extends Column> columns) {
        if (rows != null && !rows.isEmpty()) {
            rows.forEach(r -> checkInlineTableRow(it, r, columns));
        } else {
            String message = String.format(INLINE_TABLE_S_ROWS_NOT_DEFINED, it.getName());
            results.add(new VerificationResultR(TABLE, message, ERROR, DATABASE));
        }
    }

    private Object checkInlineTableRow(InlineTable it, Row r, List<? extends Column> columns) {
        if (r.getRowValues() != null && !r.getRowValues().isEmpty()) {
            if (r.getRowValues().size() != columns.size()) {
                String message = String.format(INLINE_TABLE_ROW_VALUES_SIZE_NOT_CORRECT, it.getName());
                results.add(new VerificationResultR(TABLE, message, ERROR, DATABASE));
            }
            r.getRowValues().forEach(rv -> checkInlineTableValue(it, rv));
        } else {
            String message = INLINE_TABLE_ROW_VALUES_NOT_DEFINED;
            results.add(new VerificationResultR(TABLE, message, ERROR, DATABASE));
        }
        return null;
    }

    private void checkInlineTableValue(InlineTable it, RowValue rowValue) {

    }

    private void checkColumnAttrebutes(Table it, Column column) {
        if (column != null) {
            if (column.getName() == null) {
                String message = String.format(TABLE_S_COLUMN_NAME_NOT_DEFINED, it.getName());
                results.add(new VerificationResultR(TABLE, message, ERROR, DATABASE));
            }
            if (column.getType() == null) {
                String message = String.format(TABLE_S_COLUMN_TYPE_NOT_DEFINED, it.getName());
                results.add(new VerificationResultR(TABLE, message, ERROR, DATABASE));
            }
        }
    }

    protected void checkTable(DatabaseSchema s, Table table) {
        if (table instanceof PhysicalTable || table instanceof SystemTable || table instanceof ViewTable) {
            checkPhysicalOrSystemTable(s, table);
        }
        if (table instanceof InlineTable it) {
            checkInlineTable(s, it);
        }
        if (table instanceof SqlView sv) {
            checkSqlView(s, sv);
        }
    }

    private void checkSchema(DatabaseSchema schema) {
        if (schema != null && !isEmpty(schema.getName())) {
            try {
                List<SchemaReference> schemaList = databaseService.getSchemas(databaseMetaData);
                if (!isEmpty(schema.getName()) && !schemaList.stream().filter(sr -> schema.getName().equals(sr.name())).findFirst().isPresent()) {
                    String msg = String.format(SCHEMA_S_DOES_NOT_EXIST, schema);
                    results.add(new VerificationResultR(TABLE, msg, ERROR, DATABASE));
                }
            } catch (SQLException e) {
                String msg = String.format(COULD_NOT_CHECK_EXISTANCE_OF_SCHEMA_S, schema.getName());
                results.add(new VerificationResultR(SCHEMA, msg, ERROR, DATABASE));
            }
        }
    }

    protected static boolean isEmpty(String v) {
        return (v == null) || v.equals("");
    }
}
