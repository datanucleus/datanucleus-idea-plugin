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

/**
 * Persistence api related annotation class names.
 */
class PersistenceApiConstants {

    //
    // Constants
    //

    static final String ANNOTATION_JPA_ENTITY = "javax.persistence.Entity";

    static final String ANNOTATION_JPA_MAPPED_SUPERCLASS = "javax.persistence.MappedSuperclass";

    static final String ANNOTATION_JPA_EMBEDDABLE = "javax.persistence.Embeddable";

    static final String ANNOTATION_PERSISTENCE_CAPABLE = "javax.jdo.annotations.PersistenceCapable";

    static final String ANNOTATION_PERSISTENCE_AWARE = "javax.jdo.annotations.PersistenceAware";

    //
    // Hidden constructor
    //

    private PersistenceApiConstants() {
        // prohibit instantiation
    }

}
