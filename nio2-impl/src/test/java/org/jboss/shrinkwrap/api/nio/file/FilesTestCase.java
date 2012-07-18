/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.shrinkwrap.api.nio.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test cases to assert the ShrinkWrap implementation of the NIO.2 {@link FileSystem} is working as expected via the
 * {@link Files} convenience API.
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class FilesTestCase {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(FilesTestCase.class.getName());

    /**
     * {@link FileSystem} under test
     */
    private FileSystem fs;

    @Before
    public void createFileSystem() throws IOException {
        final String name = "test.jar";
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, name);
        final Map<String, JavaArchive> environment = new HashMap<>();
        environment.put("archive", archive);
        this.fs = FileSystems.newFileSystem(URI.create("shrinkwrap://" + archive.getId() + "/"), environment);
    }

    @After
    public void closeFileSystem() throws IOException {
        this.fs.close();
    }

    @Test
    public void delete() throws IOException {

        // Backdoor add, because we only test delete here (not adding via the Files API)
        final Archive<?> archive = this.getArchive();
        final String pathString = "fileToDelete";
        archive.add(new NamedAsset() {

            @Override
            public InputStream openStream() {
                return new ByteArrayInputStream("Hello".getBytes());
            }

            @Override
            public String getName() {
                return pathString;
            }
        });

        // Ensure added
        Assert.assertTrue(archive.contains(pathString));

        // Delete
        final Path path = fs.getPath(pathString);
        Files.delete(path);

        // Ensure deleted
        Assert.assertFalse(archive.contains(pathString));
    }

    @Test
    public void deleteNonexistant() throws IOException {

        final Archive<?> archive = this.getArchive();
        final String pathString = "nonexistant";

        // Ensure file doesn't exist
        Assert.assertFalse(archive.contains(pathString));

        // Attempt delete
        final Path path = fs.getPath(pathString);
        boolean gotException = false;
        try {
            Files.delete(path);
        } catch (final NoSuchFileException nsfe) {
            gotException = true;
        }
        Assert.assertTrue(
            "Request to remove nonexistant path should have thrown " + NoSuchFileException.class.getSimpleName(),
            gotException);
    }

    @Test
    public void deleteDirectory() throws IOException {

        final String directoryName = "directory";
        final Archive<?> archive = this.getArchive().addAsDirectory(directoryName);

        // Preconditions
        Assert.assertNull("Test archive should contain the directory, not content", archive.get(directoryName)
            .getAsset());

        // Attempt delete
        final Path path = fs.getPath(directoryName);
        Files.delete(path);

        // Assertion
        Assert.assertFalse("Archive should no longer contain directory ", archive.contains(directoryName));
    }

    @Test
    public void deleteUnemptyDirectory() throws IOException {

        final String directoryName = "directory";
        final String subDirectoryName = directoryName + "/subdir";
        final Archive<?> archive = this.getArchive().addAsDirectory(subDirectoryName);

        // Preconditions
        Assert.assertNull("Test archive should contain the directory, not content", archive.get(subDirectoryName)
            .getAsset());

        // Attempt delete
        final Path path = fs.getPath(directoryName);
        boolean gotException = false;
        try {
            Files.delete(path);
        } catch (final DirectoryNotEmptyException dnee) {
            gotException = true;
        }
        Assert.assertTrue("Should not be able to delete non-empty directory", gotException);
    }

    @Test
    public void createDirectory() throws IOException {
        final String dirName = "/newDirectory";
        final Path dir = fs.getPath(dirName);

        // Ensure dir doesn't exist
        final Archive<?> archive = this.getArchive();
        Assert.assertFalse(archive.contains(dirName));

        // Attempt create
        final Path createdDir = Files.createDirectory(dir, (FileAttribute<?>) null);
        Assert.assertTrue("Archive does not contain created directory", archive.contains(dirName));
        Assert.assertTrue("Created path is not a directory", archive.get(dirName).getAsset() == null);
        Assert.assertEquals("Created directory name was not as expected", dirName, createdDir.toString());
    }

    @Test
    public void createDirectoryRecursiveProhibited() throws IOException {
        final String dirName = "/newDirectory/child";
        final Path dir = fs.getPath(dirName);

        // Ensure dir doesn't exist
        final Archive<?> archive = this.getArchive();
        Assert.assertFalse(archive.contains(dirName));

        // Attempt create
        boolean gotException = false;
        try {
            Files.createDirectory(dir, (FileAttribute<?>) null);
        }
        // Just check for IOException, expected to be thrown via the NIO.2 API (wouldn't be *my* choice)
        catch (final IOException ioe) {
            gotException = true;
        }
        Assert.assertTrue("Should not be able to create directory unless parents are first present", gotException);
    }

    @Ignore
    // Implement Path.relativize()
    @Test
    public void createDirectoriesRecursive() throws IOException {
        final String dirName = "/newDirectory/child";
        final Path dir = fs.getPath(dirName);

        // Ensure dir doesn't exist
        final Archive<?> archive = this.getArchive();
        Assert.assertFalse(archive.contains(dirName));

        // Attempt create
        final Path createdDir = Files.createDirectories(dir, (FileAttribute<?>) null);
        Assert.assertTrue("Archive does not contain created directory", archive.contains(dirName));
        Assert.assertTrue("Created path is not a directory", archive.get(dirName).getAsset() == null);
        Assert.assertEquals("Created directory name was not as expected", dirName, createdDir.toString());
    }

    /**
     * Gets the archive associated with the filesystem
     *
     * @return
     */
    private Archive<?> getArchive() {
        final ShrinkWrapFileSystem swfs = (ShrinkWrapFileSystem) this.fs;
        final Archive<?> archive = swfs.getArchive();
        return archive;
    }

}
