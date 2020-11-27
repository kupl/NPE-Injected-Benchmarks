package org.apache.maven.plugins.enforcer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugins.enforcer.utils.ArtifactMatcher;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyGraphBuilder;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.console.ConsoleLogger;

/**
 * This rule bans all transitive dependencies. There is a configuration option to exclude certain artifacts from being
 * checked.
 * 
 * @author Jakub Senko
 */
public class BanTransitiveDependencies
    extends AbstractNonCacheableEnforcerRule
    implements EnforcerRule
{

    private EnforcerRuleHelper helper;

    /**
     * Specify the dependencies that will be ignored. This can be a list of artifacts in the format
     * <code>groupId[:artifactId][:version][:type][:scope]</code>. Wildcard '*' can be used to in place of specific
     * section (ie group:*:1.0 will match both 'group:artifact:1.0' and 'group:anotherArtifact:1.0') <br>
     * You can override this patterns by using includes. Version is a string representing standard maven version range.
     * Empty patterns will be ignored.
     */
    private List<String> excludes;

    /**
     * Specify the dependencies that will be checked. These are exceptions to excludes intended for more convenient and
     * finer settings. This can be a list of artifacts in the format
     * <code>groupId[:artifactId][:version][:type][:scope]</code>. Wildcard '*' can be used to in place of specific
     * section (ie group:*:1.0 will match both 'group:artifact:1.0' and 'group:anotherArtifact:1.0') <br>
     * Version is a string representing standard maven version range. Empty patterns will be ignored.
     */
    private List<String> includes;

    /**
     * Searches dependency tree recursively for transitive dependencies that are not excluded, while generating nice
     * info message along the way.
     * 
     * @throws InvalidVersionSpecificationException
     */
/**
 * Searches dependency tree recursively for transitive dependencies that are not excluded, while generating nice
 * info message along the way.
 *
 * @throws InvalidVersionSpecificationException
 * 		
 */
private static boolean searchTree(org.apache.maven.shared.dependency.graph.DependencyNode node, int level, org.apache.maven.plugins.enforcer.utils.ArtifactMatcher excludes, java.lang.StringBuilder message) throws org.apache.maven.artifact.versioning.InvalidVersionSpecificationException {
    java.util.List<org.apache.maven.shared.dependency.graph.DependencyNode> children = node.getChildren();
    /* if the node is deeper than direct dependency and is empty, it is transitive. */
    boolean hasTransitiveDependencies = level > 1;
    boolean excluded = false;
    /* holds recursive message from children, will be appended to current message if this node has any transitive
    descendants if message is null, don't generate recursive message.
     */
    java.lang.StringBuilder messageFromChildren = new java.lang.StringBuilder();
    if (excludes.match(node.getArtifact())) {
        // is excluded, we don't care about descendants
        excluded = true;
        hasTransitiveDependencies = false;
    } else {
        for (org.apache.maven.shared.dependency.graph.DependencyNode childNode : children) {
            /* if any of the children has transitive d. so does the parent */
            hasTransitiveDependencies = org.apache.maven.plugins.enforcer.BanTransitiveDependencies.searchTree(childNode, level + 1, excludes, messageFromChildren) || hasTransitiveDependencies;
        }
    }
    // then generate message
    if ((excluded || hasTransitiveDependencies) && (/* NPEX_NULL_EXP */
    message != null)) {
        for (int i = 0; i < level; i++) {
            message.append("   ");
        }
        message.append(node.getArtifact());
        if (excluded) {
            message.append(" [excluded]" + java.lang.System.lineSeparator());
        }
        if (hasTransitiveDependencies) {
            if (level == 1) {
                message.append(" has transitive dependencies:");
            }
            message.append(java.lang.System.lineSeparator()).append(messageFromChildren);
        }
    }
    return hasTransitiveDependencies;
}

    @Override
    public void execute( EnforcerRuleHelper helper )
        throws EnforcerRuleException
    {
        this.helper = helper;

        if ( excludes == null )
        {
            excludes = Collections.emptyList();
        }
        if ( includes == null )
        {
            includes = Collections.emptyList();
        }

        final ArtifactMatcher exclusions = new ArtifactMatcher( excludes, includes );

        DependencyNode rootNode = null;

        try
        {
            MavenProject project = (MavenProject) helper.evaluate( "${project}" );
            List<MavenProject> reactorProjects = (List<MavenProject>) helper.evaluate( "${reactorProjects}" );
            rootNode = createDependencyGraphBuilder().buildDependencyGraph( project, null, reactorProjects );
        }
        catch ( Exception e )
        {
            throw new EnforcerRuleException( "Error: Could not construct dependency tree.", e );
        }

        String message = getMessage();
        StringBuilder generatedMessage = null;
        if ( message == null )
        {
            generatedMessage = new StringBuilder();
        }

        try
        {
            if ( searchTree( rootNode, 0, exclusions, generatedMessage ) )
            {
                throw new EnforcerRuleException( message == null ? generatedMessage.toString() : message );
            }
        }
        catch ( InvalidVersionSpecificationException e )
        {
            throw new EnforcerRuleException( "Error: Invalid version range.", e );
        }

    }

    private DependencyGraphBuilder createDependencyGraphBuilder()
        throws ComponentLookupException
    {
        // CHECKSTYLE_OFF: LineLength
        DefaultDependencyGraphBuilder builder =
            (DefaultDependencyGraphBuilder) helper.getContainer().lookup( DependencyGraphBuilder.class.getCanonicalName(),
                                                                          "default" );
        // CHECKSTYLE_ON: LineLength

        builder.enableLogging( new ConsoleLogger( ConsoleLogger.LEVEL_DISABLED, "DefaultDependencyGraphBuilder" ) );

        return builder;
    }

}
