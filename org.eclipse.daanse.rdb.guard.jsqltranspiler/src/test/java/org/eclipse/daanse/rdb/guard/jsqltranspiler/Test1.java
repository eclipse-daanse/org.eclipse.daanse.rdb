package org.eclipse.daanse.rdb.guard.jsqltranspiler;

import static org.mockito.Mockito.mock;

import org.eclipse.daanse.rdb.guard.api.SqlGuard;
import org.eclipse.daanse.rdb.guard.api.SqlGuardFactory;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

public class Test1 {

    @Test
    void testName(@InjectService SqlGuardFactory sqlGuardFactory) throws Exception {
        DatabaseCatalog databaseCatalog = mock(DatabaseCatalog.class);
        // TODO: mock against rdb.structure.api not use an impl.
        SqlGuard guard = sqlGuardFactory.create(null, "currentSchemaName", databaseCatalog);
        // guard.guard("select bar from foo");
    }
}