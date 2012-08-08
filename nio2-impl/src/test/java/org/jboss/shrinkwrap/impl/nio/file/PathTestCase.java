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
package org.jboss.shrinkwrap.impl.nio.file;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases to assert the ShrinkWrap implementation of the NIO.2 {@link Path} is working as contracted.
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class PathTestCase {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(PathTestCase.class.getName());

    private FileSystem fileSystem;

    @Before
    public void createFileSystem() throws URISyntaxException, IOException {

        // Setup and mount the archive
        final String name = "test.jar";
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, name);
        final FileSystem fs = ShrinkWrapFileSystems.newFileSystem(archive);
        this.fileSystem = fs;
    }

    @After
    public void closeFs() throws IOException {
        this.fileSystem.close();
    }

    @Test
    public void rootIsAbsolute() {
        final Path path = fileSystem.getPath("/");
        Assert.assertTrue("Root path must be absolute", path.isAbsolute());
        Assert.assertEquals("Root path should be equal to root archive path value", path.toString(), ArchivePaths
            .root().get());
    }

    @Test
    public void getFileSystem() {
        final Path path = fileSystem.getPath("/");
        Assert.assertEquals("FileSystem not obtained correctly via Path", fileSystem, path.getFileSystem());
    }

    @Test
    public void emptyStringBecomesRoot() {
        final Path path = fileSystem.getPath("");
        Assert.assertEquals("Empty path should be resolved to root archive path value", path.toString(), ArchivePaths
            .root().get());
    }

    @Test
    public void relativeCorrectedToAbsolute() {
        final Path path = fileSystem.getPath("relativePath");
        Assert.assertTrue("Relative paths must be adjusted to absolute", path.isAbsolute());
        Assert.assertEquals("Relative input was not adjusted to absolute", path.toString(), "/relativePath");
    }

    @Test
    public void getRoot() {
        final Path path = fileSystem.getPath("someNode");
        final Path root = path.getRoot();
        Assert.assertEquals("Did not return correct roor", root.toString(), "/");
    }

    @Test
    public void getRootFromNested() {
        final Path path = fileSystem.getPath("someNode/child");
        final Path root = path.getRoot();
        Assert.assertEquals("Did not return correct roor", root.toString(), "/");
    }

    @Test
    public void parent() {
        final Path path = fileSystem.getPath("parent/child");
        final Path parent = path.getParent();
        Assert.assertEquals("Did not return correct parent", parent.toString(), "/parent");
    }

    @Test
    public void nestedParent() {
        final Path path = fileSystem.getPath("parent/child/grandchild");
        final Path parent = path.getParent();
        Assert.assertEquals("Did not return correct parent", parent.toString(), "/parent/child");
    }

    @Test
    public void rootofParentIsNull() {
        final Path path = fileSystem.getPath("/");
        final Path parent = path.getParent();
        Assert.assertNull("Parent of root should be null", parent);
    }

    @Test
    public void getFileName() {
        final String location = "/dir/nestedDir/filename";
        final Path path = fileSystem.getPath(location);
        final Path fileName = path.getFileName();
        Assert.assertEquals("File name was not as expected", location, fileName.toString());
    }

    @Test
    public void getRootFileName() {
        final Path path = fileSystem.getPath("/");
        final Path fileName = path.getFileName();
        Assert.assertNull("Root file name should be null", fileName);
    }

    @Test
    public void getRootNameCount() {
        final Path path = fileSystem.getPath("/");
        final int count = path.getNameCount();
        Assert.assertEquals("Root should have no name count", 0, count);
    }

    @Test
    public void getTopLevelNameCount() {
        final Path path = fileSystem.getPath("/toplevel");
        final int count = path.getNameCount();
        Assert.assertEquals("Top-level element should have name count 1", 1, count);
    }

    @Test
    public void getTopLevelAppendedSlashNameCount() {
        final Path path = fileSystem.getPath("/toplevel/");
        final int count = path.getNameCount();
        Assert.assertEquals("Top-level element should have name count 1", 1, count);
    }

    @Test
    public void getTopLevelNoPrecedingSlashNameCount() {
        final Path path = fileSystem.getPath("toplevel/");
        final int count = path.getNameCount();
        Assert.assertEquals("Top-level element should have name count 1", 1, count);
    }

    @Test
    public void nestedNameCount() {
        final Path path = fileSystem.getPath("toplevel/nested");
        final int count = path.getNameCount();
        Assert.assertEquals("nested-level element should have name count 2", 2, count);
    }

    @Test
    public void toAbsolutePath() {
        final Path path = fileSystem.getPath("toplevel");
        Assert.assertEquals("toAbsolute should return the absolute form of the Path", "/toplevel", path
            .toAbsolutePath().toString());
    }
}
