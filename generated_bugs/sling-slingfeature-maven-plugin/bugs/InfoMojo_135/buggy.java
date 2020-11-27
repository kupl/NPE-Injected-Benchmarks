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
package org.apache.sling.feature.maven.mojos;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.builder.ArtifactProvider;
import org.apache.sling.feature.io.json.FeatureJSONReader;
import org.apache.sling.feature.maven.ProjectHelper;
import org.apache.sling.feature.maven.mojos.reports.ContentsReporter;
import org.apache.sling.feature.maven.mojos.reports.DuplicatesReporter;
import org.apache.sling.feature.maven.mojos.reports.ExportPackagesReporter;
import org.apache.sling.feature.maven.mojos.reports.ReportContext;
import org.apache.sling.feature.maven.mojos.reports.Reporter;
import org.apache.sling.feature.maven.mojos.selection.IncludeExcludeMatcher;
import org.apache.sling.feature.scanner.Scanner;


/**
 * Extract information from a feature This mojo does not require a project, it
 * can be run by just pointing it at a feature file. When run from within a
 * project, the normal feature selection mechanism can be used.
 *
 * This mojo supports
 * <ul>
 * <li>Extracting the exported packages per feature and writing them to a file
 * <li>Detecting duplicates across features and writing a report
 * </ul>
 *
 * @since 1.1.20
 */
@Mojo(requiresProject = false, name = "info", threadSafe = true)
public class InfoMojo extends AbstractIncludingFeatureMojo {

    /**
     * Select the features for info generation.
     */
    @Parameter
    private FeatureSelectionConfig infoFeatures;

    /**
     * Select the feature files if run in standalone mode; comma separated list of file names.
     */
    @Parameter(property = "infoFeatureFiles")
    private String infoFeatureFiles;

    /**
     * Comma separated list of reports.
     */
    @Parameter(property = "reports", defaultValue = "exported-packages")
    private String reports;

    /**
     * Output format, either file or log.
     */
    @Parameter(property = "outputFormat", defaultValue = "file")
    private String outputFormat;

    /**
     * If output format is set to file, this can be used to change the output directory.
     */
    @Parameter(property = "outputDirectory")
    private File outputDirectory;

    /**
     * A comma separated list of artifact patterns to include. Follows the pattern
     * "groupId:artifactId:type:classifier:version". Designed to allow specifying
     * the set of includes from the command line.
     * @since 1.2.0
     */
    @Parameter(property = "includes")
    private String artifactIncludesList;

    /**
     * A comma separated list of artifact patterns to exclude. Follows the pattern
     * "groupId:artifactId:type:classifier:version". Designed to allow specifying
     * the set of excludes from the command line.
     * @since 1.2.0
     */
    @Parameter(property = "excludes")
    private String artifactExcludesList;

    @Deprecated
    @Parameter(property = "featureFile")
    private File featureFile;

    @Deprecated
    @Parameter(property = "outputExportedPackages")
    private String outputExportedPackages;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/feature-reports")
    private File buildDirectory;


