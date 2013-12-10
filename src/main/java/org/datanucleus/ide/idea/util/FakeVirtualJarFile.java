/*******************************************************************************
 * Copyright (c) 2010 Gerold Klinger and sourceheads Information Technology GmbH.
 * All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ...
 ******************************************************************************/

package org.datanucleus.ide.idea.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: geri
 * Date: 10.09.12
 * Time: 19:11
 */
@SuppressWarnings("MagicCharacter")
public class FakeVirtualJarFile extends VirtualFile {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final VirtualFile[] EMPTY__VIRTUAL_FILES_ARRAY = new VirtualFile[0];

    private final String path;

    public FakeVirtualJarFile(final String path) {
        Validate.notEmpty(path.trim(), "path is null or empty!");
        this.path = FileUtil.toSystemDependentName(path);
    }


    @NotNull
    @Override
    public String getName() {
        return this.path.substring(this.path.lastIndexOf(File.separator) + 1);
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        return new FakeVirtualFileSystem();
    }

    @Override
    public String getPath() {
        return this.path + '!' + File.separator;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public VirtualFile getParent() {
        return null;
    }

    @Override
    public VirtualFile[] getChildren() {
        return EMPTY__VIRTUAL_FILES_ARRAY;
    }

    @NotNull
    @Override
    public OutputStream getOutputStream(final Object o, final long l, final long l1) throws IOException {

        return new ByteArrayOutputStream();
    }

    @NotNull
    @Override
    public byte[] contentsToByteArray() throws IOException {
        return EMPTY_BYTE_ARRAY;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public void refresh(final boolean b, final boolean b1, @Nullable final Runnable runnable) {
      
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(EMPTY_BYTE_ARRAY);
    }

}
