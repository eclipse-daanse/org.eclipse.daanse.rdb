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
package org.eclipse.daanse.rdb.structure.emf.provider;

import java.io.IOException;
import java.util.Map;

import org.eclipse.daanse.rdb.structure.api.DatabaseStructureProvider;
import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.eclipse.daanse.rdb.structure.emf.Constants;
import org.eclipse.daanse.rdb.structure.emf.rdbstructure.RelationalDatabasePackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.Designate;

@Component(service = DatabaseStructureProvider.class, scope = ServiceScope.SINGLETON, configurationPid = Constants.PID_EMF_DATABASE_CATALOG_PROVIDER)
@Designate(factory = true, ocd = org.eclipse.daanse.rdb.structure.emf.DatabaseStructureConfig.class)
public class EmfDatabaseStructureProvider implements DatabaseStructureProvider {

    @Reference(target = "(" + EMFNamespaces.EMF_MODEL_NAME + "=" + RelationalDatabasePackage.eNAME + ")")
    private ResourceSet resourceSet;

    private DatabaseCatalog databaseCatalog;

    @Activate
    public void activate(org.eclipse.daanse.rdb.structure.emf.DatabaseStructureConfig config) throws IOException {

        URI uri = URI.createURI(config.resource_url());

        Resource resource = resourceSet.getResource(uri, true);
        resource.load(Map.of());

        EObject root = resource.getContents().get(0);

        if (root instanceof DatabaseCatalog databaseCatalog) {
            this.databaseCatalog = databaseCatalog;
        }
    }

    private void cleanAllResources() {
        resourceSet.getResources().forEach(Resource::unload);
    }

    @Deactivate
    public void deactivate() {
        cleanAllResources();
    }

    @Override
    public DatabaseCatalog get() {
        return databaseCatalog;
    }
}
