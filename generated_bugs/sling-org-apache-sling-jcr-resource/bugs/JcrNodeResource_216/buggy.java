/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.jcr.resource.internal.helper.jcr;

import static javax.jcr.Property.JCR_CONTENT;
import static javax.jcr.Property.JCR_DATA;
import static javax.jcr.nodetype.NodeType.NT_FILE;
import static javax.jcr.nodetype.NodeType.NT_LINKED_FILE;
import static javax.jcr.nodetype.NodeType.NT_FROZEN_NODE;
import static javax.jcr.Property.JCR_FROZEN_PRIMARY_TYPE;

import java.io.InputStream;
import java.net.URI;
import java.security.AccessControlException;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.sling.adapter.annotations.Adaptable;
import org.apache.sling.adapter.annotations.Adapter;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.external.ExternalizableInputStream;
import org.apache.sling.api.resource.external.URIProvider;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.apache.sling.jcr.resource.internal.HelperData;
import org.apache.sling.jcr.resource.internal.JcrModifiableValueMap;
import org.apache.sling.jcr.resource.internal.JcrValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Resource that wraps a JCR Node */
@Adaptable(adaptableClass=Resource.class, adapters={
        @Adapter({Node.class, Map.class, Item.class, ValueMap.class}),
        @Adapter(value=InputStream.class, condition="If the resource is a JcrNodeResource and has a jcr:data property or is an nt:file node."),
        @Adapter(value=ExternalizableInputStream.class, condition="If the resource is a JcrNodeResource and has a jcr:data property or is an nt:file node, and can be read using a secure URL.")
})
class JcrNodeResource extends JcrItemResource<Node> { // this should be package private, see SLING-1414

    /** marker value for the resourceSupertType before trying to evaluate */
    private static final String UNSET_RESOURCE_SUPER_TYPE = "<unset>";

    /** default log */
    private static final Logger LOGGER = LoggerFactory.getLogger(JcrNodeResource.class);

    private String resourceType;

    private String resourceSuperType;

    private final HelperData helper;

    /**
     * Constructor
     * @param resourceResolver
     * @param path The path of the resource (lazily initialized if null)
     * @param node The Node underlying this resource
     * @param dynamicClassLoader Dynamic class loader for loading serialized objects.
     * @throws RepositoryException
     */
    public JcrNodeResource(final ResourceResolver resourceResolver,
                           final String path,
                           final String version,
                           final Node node,
                           final HelperData helper) {
        super(resourceResolver, path, version, node, new JcrNodeResourceMetadata(node));
        this.helper = helper;
        this.resourceSuperType = UNSET_RESOURCE_SUPER_TYPE;
    }

    /**
     * @see org.apache.sling.api.resource.Resource#getResourceType()
     */
    @Override
    public String getResourceType() {
        if ( this.resourceType == null ) {
            try {
                this.resourceType = getResourceTypeForNode(getNode());
            } catch (final RepositoryException e) {
                LOGGER.error("Unable to get resource type for node " + getNode(), e);
                this.resourceType = "<unknown resource type>";
            }
        }
        return resourceType;
    }

