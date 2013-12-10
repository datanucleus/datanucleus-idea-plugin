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

import java.io.File;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;

import org.apache.commons.lang.Validate;

/**
 * User: geri
 * Date: 09.09.12
 * Time: 18:25
 */
public abstract class VirtualFileUtils {

    private VirtualFileUtils() {
        // prohibit derivation
    }

    public static File toIOFile(final VirtualFile virtualFile) {
        return new File(virtualFile.getPresentableUrl());
    }

    public static boolean existsOnFilesystem(final VirtualFile virtualFile) {
        return toIOFile(virtualFile).exists();
    }

    public static VirtualFile getVirtualFileForPath(final String path) {
        final VirtualFileSystem fileSystem = VirtualFileManager.getInstance().getFileSystem(LocalFileSystem.PROTOCOL);
        VirtualFile fileByPath = fileSystem.findFileByPath(path);
        if (fileByPath == null) {
            fileByPath = createStub(path);
        }
        return fileByPath;
    }

    public static VirtualFile createStub(final String path) {
        return new FakeVirtualJarFile(path);
    }

    public static String toPathString(final VirtualFile virtualFile) {
        Validate.notNull(virtualFile, "virtual file is null!");
        String vfPath = virtualFile.getPath();
        if (vfPath.toLowerCase().endsWith(".jar!/")) {
            vfPath = vfPath.substring(0, vfPath.length() - 2);
        }
        return vfPath;
    }
}
