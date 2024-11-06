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

import org.eclipse.daanse.rdb.guard.api.SqlGuard;
import org.eclipse.daanse.rdb.guard.api.SqlGuardFactory;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(scope = ServiceScope.SINGLETON)
public class TranspilerSqlGuardFactory implements SqlGuardFactory {

    @Override
    public SqlGuard create(String currentCatalogName, String currentSchemaName, DatabaseCatalog databaseCatalog) {
        return new TranspilerSqlGuard(currentCatalogName, currentSchemaName, databaseCatalog);
    }

}
