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

package org.datanucleus.ide.idea;

import java.text.MessageFormat;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;

import org.datanucleus.ide.idea.integration.EnhancerSupport;
import org.datanucleus.ide.idea.util.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

/**
 * User: geri
 * Date: 11.09.12
 * Time: 05:25
 */
public class DNEComputableLoggerWrapper {

    private final Logger logger;

    private DNEState state = null;
    private CompileContext cCtx = null;
    private Module module = null;

    public DNEComputableLoggerWrapper(final Logger logger) {
        this.logger = logger;
    }

    void update(final DNEState state, @Nullable final CompileContext cCtx, @Nullable final Module module) {
        this.state = state;
        this.cCtx = cCtx;
        this.module = module;
    }

    public void error(final String msg) {
        error(msg, null);
    }

    public void error(final String msg, @Nullable final Throwable t) {
        final String message = msgForLog(this.state, this.module, msg);
        if (t == null) {
            this.logger.error(message);
            this.uiLogMsg(CompilerMessageCategory.ERROR, message);
        } else {
            this.logger.error(message, t);
            final String stackTrace = ExceptionUtils.stackTraceToString(t);
            this.uiLogMsg(CompilerMessageCategory.ERROR, message + "\n\n" + stackTrace);
        }
    }

    public void warn(final String msg) {
        final String message = msgForLog(this.state, this.module, msg);
        this.logger.warn(message);
        this.uiLogMsg(CompilerMessageCategory.WARNING, message);
    }

    public void info(final String msg) {
        final String message = msgForLog(this.state, this.module, msg);
        this.logger.info(message);
        this.uiLogMsg(CompilerMessageCategory.INFORMATION, message);
    }

    public void debug(final String msg) {
        final String message = msgForLog(this.state, this.module, msg);
        this.logger.debug(message);

        // No debug messages to ui message window
        // this.uiLogMsg(CompilerMessageCategory.INFORMATION, message);
    }

    @SuppressWarnings("ConstantConditions")
    private static String msgForLog(final DNEState state, @Nullable final Module module, final String msg) {
        final EnhancerSupport enhancerSupport = state == null ? null : state.getEnhancerSupport();
        final PersistenceApi persistenceApi = state == null ? null : state.getApi();
        final String eSuppName = enhancerSupport == null ? "-" : enhancerSupport.getName();
        final String persApiName = persistenceApi == null ? "-" : persistenceApi.name();
        final String modName = module == null ? "-" : module.getName();
        return MessageFormat.format("Enhancer | {0}[{1}] |{2}| " + msg, eSuppName, persApiName, modName);
    }

    @SuppressWarnings("MagicCharacter")
    private void uiLogMsg(final CompilerMessageCategory cat, final String msg) {
        if (this.cCtx != null) {
            final EnhancerSupport enhancerSupport = this.state.getEnhancerSupport();
            this.cCtx.addMessage(cat, msg, null, -1, -1);
        } else {
            this.logger.warn("UI message logger called, but no CompileContext available. Original message: " + msg);
        }
    }

    @SuppressWarnings("ObjectEquality")
    private boolean isCurrentInstance(final DNEComputableLoggerWrapper logger) {
        return this == logger;
    }

}
