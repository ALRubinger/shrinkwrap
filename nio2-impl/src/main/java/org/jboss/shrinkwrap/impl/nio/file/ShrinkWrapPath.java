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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;

/**
 * NIO.2 {@link Path} implementation adapting to the {@link ArchivePath} construct in a ShrinkWrap {@link Archive}
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class ShrinkWrapPath implements Path {

    /**
     * Internal ShrinkWrap {@link ArchivePath} congruent construct
     */
    private final ArchivePath delegate;

    /**
     * Owning {@link ShrinkWrapFileSystem}
     */
    private final ShrinkWrapFileSystem fileSystem;

    /**
     * Constructs a new instance using the specified (required) underlying delegate
     *
     * @param delegate
     * @throws IllegalArgumentException
     *             If the delegate is not specified
     */
    ShrinkWrapPath(final ArchivePath delegate, final ShrinkWrapFileSystem fileSystem) throws IllegalArgumentException {
        if (delegate == null) {
            throw new IllegalArgumentException(ArchivePath.class.getSimpleName() + " delegate must be specified");
        }
        if (fileSystem == null) {
            throw new IllegalArgumentException("File system must be specified.");
        }
        this.delegate = delegate;
        this.fileSystem = fileSystem;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#getFileSystem()
     */
    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#isAbsolute()
     */
    @Override
    public boolean isAbsolute() {
        // All ArchivePaths are absolute
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#getRoot()
     */
    @Override
    public Path getRoot() {
        return new ShrinkWrapPath(ArchivePaths.root(), fileSystem);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#getFileName()
     */
    @Override
    public Path getFileName() {
        // Root has no file name
        if (delegate.equals(ArchivePaths.root())) {
            return null;
        }
        // Because these paths have no notion of relativity, we must return the full absolute form
        else {
            return this;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#getParent()
     */
    @Override
    public Path getParent() {
        final ArchivePath parent = delegate.getParent();
        if (parent == null) {
            return null;
        }
        return new ShrinkWrapPath(parent, fileSystem);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#getNameCount()
     */
    @Override
    public int getNameCount() {
        String context = this.delegate.get();
        // Kill trailing slashes
        if (context.endsWith(ArchivePath.SEPARATOR_STRING)) {
            context = context.substring(0, context.length() - 1);
        }
        // Kill preceding slashes
        if (context.startsWith(ArchivePath.SEPARATOR_STRING)) {
            context = context.substring(1);
        }
        // Root
        if (context.length() == 0) {
            return 0;
        }
        // Else count names by using the separator
        final int pathSeparators = this.countOccurrences(context, ArchivePath.SEPARATOR, 0);
        return pathSeparators + 1;
    }

    /**
     * Returns the number of occurrences of the specified character in the specified {@link String}, starting at the
     * specified offset
     *
     * @param string
     * @param c
     * @param offset
     * @return
     */
    private int countOccurrences(final String string, char c, int offset) {
        assert string != null : "String must be specified";
        return ((offset = string.indexOf(c, offset)) == -1) ? 0 : 1 + countOccurrences(string, c, offset + 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#getName(int)
     */
    @Override
    public Path getName(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#subpath(int, int)
     */
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#startsWith(java.nio.file.Path)
     */
    @Override
    public boolean startsWith(Path other) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#startsWith(java.lang.String)
     */
    @Override
    public boolean startsWith(String other) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#endsWith(java.nio.file.Path)
     */
    @Override
    public boolean endsWith(Path other) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#endsWith(java.lang.String)
     */
    @Override
    public boolean endsWith(String other) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#normalize()
     */
    @Override
    public Path normalize() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#resolve(java.nio.file.Path)
     */
    @Override
    public Path resolve(Path other) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#resolve(java.lang.String)
     */
    @Override
    public Path resolve(String other) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#resolveSibling(java.nio.file.Path)
     */
    @Override
    public Path resolveSibling(Path other) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#resolveSibling(java.lang.String)
     */
    @Override
    public Path resolveSibling(String other) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#relativize(java.nio.file.Path)
     */
    @Override
    public Path relativize(final Path other) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#toUri()
     */
    @Override
    public URI toUri() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#toAbsolutePath()
     */
    @Override
    public Path toAbsolutePath() {
        // Paths are already absolute
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#toRealPath(java.nio.file.LinkOption[])
     */
    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#toFile()
     */
    @Override
    public File toFile() {
        throw new UnsupportedOperationException(
            "This path is associated with a ShrinkWrap archive, not the default provider");
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#register(java.nio.file.WatchService, java.nio.file.WatchEvent.Kind<?>[],
     *      java.nio.file.WatchEvent.Modifier[])
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("ShrinkWrap Paths do not support registration with a watch service.");
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#register(java.nio.file.WatchService, java.nio.file.WatchEvent.Kind<?>[])
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        return this.register(watcher, events, (Modifier) null);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#iterator()
     */
    @Override
    public Iterator<Path> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#compareTo(java.nio.file.Path)
     */
    @Override
    public int compareTo(final Path other) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.nio.file.Path#toString()
     */
    @Override
    public String toString() {
        return this.delegate.get();
    }

}