    /**
     * @see org.apache.sling.api.resource.Resource#getResourceSuperType()
     */
    @Override
    public String getResourceSuperType() {
        // Yes, this isn't how you're supposed to compare Strings, but this is intentional.
        if ( resourceSuperType == UNSET_RESOURCE_SUPER_TYPE ) {
            try {
                if (getNode().hasProperty(JcrResourceConstants.SLING_RESOURCE_SUPER_TYPE_PROPERTY)) {
                    resourceSuperType = getNode().getProperty(JcrResourceConstants.SLING_RESOURCE_SUPER_TYPE_PROPERTY).getValue().getString();
                }
            } catch (RepositoryException re) {
                // we ignore this
            }
            if ( resourceSuperType == UNSET_RESOURCE_SUPER_TYPE ) {
                resourceSuperType = null;
            }
        }
        return resourceSuperType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Type> Type adaptTo(Class<Type> type) {
        if (type == Node.class || type == Item.class) {
            return (Type) getNode(); // unchecked cast
        } else if (type == InputStream.class) {
            return (Type) getInputStream(); // unchecked cast
        } else if (type == Map.class || type == ValueMap.class) {
            return (Type) new JcrValueMap(getNode(), this.helper); // unchecked cast
        } else if (type == ModifiableValueMap.class ) {
            // check write
            try {
                getNode().getSession().checkPermission(getPath(),
                    "set_property");
                return (Type) new JcrModifiableValueMap(getNode(), this.helper);
            } catch (AccessControlException ace) {
                // the user has no write permission, cannot adapt
                LOGGER.debug(
                    "adaptTo(ModifiableValueMap): Cannot set properties on {}",
                    this);
            } catch (RepositoryException e) {
                // some other problem, cannot adapt
                LOGGER.debug(
                    "adaptTo(ModifiableValueMap): Unexpected problem for {}",
                    this);
            }
        }

        // fall back to default implementation
        return super.adaptTo(type);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
        	+ ", type=" + getResourceType()
        	+ ", superType=" + getResourceSuperType()
            + ", path=" + getPath();
    }

    // ---------- internal -----------------------------------------------------

    private Node getNode() {
        return getItem();
    }

    /**
     * Returns a stream to the <em>jcr:data</em> property if the
     * {@link #getNode() node} is an <em>nt:file</em> or <em>nt:resource</em>
     * node. Otherwise returns <code>null</code>.
     */
/**
 * Returns a stream to the <em>jcr:data</em> property if the
 * {@link #getNode() node} is an <em>nt:file</em> or <em>nt:resource</em>
 * node. Otherwise returns <code>null</code>.
 */
private java.io.InputStream getInputStream() {
    // implement this for nt:file only
    final javax.jcr.Node node = getNode();
    {
        try {
            // find the content node: for nt:file it is jcr:content
            // otherwise it is the node of this resource
            javax.jcr.Node content = (node.isNodeType(javax.jcr.nodetype.NodeType.NT_FILE) || (node.isNodeType(javax.jcr.nodetype.NodeType.NT_FROZEN_NODE) && node.getProperty(javax.jcr.Property.JCR_FROZEN_PRIMARY_TYPE).getString().equals(javax.jcr.nodetype.NodeType.NT_FILE))) ? node.getNode(javax.jcr.Property.JCR_CONTENT) : node.isNodeType(javax.jcr.nodetype.NodeType.NT_LINKED_FILE) ? node.getProperty(javax.jcr.Property.JCR_CONTENT).getNode() : node;
            javax.jcr.Property data;
            // if the node has a jcr:data property, use that property
            if (content.hasProperty(javax.jcr.Property.JCR_DATA)) {
                data = content.getProperty(javax.jcr.Property.JCR_DATA);
            } else {
                // otherwise try to follow default item trail
                try {
                    javax.jcr.Item item = content.getPrimaryItem();
                    while (item.isNode()) {
                        item = ((javax.jcr.Node) (item)).getPrimaryItem();
                    } 
                    data = ((javax.jcr.Property) (item));
                } catch (javax.jcr.ItemNotFoundException infe) {
                    // we don't actually care, but log for completeness
                    org.apache.sling.jcr.resource.internal.helper.jcr.JcrNodeResource.LOGGER.debug("getInputStream: No primary items for {}", toString(), infe);
                    data = null;
                }
            }
            java.net.URI uri = convertToPublicURI();
            if (uri != null) {
                return new org.apache.sling.jcr.resource.internal.helper.jcr.JcrExternalizableInputStream(data, uri);
            }
            {
                return /* NPEX_NULL_EXP */
                data.getBinary().getStream();
            }
        } catch (javax.jcr.RepositoryException re) {
            org.apache.sling.jcr.resource.internal.helper.jcr.JcrNodeResource.LOGGER.error("getInputStream: Cannot get InputStream for " + this, re);
        }
    }
    // fallback to non-streamable resource
    return null;
}

    /**
     * Ask each URIProvider in turn for a Public URI, and return the first
     * public URI provided.
     * @return a public URI.
     */
    private URI convertToPublicURI() {
        for (URIProvider up : helper.getURIProviders()) {
            try {
                URI uri = up.toURI(this, URIProvider.Scope.EXTERNAL, URIProvider.Operation.READ);
                if ( uri != null ) {
                    return uri;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.debug(up.getClass().toString()+" declined toURI ", e);
            }
        }
        return null;
    }


    // ---------- Descendable interface ----------------------------------------

    @Override
    Iterator<Resource> listJcrChildren() {
        try {
            if (getNode().hasNodes()) {
                return new JcrNodeResourceIterator(getResourceResolver(), path, version,
                    getNode().getNodes(), this.helper, null);
            }
        } catch (final RepositoryException re) {
            LOGGER.error("listChildren: Cannot get children of " + this, re);
        }

        return null;
    }
}
