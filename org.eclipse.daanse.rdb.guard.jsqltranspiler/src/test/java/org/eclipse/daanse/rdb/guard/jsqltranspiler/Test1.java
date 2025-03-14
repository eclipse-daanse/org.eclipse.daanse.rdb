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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.eclipse.daanse.rdb.guard.api.SqlGuard;
import org.eclipse.daanse.rdb.guard.api.SqlGuardFactory;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.pojo.ColumnImpl;
import org.eclipse.daanse.rdb.structure.pojo.DatabaseCatalogImpl;
import org.eclipse.daanse.rdb.structure.pojo.DatabaseSchemaImpl;
import org.eclipse.daanse.rdb.structure.pojo.PhysicalTableImpl;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

import ai.starlake.transpiler.TableNotDeclaredException;

public class Test1 {

    private static final String FOO_FACT = "fooFact";

    private static final String VALUE = "value";

    private static final String BUZZ = "buzz";

    private static final String BAR = "bar";

    private static final String FOO = "foo";

    private static final String NAME = "name";

    private static final String ID = "id";

    private static final String SCH = "sch";

    private static final String SQL_WITH_FUNCTION_WRONG_COLUMN = "select trim(foo.name1)  from foo";

    private static final String SQL_WITH_FUNCTION_WRONG_TABLE = "select trim(foo1.name)  from foo";

    private static final String SQL_WITH_FUNCTION = "select trim(foo.name)  from foo";

    private static final String SQL_WITH_FUNCTION_EXPECTED = "SELECT Trim( foo.name ) FROM sch.foo";

    private static final String SQL_WITH_HAVING_WRONG_COLUMN = """
            select %s(foo.id) from foo group by foo.name having foo.name1 = 'tets'""";

    private static final String SQL_WITH_HAVING_WRONG_COLUMN_EXPECTED = """
        SELECT %s(foo.id) FROM sch.foo GROUP BY foo.name HAVING foo.name1 = 'tets'""";

    private static final String SQL_WITH_HAVING_WRONG_TABLE1 = """
            select %s(foo.id) from foo group by foo.name having foo1.name = 'tets'""";

    private static final String SQL_WITH_HAVING_WRONG_TABLE1_EXPECTED = """
            SELECT %s(foo.id) FROM sch.foo GROUP BY foo.name HAVING foo1.name = 'tets'""";

    private static final String SQL_WITH_HAVING1 = """
        select %s(foo.id) from foo group by foo.name having foo.name = 'tets'""";

    private static final String SQL_WITH_HAVING1_EXPECTED = """
        SELECT %s(foo.id) FROM sch.foo GROUP BY foo.name HAVING foo.name = 'tets'""";

    private static final String SQL_WITH_HAVING = """
            select %s(foo.id) from foo group by foo.name having %s(foo.id) > 5""";

    private static final String SQL_WITH_HAVING_EXPECTED = """
            SELECT %s(foo.id) FROM sch.foo GROUP BY foo.name HAVING %s(foo.id) > 5""";

    private static final String SQL_WITH_HAVING_WRONG_TABLE = """
            select %s(foo.id) from foo group by foo.name having %s(foo1.id) > 5""";

    private static final String SQL_WITH_HAVING_WRONG_TABLE_EXPECTED = """
            SELECT %s(foo.id) FROM sch.foo GROUP BY foo.name HAVING %s(foo1.id) > 5""";

    private static final String SQL_WITH_AGG_WITH_WRONG_TABLE = """
            select %s(foo1.id) from foo group by foo.name""";

        private static final String SQL_WITH_AGG = """
                select %s(foo.id)  from foo group by foo.name""";

        private static final String SQL_WITH_AGG_EXPECTED = """
                SELECT %s(foo.id) FROM sch.foo GROUP BY foo.name""";

    private static final String SQL_WITH_GROUP = "select * from foo group by foo.id, foo.name";

    private static final String SQL_WITH_GROUP_EXPECTED = """
            SELECT sch.foo.id /* Resolved Column*/ , sch.foo.name /* Resolved Column*/  FROM sch.foo GROUP BY foo.id, foo.name""";

    private static final String TABLE_FOO1_DOES_NOT_EXIST_IN_THE_GIVEN_SCHEMA_SCH = "Table foo1 does not exist in the given Schema sch";

    private static final String SIMPLE_SQL_WITH_WRONG_TABLE = "select * from foo1";

    private static final String SQL_WITH_WRONG_TABLE = """
        select * from foo where foo.id in (select fooFact1.id from fooFact1)
            """;
    private static final String SQL_WITH_WRONG_TABLE_EXPECTED = """
    SELECT sch.foo.id /* Resolved Column*/ , sch.foo.name /* Resolved Column*/  FROM sch.foo WHERE foo.id IN (SELECT fooFact1.id FROM fooFact1)""";

