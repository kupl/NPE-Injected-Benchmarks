/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.feature.maven.mojos;

import static org.apache.sling.feature.diff.FeatureDiff.compareFeatures;
import static org.apache.sling.feature.io.json.FeatureJSONWriter.write;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.diff.DiffRequest;
import org.apache.sling.feature.io.json.FeatureJSONReader;
import org.apache.sling.feature.maven.FeatureConstants;

/**
 * Compares different versions of the same Feature Model.
 */
@Mojo(name = "features-diff",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.TEST,
    threadSafe = true
)
@SuppressWarnings("deprecation")
public final class FeaturesDiffMojo extends AbstractIncludingFeatureMojo {

    @Parameter
    private FeatureSelectionConfig selection;

    @Parameter(defaultValue = "${project.build.directory}/features-diff", readonly = true)
    private File mainOutputDir;

    @Parameter(defaultValue = "(,${project.version})")
    protected String comparisonVersion;

    @Component
    protected ArtifactResolver resolver;

    @Component
    protected ArtifactFactory factory;

    @Component
    private ArtifactMetadataSource metadataSource;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Retrieving Feature files...");
        final Collection<Feature> features = getSelectedFeatures(selection).values();

        if (features.isEmpty()) {
            getLog().debug("There are no assciated Feature files to current project, plugin execution will be interrupted");
            return;
        }

        if (!mainOutputDir.exists()) {
            mainOutputDir.mkdirs();
        }

        getLog().debug("Starting Feature(s) analysis...");

        for (final Feature feature : features) {
            onFeature(feature);
        }
    }

    private void onFeature(Feature current) throws MojoExecutionException, MojoFailureException {
        Feature previous = getPreviousFeature(current);

        if (previous == null) {
            getLog().info("There is no previous verion available of " + current + " model");
            return;
        }

        getLog().info("Comparing current " + current + " to previous " + previous);

        Feature featureDiff = compareFeatures(new DiffRequest()
                                              .setPrevious(previous)
                                              .setCurrent(current));

        File outputDiffFile = new File(mainOutputDir, featureDiff.getId().getClassifier().concat(".json"));

        getLog().info("Rendering differences to file " + outputDiffFile);

        try (FileWriter writer = new FileWriter(outputDiffFile)) {
            write(writer, featureDiff);
        } catch (IOException e) {
            throw new MojoExecutionException("An error occurred while serializing Feature diff to " + outputDiffFile, e);
        }

        projectHelper.attachArtifact(project,
                                     FeatureConstants.PACKAGING_FEATURE,
                                     featureDiff.getId().getClassifier(),
                                     outputDiffFile);
    }

private org.apache.sling.feature.Feature getPreviousFeature(org.apache.sling.feature.Feature current) throws org.apache.maven.plugin.MojoExecutionException, org.apache.maven.plugin.MojoFailureException {
    org.apache.maven.artifact.versioning.VersionRange range;
    try {
        range = org.apache.maven.artifact.versioning.VersionRange.createFromVersionSpec(comparisonVersion);
    } catch (org.apache.maven.artifact.versioning.InvalidVersionSpecificationException e) {
        throw new org.apache.maven.plugin.MojoFailureException("Invalid comparison version: " + e.getMessage());
    }
    org.apache.maven.artifact.Artifact previousArtifact;
    try {
        previousArtifact = factory.createDependencyArtifact(current.getId().getGroupId(), current.getId().getArtifactId(), range, current.getId().getType(), current.getId().getClassifier(), org.apache.maven.artifact.Artifact.SCOPE_COMPILE);
        if (!previousArtifact.getVersionRange().isSelectedVersionKnown(previousArtifact)) {
            getLog().debug("Searching for versions in range: " + previousArtifact.getVersionRange());
            java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion> availableVersions = metadataSource.retrieveAvailableVersions(previousArtifact, mavenSession.getLocalRepository(), project.getRemoteArtifactRepositories());
            filterSnapshots(availableVersions);
            org.apache.maven.artifact.versioning.ArtifactVersion version = range.matchVersion(availableVersions);
            {
                previousArtifact.selectVersion(/* NPEX_NULL_EXP */
                version.toString());
            }
        }
    } catch (org.apache.maven.artifact.versioning.OverConstrainedVersionException ocve) {
        throw new org.apache.maven.plugin.MojoFailureException("Invalid comparison version: " + ocve.getMessage());
    } catch (org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException amre) {
        throw new org.apache.maven.plugin.MojoExecutionException("Error determining previous version: " + amre.getMessage(), amre);
    }
    if (previousArtifact.getVersion() == null) {
        getLog().info(("Unable to find a previous version of the " + current) + " Feature in the repository");
        return null;
    }
    try {
        resolver.resolve(previousArtifact, project.getRemoteArtifactRepositories(), mavenSession.getLocalRepository());
    } catch (org.apache.maven.artifact.resolver.ArtifactResolutionException are) {
        getLog().warn((("Artifact " + previousArtifact) + " cannot be resolved : ") + are.getMessage(), are);
    } catch (org.apache.maven.artifact.resolver.ArtifactNotFoundException anfe) {
        getLog().warn(("Artifact " + previousArtifact) + " does not exist on local/remote repositories", anfe);
    }
    java.io.File featureFile = previousArtifact.getFile();
    if ((featureFile == null) || (!featureFile.exists())) {
        return null;
    }
    try (final java.io.FileReader reader = new java.io.FileReader(featureFile)) {
        return org.apache.sling.feature.io.json.FeatureJSONReader.read(reader, featureFile.getAbsolutePath());
    } catch (java.io.IOException e) {
        throw new org.apache.maven.plugin.MojoExecutionException(("An error occurred while reading the " + featureFile) + " Feature file:", e);
    }
}

    private void filterSnapshots(List<ArtifactVersion> versions) {
        Iterator<ArtifactVersion> versionIterator = versions.iterator();
        while (versionIterator.hasNext()) {
            ArtifactVersion version = versionIterator.next();
            if (version.getQualifier() != null && version.getQualifier().endsWith("SNAPSHOT")) {
                versionIterator.remove();
            }
        }
    }

}
