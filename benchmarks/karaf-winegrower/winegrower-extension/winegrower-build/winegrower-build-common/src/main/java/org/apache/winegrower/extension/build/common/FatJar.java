/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.winegrower.extension.build.common;

import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class FatJar implements Runnable {
    private final Configuration configuration;

    public FatJar(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void run() {
        requireNonNull(configuration.jars, "Jars are not set");
        requireNonNull(configuration.output, "Output is not set").getParentFile().mkdirs();

        try (final JarOutputStream outputStream = new JarOutputStream(
                new BufferedOutputStream(new FileOutputStream(configuration.output)))) {
            final MetadataBuilder metadataBuilder = new MetadataBuilder(configuration.autoFiltering);
            byte[] buffer = new byte[8192];
            final Set<String> alreadyAdded = new HashSet<>();
            configuration.jars.forEach(shadedJar -> {
                if (shadedJar.isDirectory()) {
                    final Path root = shadedJar.toPath();
                    metadataBuilder.visitFolder(configuration.defaultArtifactName, root, new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                            outputStream.putNextEntry(new JarEntry(toPath(dir) + '/'));
                            outputStream.closeEntry();
                            return super.preVisitDirectory(dir, attrs);
                        }

                        @Override
                        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                            outputStream.putNextEntry(new JarEntry(toPath(file)));
                            Files.copy(file, outputStream);
                            outputStream.closeEntry();
                            return super.visitFile(file, attrs);
                        }

                        private String toPath(final Path file) {
                            return root.resolve(file).toString().replace("\\", "/");
                        }

                    });
                } else {
                    try (final JarInputStream inputStream = new JarInputStream(new BufferedInputStream(new FileInputStream(shadedJar)))) {
                        metadataBuilder.onJar(shadedJar.getName(), inputStream.getManifest());

                        ZipEntry nextEntry;
                        while ((nextEntry = inputStream.getNextEntry()) != null) {
                            final String name = nextEntry.getName();
                            if (!alreadyAdded.add(name)) {
                                continue;
                            }
                            metadataBuilder.onFile(name);
                            outputStream.putNextEntry(nextEntry);
                            int count;
                            while ((count = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                                if (count > 0) {
                                    outputStream.write(buffer, 0, count);
                                }
                            }
                            outputStream.closeEntry();
                        }
                    } catch (final IOException e) {
                        throw new IllegalStateException(e);
                    }
                    metadataBuilder.afterJar();
                }
            });

            outputStream.putNextEntry(new JarEntry("WINEGROWER-INF/"));
            outputStream.closeEntry();

            metadataBuilder.getMetadata().forEach((key, value) -> {
                try {
                    outputStream.putNextEntry(new JarEntry("WINEGROWER-INF/" + key + ".properties"));
                    value.store(outputStream, "index");
                    outputStream.closeEntry();
                } catch (final IOException ioe) {
                    throw new IllegalStateException(ioe);
                }
            });
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Configuration {
        private final Collection<File> jars;
        private final File output;
        private final boolean autoFiltering;
        private final String defaultArtifactName;

        public Configuration(final Collection<File> jars, final File output,
                             final boolean autoFiltering, final String defaultArtifactName) {
            this.jars = jars;
            this.output = output;
            this.autoFiltering = autoFiltering;
            this.defaultArtifactName = defaultArtifactName;
        }
    }
}