    private static final String SQL_WITH_CUSTOM_COLUMN = """
            select *, 5 as testColumn from foo where foo.id  = 10""";

    private static final String SQL_WITH_CUSTOM_COLUMN_EXPECTED = """
            SELECT sch.foo.id /* Resolved Column*/ , sch.foo.name /* Resolved Column*/ , 5 AS testColumn FROM sch.foo WHERE foo.id = 10""";

    private static final String SQL_WITH_IN = """
            select * from foo where foo.id in (select fooFact.id from fooFact)""";

    private static final String SQL_WITH_IN_EXPECTED = """
            SELECT sch.foo.id /* Resolved Column*/ , sch.foo.name /* Resolved Column*/  FROM sch.foo WHERE foo.id IN (SELECT fooFact.id FROM fooFact)""";

    private static final String TRIPLE_SELECT_SQL = """
            SELECT * FROM ( SELECT * FROM ( SELECT * FROM foo inner join fooFact on foo.id = fooFact.id ) a ) b""";

    private static final String TRIPLE_SELECT_SQL_EXPECTED = """
            SELECT sch.b.id /* Resolved Column*/ , sch.b.name /* Resolved Column*/ , sch.b.id_1 /* Resolved Column*/ , sch.b.value /* Resolved Column*/  FROM (SELECT sch.a.id /* Resolved Column*/ , sch.a.name /* Resolved Column*/ , sch.a.id_1 /* Resolved Column*/ , sch.a.value /* Resolved Column*/  FROM (SELECT sch.foo.id /* Resolved Column*/ , sch.foo.name /* Resolved Column*/ , sch.fooFact.id /* Resolved Column*/ , sch.fooFact.value /* Resolved Column*/  FROM sch.foo INNER JOIN sch.fooFact ON foo.id = fooFact.id) a) b""";

    private static final String SELECT_INNER_JOIN_C_D = """
            SELECT * FROM ((SELECT * FROM foo) c inner join fooFact on c.id = fooFact.id ) d""";

    private static final String SELECT_INNER_JOIN_C_D_EXPECTED = """
            SELECT sch.d.id /* Resolved Column*/ , sch.d.name /* Resolved Column*/ , sch.d.id_1 /* Resolved Column*/ , sch.d.value /* Resolved Column*/  FROM ((SELECT sch.foo.id /* Resolved Column*/ , sch.foo.name /* Resolved Column*/  FROM sch.foo) c INNER JOIN sch.fooFact ON c.id = fooFact.id) d""";

    private static final String SELECT_INNER_JOIN_D = """
            SELECT * FROM ( SELECT * FROM foo inner join fooFact on foo.id = fooFact.id ) d""";

    private static final String SELECT_INNER_JOIN_D_EXPECTED = """
            SELECT sch.d.id /* Resolved Column*/ , sch.d.name /* Resolved Column*/ , sch.d.id_1 /* Resolved Column*/ , sch.d.value /* Resolved Column*/  FROM (SELECT sch.foo.id /* Resolved Column*/ , sch.foo.name /* Resolved Column*/ , sch.fooFact.id /* Resolved Column*/ , sch.fooFact.value /* Resolved Column*/  FROM sch.foo INNER JOIN sch.fooFact ON foo.id = fooFact.id) d""";

    private static final String SELECT_INNER_JOIN = """
            select * from foo inner join fooFact on foo.id = fooFact.id""";

    private static final String SELECT_INNER_JOIN_EXPECTED = """
            SELECT sch.foo.id /* Resolved Column*/ , sch.foo.name /* Resolved Column*/ , sch.fooFact.id /* Resolved Column*/ , sch.fooFact.value /* Resolved Column*/  FROM sch.foo INNER JOIN sch.fooFact ON foo.id = fooFact.id""";

    private static final String SELECT_FROM_FOO = "select * from foo";

    private static final String SELECT_FROM_FOO_RESULT = """
            SELECT sch.foo.bar /* Resolved Column*/ , sch.foo.buzz /* Resolved Column*/  FROM sch.foo""";

