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

package org.datanucleus.ide.idea.ui.swing;

/* (@)JHintingTextField.java */

/* Copyright 2009 Sebastian Haufe

 * Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Shape;

/**
 * An enhanced text field, showing an {@link #getEmptyTextHint() empty text}
 * hint on its view, if the {@link #getText() text content} is empty.
 *
 * @author Sebastian Haufe
 * @version $Revision: 1.1 $ as of $Date: 2009/04/08 19:13:04 $
 */
@SuppressWarnings("ChainedMethodCall")
public class JHintingTextField extends JTextField {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 5061790840224877676L;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Creates a new <code>JHintingTextField</code>.
     */
    public JHintingTextField() {
        installHighlightPainter();
    }

    /**
     * Creates a new <code>JHintingTextField</code>.
     *
     * @param columns the number of preferred columns to calculate preferred
     *                width
     */
    public JHintingTextField(final int columns) {
        super(columns);
        installHighlightPainter();
    }

    /**
     * Creates a new <code>JHintingTextField</code>.
     *
     * @param text the text to show in the text field
     */
    public JHintingTextField(final String text) {
        super(text);
        installHighlightPainter();
    }

    /**
     * Creates a new <code>JHintingTextField</code>.
     *
     * @param text    the text to show in the text field
     * @param columns the number of preferred columns to calculate preferred
     *                width
     */
    public JHintingTextField(final String text, final int columns) {
        super(text, columns);
        installHighlightPainter();
    }

    /**
     * Creates a new <code>JHintingTextField</code>.
     *
     * @param doc     the text model
     * @param text    the text to show in the text field
     * @param columns the number of preferred columns to calculate preferred
     *                width
     */
    public JHintingTextField(final Document doc, final String text, final int columns) {
        super(doc, text, columns);
        installHighlightPainter();
    }

    // -------------------------------------------------------------------------
    // Hinting highlighter code
    // -------------------------------------------------------------------------

    private void installHighlightPainter() {
        final Highlighter highlighter = getHighlighter();
        try {
            highlighter.addHighlight(0, 0, createHighlightPainter());
        } catch (BadLocationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    protected static Highlighter.HighlightPainter createHighlightPainter() {
        return new Highlighter.HighlightPainter() {

            private final JLabel label = new JLabel("", SwingConstants.TRAILING);

            private static final int GAP = 0;

            @Override
            public void paint(final Graphics g, final int p0, final int p1, final Shape bounds, final JTextComponent c) {

                final String hint = (String) c.getClientProperty("emptyTextHint");
                if (hint == null || hint.isEmpty() || c.getDocument().getLength() != 0) {
                    return;
                }
                this.label.setText(hint);

                final Insets ins = c.getInsets();
                final boolean ltr = c.getComponentOrientation().isLeftToRight();
                if (ltr) {
                    ins.right += GAP;
                } else {
                    ins.left += GAP;
                }

                final Dimension pref = this.label.getPreferredSize();
                final int prHeight = pref.height;
                final int prWidth = pref.width;
                final int w = Math.min(c.getWidth() - ins.left - ins.right, prWidth);
                final int h = Math.min(c.getWidth() - ins.top - ins.bottom, prHeight);
                //final int x = ltr ? c.getWidth() - ins.right - w : ins.left;
                final int x = 0; // put left
                final int parentHeight = c.getHeight() - ins.top - ins.bottom;
                final int y = ins.top + (parentHeight - h) / 2;
                this.label.setForeground(Color.GRAY);
                this.label.setOpaque(false);
                SwingUtilities.paintComponent(g, this.label, c, x, y, w, h);
            }
        };
    }

    // -------------------------------------------------------------------------
    // Bean getters and setters
    // -------------------------------------------------------------------------

    /**
     * Returns the emptyTextHint.
     *
     * @return the emptyTextHint
     */
    public String getEmptyTextHint() {
        return (String) getClientProperty("emptyTextHint");
    }

    /**
     * Sets the emptyTextHint.
     *
     * @param hint the emptyTextHint to set
     */
    public void setEmptyTextHint(final String hint) {
        putClientProperty("emptyTextHint", hint);
        repaint();
    }

}
