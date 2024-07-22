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
package org.eclipse.daanse.rdb.structure.pojo;

import java.util.List;

import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;

public class DatabaseCatalogImpl implements DatabaseCatalog {

    private List<DatabaseSchemaImpl> schemas;

    public List<DatabaseSchemaImpl> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<DatabaseSchemaImpl> schemas) {
        this.schemas = schemas;
    }

    public List<LinkImpl> getLinks() {
        return links;
    }

    public void setLinks(List<LinkImpl> links) {
        this.links = links;
    }

    private List<LinkImpl> links;

}