    private static final List<String> AGGREGATIONS = List.of("sum", "count", "distinctcount", "avg");
    @Test
    void testName(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colBar = ColumnImpl.builder().withName(BAR).build();
        ColumnImpl colBuzz = ColumnImpl.builder().withName(BUZZ).build();
        PhysicalTableImpl table = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colBar, colBuzz))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(table))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        String result = guard.guard(SELECT_FROM_FOO);

        assertEquals(SELECT_FROM_FOO_RESULT, result);
    }

    @Test
    void testInnerJoin(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colIdFooTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colNameFooTable = ColumnImpl.builder().withName(NAME).build();
        PhysicalTableImpl fooTable = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colIdFooTable, colNameFooTable))
                .build();

        ColumnImpl colIdFooFactTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colValueFooFactTable = ColumnImpl.builder().withName(VALUE).build();
        PhysicalTableImpl fooFactTable = PhysicalTableImpl.builder().withName(FOO_FACT).withColumns(List.of(colIdFooFactTable, colValueFooFactTable))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(fooTable, fooFactTable))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        String result = guard.guard(SELECT_INNER_JOIN);
        assertEquals(SELECT_INNER_JOIN_EXPECTED, result);
    }

    @Test
    void testInnerJoin1(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colIdFooTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colNameFooTable = ColumnImpl.builder().withName(NAME).build();
        PhysicalTableImpl fooTable = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colIdFooTable, colNameFooTable))
                .build();

        ColumnImpl colIdFooFactTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colValueFooFactTable = ColumnImpl.builder().withName(VALUE).build();
        PhysicalTableImpl fooFactTable = PhysicalTableImpl.builder().withName(FOO_FACT).withColumns(List.of(colIdFooFactTable, colValueFooFactTable))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(fooTable, fooFactTable))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        String result = guard.guard(SELECT_INNER_JOIN_C_D);

        assertEquals(SELECT_INNER_JOIN_C_D_EXPECTED, result);
    }

    @Test
    void testInnerJoin2(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colIdFooTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colNameFooTable = ColumnImpl.builder().withName(NAME).build();
        PhysicalTableImpl fooTable = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colIdFooTable, colNameFooTable))
                .build();

        ColumnImpl colIdFooFactTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colValueFooFactTable = ColumnImpl.builder().withName(VALUE).build();
        PhysicalTableImpl fooFactTable = PhysicalTableImpl.builder().withName(FOO_FACT).withColumns(List.of(colIdFooFactTable, colValueFooFactTable))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(fooTable, fooFactTable))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        String result = guard.guard(SELECT_INNER_JOIN_D);

        assertEquals(SELECT_INNER_JOIN_D_EXPECTED, result);
    }

    @Test
    void testTripleSelect (@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colIdFooTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colNameFooTable = ColumnImpl.builder().withName(NAME).build();
        PhysicalTableImpl fooTable = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colIdFooTable, colNameFooTable))
                .build();

        ColumnImpl colIdFooFactTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colValueFooFactTable = ColumnImpl.builder().withName(VALUE).build();
        PhysicalTableImpl fooFactTable = PhysicalTableImpl.builder().withName(FOO_FACT).withColumns(List.of(colIdFooFactTable, colValueFooFactTable))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(fooTable, fooFactTable))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        String result = guard.guard(TRIPLE_SELECT_SQL);

        assertEquals(TRIPLE_SELECT_SQL_EXPECTED, result);
    }

    @Test
    void testWhere(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colIdFooTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colNameFooTable = ColumnImpl.builder().withName(NAME).build();
        PhysicalTableImpl fooTable = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colIdFooTable, colNameFooTable))
                .build();

        ColumnImpl colIdFooFactTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colValueFooFactTable = ColumnImpl.builder().withName(VALUE).build();
        PhysicalTableImpl fooFactTable = PhysicalTableImpl.builder().withName(FOO_FACT).withColumns(List.of(colIdFooFactTable, colValueFooFactTable))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(fooTable, fooFactTable))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        String result = guard.guard(SQL_WITH_IN);

        assertEquals(SQL_WITH_IN_EXPECTED, result);
    }

    @Test
    void testAdditionalColumn(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colIdFooTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colNameFooTable = ColumnImpl.builder().withName(NAME).build();
        PhysicalTableImpl fooTable = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colIdFooTable, colNameFooTable))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(fooTable))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        String result = guard.guard(SQL_WITH_CUSTOM_COLUMN);

        assertEquals(SQL_WITH_CUSTOM_COLUMN_EXPECTED, result);
    }

    @Test
    void testUndefinedTable(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colIdFooTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colNameFooTable = ColumnImpl.builder().withName(NAME).build();
        PhysicalTableImpl fooTable = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colIdFooTable, colNameFooTable))
                .build();

        ColumnImpl colIdFooFactTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colValueFooFactTable = ColumnImpl.builder().withName(VALUE).build();
        PhysicalTableImpl fooFactTable = PhysicalTableImpl.builder().withName(FOO_FACT).withColumns(List.of(colIdFooFactTable, colValueFooFactTable))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(fooTable, fooFactTable))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        String result = guard.guard(SQL_WITH_WRONG_TABLE);
        //TODO
        //assertThrows(RuntimeException.class, () -> guard.guard("select * from foo where foo.id in (select fooFact1.id from fooFact1)"));

        assertEquals(
                SQL_WITH_WRONG_TABLE_EXPECTED, result);
    }


    @Test
    void test(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colIdFooTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colNameFooTable = ColumnImpl.builder().withName(NAME).build();
        PhysicalTableImpl fooTable = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colIdFooTable, colNameFooTable))
                .build();


        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(fooTable))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> guard.guard(SIMPLE_SQL_WITH_WRONG_TABLE));
        assertEquals(TABLE_FOO1_DOES_NOT_EXIST_IN_THE_GIVEN_SCHEMA_SCH, thrown.getMessage());
    }

    @Test
    void testGroup(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colIdFooTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colNameFooTable = ColumnImpl.builder().withName(NAME).build();
        PhysicalTableImpl fooTable = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colIdFooTable, colNameFooTable))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(fooTable))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        String result = guard.guard(SQL_WITH_GROUP);

        assertEquals(SQL_WITH_GROUP_EXPECTED, result);
    }

    @Test
    void testGroupAggregation(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colIdFooTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colNameFooTable = ColumnImpl.builder().withName(NAME).build();
        PhysicalTableImpl fooTable = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colIdFooTable, colNameFooTable))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(fooTable))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        for (String agg : AGGREGATIONS) {
            String result = guard.guard(String.format(SQL_WITH_AGG, agg));
            assertEquals(String.format( SQL_WITH_AGG_EXPECTED, agg), result);

            assertThrows(TableNotDeclaredException.class, () -> guard.guard(String.format(SQL_WITH_AGG_WITH_WRONG_TABLE, agg)));
            //assertEquals(String.format( SQL_WITH_AGG_WITH_WRONG_TABLE_EXPECTED, agg), result);

            result = guard.guard(String.format(SQL_WITH_HAVING, agg, agg));
            assertEquals(String.format(SQL_WITH_HAVING_EXPECTED, agg, agg), result);

            result = guard.guard(String.format(SQL_WITH_HAVING_WRONG_TABLE, agg, agg));
            //TODO
            //assertThrows(RuntimeException.class, () -> guard.guard("select avg(foo1.id) from foo group by foo.name"));
            assertEquals(String.format(SQL_WITH_HAVING_WRONG_TABLE_EXPECTED, agg, agg), result);

            result = guard.guard(String.format(SQL_WITH_HAVING1, agg));
            assertEquals(String.format(SQL_WITH_HAVING1_EXPECTED, agg), result);

            result = guard.guard(String.format(SQL_WITH_HAVING_WRONG_TABLE1, agg));
            //TODO
            //assertThrows(RuntimeException.class, () -> guard.guard("select count(foo.id) from foo group by foo.name having foo1.name = 'tets'"));
            assertEquals(String.format(SQL_WITH_HAVING_WRONG_TABLE1_EXPECTED, agg), result);

            result = guard.guard(String.format(SQL_WITH_HAVING_WRONG_COLUMN, agg));
            //TODO
            //assertThrows(RuntimeException.class, () -> guard.guard("select count(foo.id) from foo group by foo.name having foo.name1 = 'tets'"));
            assertEquals(String.format(SQL_WITH_HAVING_WRONG_COLUMN_EXPECTED, agg), result);

        }
    }

    @Test
    void testFunctions(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        ColumnImpl colIdFooTable = ColumnImpl.builder().withName(ID).build();
        ColumnImpl colNameFooTable = ColumnImpl.builder().withName(NAME).build();
        PhysicalTableImpl fooTable = PhysicalTableImpl.builder().withName(FOO).withColumns(List.of(colIdFooTable, colNameFooTable))
                .build();

        DatabaseSchemaImpl databaseSchema = DatabaseSchemaImpl.builder().withName(SCH).withTables(List.of(fooTable))
                .build();

        DatabaseCatalog databaseCatalog = DatabaseCatalogImpl.builder().withSchemas(List.of(databaseSchema)).build();

        SqlGuard guard = sqlGuardFactory.create("", SCH, databaseCatalog);

        String result = guard.guard(SQL_WITH_FUNCTION);

        assertEquals(SQL_WITH_FUNCTION_EXPECTED, result);

        assertThrows(TableNotDeclaredException.class, () -> guard.guard(SQL_WITH_FUNCTION_WRONG_TABLE));

        assertThrows(org.eclipse.daanse.rdb.guard.api.UnresolvableObjectsGuardException.class, () -> guard.guard(SQL_WITH_FUNCTION_WRONG_COLUMN));
    }

}
