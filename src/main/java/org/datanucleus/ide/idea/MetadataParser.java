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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.Set;

import com.intellij.openapi.vfs.VirtualFile;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility methods for parsing persistence metadata files (JDO and JPA).<br/>
 * <br/>
 * This does not include JPA 'persistence.xml' files (see {@link #parseQualifiedClassNames(com.intellij.openapi.vfs.VirtualFile)}).
 */
final class MetadataParser {

    private static final String[] JDO_METADATA_ROOT_NODES = {"jdo", "orm"};

    private static final String[] JPA_METADATA_ROOT_NODES = {"entity", "mapped-superclass", "embeddable"};

    private MetadataParser() {
        // no instantiation allowed
    }

    //
    // Utility methods
    //

    /**
     * Load a file's content into String.
     *
     * @param file the file to load from
     * @return {@link java.lang.String} containing file's content
     * @throws IOException .
     */
    static String loadStringFromFile(final VirtualFile file) throws IOException {
        return new String(file.contentsToByteArray(), file.getCharset());
    }

    /**
     * Get qualified class names from metadata file (either JDO '.jdo' and '.orm' or JPA '.xml' orm file (not 'persistence.xml'!)).
     *
     * @param file the metadata file.
     * @return Unique collection of class names configured in metadata file
     * @throws ParserConfigurationException .
     * @throws IOException                  .
     * @throws SAXException                 .
     * @throws XPathExpressionException     .
     */
    static Set<String> parseQualifiedClassNames(final VirtualFile file)
            throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        final String xml = loadStringFromFile(file);

        final Document doc = createDocumentBuilderFactory(xml);

        final Set<String> qualifiedClassNames = new LinkedHashSet<String>();

        //
        // parse metadata files (intentionally kept simple - no patterns)

        final Set<String> qualifiedClassNamesJdo = parseQualifiedClassNamesJdo(doc);

        if (qualifiedClassNamesJdo.isEmpty()) {
            final Set<String> qualifiedClassNamesJpa = parseQualifiedClassNamesJpa(doc);
            if (!qualifiedClassNamesJpa.isEmpty()) {
                qualifiedClassNames.addAll(qualifiedClassNamesJpa);
            }
        } else {
            qualifiedClassNames.addAll(qualifiedClassNamesJdo);
        }

        return qualifiedClassNames;
    }

    //
    // Helper methods
    //

    private static Set<String> parseQualifiedClassNamesJdo(final Document doc)
            throws XPathExpressionException {

        final Set<String> jpaClassNames = new LinkedHashSet<String>();

        for (final String jdoMetadataRootNode : JDO_METADATA_ROOT_NODES) {
            final Set<String> classNames = parseQualifiedClassNamesJdoInternal(doc, jdoMetadataRootNode);
            if (!classNames.isEmpty()) {
                jpaClassNames.addAll(classNames);
            }
        }
        return jpaClassNames;
    }

    private static Set<String> parseQualifiedClassNamesJdoInternal(final Document doc, final String rootNodeName)
            throws XPathExpressionException {


        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        final XPathExpression expr1 = xpath.compile("//" + rootNodeName + "/package/@name");

        final NodeList packageNodes = (NodeList) expr1.evaluate(doc, XPathConstants.NODESET);

        final Set<String> packageAndClasses = new LinkedHashSet<String>();
        for (int i = 0; i < packageNodes.getLength(); ++i) {
            final Node packageNode = packageNodes.item(i);
            final String packageName = packageNode.getNodeValue();

            final XPathExpression expr2 = xpath.compile("//" + rootNodeName + "/package[@name=\"" + packageName + "\"]/class/@name");
            final NodeList classNodes = (NodeList) expr2.evaluate(doc, XPathConstants.NODESET);

            for (int j = 0; j < packageNodes.getLength(); ++j) {
                final Node classNodeItem = classNodes.item(j);
                final String className = classNodeItem.getNodeValue();

                if (className != null) {
                    packageAndClasses.add(packageName + '.' + className);
                }
            }
        }

        return packageAndClasses;
    }

    private static Set<String> parseQualifiedClassNamesJpa(final Document doc)
            throws XPathExpressionException {

        final Set<String> jpaClassNames = new LinkedHashSet<String>();

        for (final String jpaMetadataRootNode : JPA_METADATA_ROOT_NODES) {
            final Set<String> classNames = parseQualifiedClassNamesJpaInternal(doc, jpaMetadataRootNode);
            if (!classNames.isEmpty()) {
                jpaClassNames.addAll(classNames);
            }
        }
        return jpaClassNames;
    }

    private static Set<String> parseQualifiedClassNamesJpaInternal(final Document doc, final String rootNodeName)
            throws XPathExpressionException {


        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        final XPathExpression expr1 = xpath.compile("/entity-mappings/" + rootNodeName + "/@class");

        final NodeList classNodes = (NodeList) expr1.evaluate(doc, XPathConstants.NODESET);

        final Set<String> packageAndClasses = new LinkedHashSet<String>();
        for (int i = 0; i < classNodes.getLength(); ++i) {
            final Node classNodeItem = classNodes.item(i);
            final String className = classNodeItem.getNodeValue();

            if (className != null) {
                packageAndClasses.add(className);
            }
        }

        return packageAndClasses;
    }

    private static Document createDocumentBuilderFactory(final String xmlMetadata)
            throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new InputSource(new StringReader(xmlMetadata)));
    }

}
