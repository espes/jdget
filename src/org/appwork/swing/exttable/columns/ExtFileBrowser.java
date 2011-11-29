/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.exttable.columns
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable.columns;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.appwork.app.gui.MigPanel;
import org.appwork.utils.swing.SwingUtils;

/**
 * @author Thomas
 * 
 */
public abstract class ExtFileBrowser<T> extends ExtComponentColumn<T> implements ActionListener {
    protected MigPanel renderer;
    private JTextField rendererLabel;
    private JButton    rendererButton;
    protected MigPanel editor;
    private JButton    editorButton;
    private JTextField editorLabel;
    private T          editObject;

    private Color      bg;
    private Color      fg;

    @Override
    public void actionPerformed(ActionEvent e) {
        getModel().setSelectedObject(editObject);
        File newFile = browse(editObject);
        setFile(editObject, newFile);
        cancelCellEditing();

    }

    /**
     * @param editObject2
     * @param newFile
     */
    abstract protected void setFile(T object, File newFile);

    /**
     * @param editObject2
     * @return
     */
    abstract public File browse(T object);

    public ExtFileBrowser(String name) {
        super(name);

        rendererButton = new JButton("Browse");

        // renderer
        this.rendererLabel = new JTextField();
        rendererLabel.setEditable(false);
        SwingUtils.setOpaque(rendererLabel, false);
        rendererLabel.setBorder(null);
        this.renderer = new MigPanel("ins 0", "[grow,fill]0[]", "[grow,fill]") {
            public void setForeground(Color fg) {
                super.setForeground(fg);
                rendererLabel.setForeground(fg);

            }

            public void setBackground(Color bg) {
                super.setBackground(bg);
            }

            public void setVisible(boolean aFlag) {
                rendererLabel.setVisible(aFlag);
                rendererButton.setVisible(aFlag);
            }

        };
        renderer.setOpaque(false);
        renderer.add(rendererLabel);
        renderer.add(rendererButton);
        // editor

        bg = rendererLabel.getBackground();
        fg = rendererLabel.getForeground();

        editorButton = new JButton("Browse");

        editorButton.addActionListener(this);
        editorLabel = new JTextField();
        editorLabel.setEditable(false);
        SwingUtils.setOpaque(editorLabel, false);
        editorLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                getModel().setSelectedObject(editObject);
            }
        });
        editorLabel.setBorder(null);
        this.editor = new MigPanel("ins 0", "[grow,fill]0[]", "[grow,fill]") {
            public void setForeground(Color fg) {
                super.setForeground(fg);
                editorLabel.setForeground(fg);

            }

            public void setVisible(boolean aFlag) {
                editorLabel.setVisible(aFlag);
                editorButton.setVisible(aFlag);
            }

            public void setBackground(Color bg) {
                super.setBackground(bg);
            }

        };
        editor.setOpaque(false);
        editor.add(editorLabel);
        editor.add(editorButton);

    }

    @Override
    protected JComponent getInternalEditorComponent(T value, boolean isSelected, int row, int column) {
        // TODO Auto-generated method stub
        return editor;
    }

    @Override
    protected JComponent getInternalRendererComponent(T value, boolean isSelected, boolean hasFocus, int row, int column) {
        return renderer;
    }

    @Override
    public void configureEditorComponent(T value, boolean isSelected, int row, int column) {
     
            editObject = value;
            editorLabel.setText(getFile(value) + "");
            editorLabel.setCaretPosition(0);
        

    }

    @Override
    public void configureRendererComponent(T value, boolean isSelected, boolean hasFocus, int row, int column) {
        rendererLabel.setText(getFile(value) + "");

    }

    /**
     * @param value
     * @return
     */
    abstract public File getFile(T value);

    @Override
    public void resetEditor() {
        editor.setForeground(fg);
        editor.setBackground(bg);
        editor.setOpaque(false);
    }

    @Override
    public void resetRenderer() {
        renderer.setForeground(fg);
        renderer.setBackground(bg);
        renderer.setOpaque(false);
    }

}
