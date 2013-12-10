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

import java.io.IOException;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: geri
 * Date: 10.09.12
 * Time: 19:36
 */
public class FakeVirtualFileSystem extends VirtualFileSystem {
    @NotNull
    @Override
    public String getProtocol() {
        return "file";
    }

    @Override
    public VirtualFile findFileByPath(@NotNull @NonNls final String s) {
        return null;  
    }

    @Override
    public void refresh(final boolean b) {
        
    }

    @Override
    public VirtualFile refreshAndFindFileByPath(@NotNull final String s) {
        return null;  
    }

    @Override
    public void addVirtualFileListener(@NotNull final VirtualFileListener virtualFileListener) {
        
    }

    @Override
    public void removeVirtualFileListener(@NotNull final VirtualFileListener virtualFileListener) {
        
    }

    @Override
    protected void deleteFile(final Object o, @NotNull final VirtualFile virtualFile) throws IOException {
        
    }

    @Override
    protected void moveFile(final Object o, @NotNull final VirtualFile virtualFile, @NotNull final VirtualFile virtualFile1) throws IOException {
        
    }

    @Override
    protected void renameFile(final Object o, @NotNull final VirtualFile virtualFile, @NotNull final String s) throws IOException {
        
    }

    @Override
    protected VirtualFile createChildFile(final Object o, @NotNull final VirtualFile virtualFile, @NotNull final String s) throws IOException {
        return null;  
    }

    @NotNull
    @Override
    protected VirtualFile createChildDirectory(final Object o, @NotNull final VirtualFile virtualFile, @NotNull final String s) throws IOException {
        return null;  
    }

    @Override
    protected VirtualFile copyFile(final Object o, @NotNull final VirtualFile virtualFile, @NotNull final VirtualFile virtualFile1, @NotNull final String s) throws IOException {
        return null;  
    }

    @Override
    public boolean isReadOnly() {
        return false;  
    }
}
