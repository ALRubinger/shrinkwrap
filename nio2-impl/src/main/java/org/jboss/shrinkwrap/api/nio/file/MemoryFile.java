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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * Composes together the {@link SeekableInMemoryByteChannel} implementation such that it may be represented as an
 * {@link Asset}, able to be added to an {@link Archive}
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
class MemoryFile implements Asset, SeekableByteChannel {

    private final SeekableInMemoryByteChannel delegate;

    MemoryFile(final SeekableInMemoryByteChannel delegate) {
        assert delegate != null : "Delegate must be specified";
        this.delegate = delegate;
    }

    /**
     * @return
     * @see java.nio.channels.Channel#isOpen()
     */
    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    /**
     * @throws IOException
     * @see java.nio.channels.Channel#close()
     */
    @Override
    public void close() throws IOException {
        delegate.close();
    }

    /**
     * @param dst
     * @return
     * @throws IOException
     * @see java.nio.channels.SeekableByteChannel#read(java.nio.ByteBuffer)
     */
    @Override
    public int read(final ByteBuffer dst) throws IOException {
        return delegate.read(dst);
    }

    /**
     * @param src
     * @return
     * @throws IOException
     * @see java.nio.channels.SeekableByteChannel#write(java.nio.ByteBuffer)
     */
    @Override
    public int write(final ByteBuffer src) throws IOException {
        return delegate.write(src);
    }

    /**
     * @return
     * @throws IOException
     * @see java.nio.channels.SeekableByteChannel#position()
     */
    @Override
    public long position() throws IOException {
        return delegate.position();
    }

    /**
     * {@inheritDoc}
     *
     * @param newPosition
     * @return
     * @throws IOException
     * @see java.nio.channels.SeekableByteChannel#position(long)
     */
    @Override
    public SeekableByteChannel position(final long newPosition) throws IOException {
        return delegate.position(newPosition);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @throws IOException
     * @see java.nio.channels.SeekableByteChannel#size()
     */
    @Override
    public long size() throws IOException {
        return delegate.size();
    }

    /**
     * @param size
     * @return
     * @throws IOException
     * @see java.nio.channels.SeekableByteChannel#truncate(long)
     */
    @Override
    public SeekableByteChannel truncate(final long size) throws IOException {
        return delegate.truncate(size);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.api.asset.Asset#openStream()
     */
    @Override
    public InputStream openStream() {
        return delegate.getContents();
    }

}