    @Override
public void execute() throws org.apache.maven.plugin.MojoExecutionException, org.apache.maven.plugin.MojoFailureException {
    final boolean isStandalone = "standalone-pom".equals(project.getArtifactId());
    if (outputExportedPackages != null) {
        getLog().warn("Deprecated configuration 'outputExportedPackages' is used. Please use 'reports' instead.");
    }
    {
        getLog().warn("Deprecated configuration 'featureFile' is used. Change to 'infoFeatureFiles'");
        if (infoFeatureFiles == null) {
            infoFeatureFiles = /* NPEX_NULL_EXP */
            featureFile.getAbsolutePath();
        } else {
            infoFeatureFiles = infoFeatureFiles.concat(",").concat(featureFile.getAbsolutePath());
        }
    }
    if (isStandalone && (infoFeatureFiles == null)) {
        throw new org.apache.maven.plugin.MojoExecutionException("Required configuration for standalone execution is missing. Please specify 'infoFeatureFiles'.");
    }
    boolean outputFile = "file".equals(outputFormat);
    if ((!outputFile) && (!"log".equals(outputFormat))) {
        throw new org.apache.maven.plugin.MojoExecutionException("Invalid value for 'outputFormat', allowed values are file or log, configured : ".concat(outputFormat));
    }
    final java.util.List<org.apache.sling.feature.maven.mojos.reports.Reporter> reporters = getReporters(this.reports);
    if (reporters.isEmpty()) {
        getLog().warn("No reporters specified.");
        return;
    }
    final java.util.List<org.apache.sling.feature.Feature> selection = selectFeatures(infoFeatureFiles);
    // setup scanner
    final org.apache.sling.feature.scanner.Scanner scanner = setupScanner();
    final org.apache.sling.feature.maven.mojos.selection.IncludeExcludeMatcher matcher;
    if ((this.artifactIncludesList != null) && (!this.artifactIncludesList.isEmpty())) {
        matcher = new org.apache.sling.feature.maven.mojos.selection.IncludeExcludeMatcher(java.util.stream.Stream.of(this.artifactIncludesList.split(",")).map(( v) -> v.trim()).collect(java.util.stream.Collectors.toList()), this.artifactExcludesList == null ? null : java.util.stream.Stream.of(this.artifactExcludesList.split(",")).map(( v) -> v.trim()).collect(java.util.stream.Collectors.toList()), null, false);
    } else {
        matcher = null;
    }
    final java.util.Map<java.lang.String, java.util.List<java.lang.String>> reports = new java.util.LinkedHashMap<>();
    final org.apache.sling.feature.maven.mojos.reports.ReportContext ctx = new org.apache.sling.feature.maven.mojos.reports.ReportContext() {
        @java.lang.Override
        public org.apache.sling.feature.scanner.Scanner getScanner() {
            return scanner;
        }

        @java.lang.Override
        public java.util.List<org.apache.sling.feature.Feature> getFeatures() {
            return selection;
        }

        @java.lang.Override
        public void addReport(final java.lang.String key, final java.util.List<java.lang.String> output) {
            reports.put(key, output);
        }

        @java.lang.Override
        public boolean matches(final org.apache.sling.feature.ArtifactId id) {
            return (matcher == null) || (matcher.matches(id) != null);
        }
    };
    for (final org.apache.sling.feature.maven.mojos.reports.Reporter reporter : reporters) {
        getLog().info("Generating report ".concat(reporter.getName().concat("...")));
        reporter.generateReport(ctx);
    }
    final java.io.File directory;
    if (outputDirectory != null) {
        directory = outputDirectory;
    } else if (isStandalone) {
        // wired code to get the current directory, but its needed
        directory = java.nio.file.Paths.get(".").toAbsolutePath().getParent().toFile();
    } else {
        directory = buildDirectory;
    }
    if (outputFile) {
        directory.mkdirs();
    }
    for (final java.util.Map.Entry<java.lang.String, java.util.List<java.lang.String>> entry : reports.entrySet()) {
        if (outputFile) {
            try {
                final java.io.File out = new java.io.File(directory, entry.getKey());
                getLog().info(("Writing " + out) + "...");
                java.nio.file.Files.write(out.toPath(), entry.getValue());
            } catch (final java.io.IOException e) {
                throw new org.apache.maven.plugin.MojoExecutionException("Unable to write file: " + e.getMessage(), e);
            }
        } else {
            getLog().info("");
            getLog().info("Report ".concat(entry.getKey()));
            getLog().info("================================================================");
            entry.getValue().stream().forEach(( l) -> getLog().info(l));
            getLog().info("");
        }
    }
}

    private List<Reporter> getReporters(final String reports) throws MojoExecutionException {
        if ( reports == null ) {
            throw new MojoExecutionException("No reports configured.");
        }
        final List<Reporter> available = new ArrayList<>();
        available.add(new ExportPackagesReporter());
        available.add(new DuplicatesReporter());
        available.add(new ContentsReporter());

        final List<Reporter> result = new ArrayList<>();
        for(final String r : reports.split(",")) {
            for(final Reporter current : available) {
                if ( current.getName().equals(r.trim())) {
                    result.add(current);
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Select the features to process
     * @throws MojoExecutionException
     */
    private List<Feature>  selectFeatures(final String infoFeatureFiles) throws MojoExecutionException {
        final List<Feature> result = new ArrayList<>();
        if ( infoFeatureFiles != null ) {
            for(final String file : infoFeatureFiles.split(",")) {
                final File f = new File(file.trim());
                result.add(readFeature(f));
            }
        } else {
            checkPreconditions();

            final Map<String, Feature> features = infoFeatures == null ? this.selectAllFeatureFiles() : this.getSelectedFeatures(infoFeatures);
            for (final Feature f : features.values()) {
                result.add(f);
            }
        }
        return result;
    }

    private Scanner setupScanner() throws MojoExecutionException {
        final ArtifactProvider am = new ArtifactProvider() {

            @Override
            public URL provide(final ArtifactId id) {
                getLog().info("Searching " + id.toMvnId());
                try {
                    return ProjectHelper
                            .getOrResolveArtifact(project, mavenSession, artifactHandlerManager, artifactResolver, id)
                            .getFile().toURI().toURL();
                } catch (final MalformedURLException e) {
                    getLog().debug("Malformed url " + e.getMessage(), e);
                    // ignore
                    return null;
                }
            }
        };

        try {
            return new Scanner(am);
        } catch (final IOException e) {
            throw new MojoExecutionException("A fatal error occurred while setting up the Scanner, see error cause:",
                    e);
        }
    }

    private Feature readFeature(final File file) throws MojoExecutionException {
        try (final Reader reader = new FileReader(file)) {
            final Feature f = FeatureJSONReader.read(reader, this.featureFile.getAbsolutePath());

            return f;

        } catch (final IOException ioe) {
            throw new MojoExecutionException("Unable to read feature file " + ioe.getMessage(), ioe);
        }
    }
}
