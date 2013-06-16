/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.plaf.TableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.frostwire.gui.theme.SkinTableUI;
import com.frostwire.gui.theme.ThemeMediator;
import com.limegroup.gnutella.gui.AbstractCellEditor;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;


/**
 * Checklist for Editable/Interactive cell renderers (which will need a corresponding {@link AbstractCellEditor} implementation)
 * 
 * If you are writing a renderer for a cell editor, remember to:
 * 1. Make sure the Model for your table <code>isCellEditable()</model> method returns true for that column.
 * 2. Make sure to add the proper default cell editors on your mediator's setDefaultEditors class (on that particular column).
 * 3. Make sure to add the proper default cell renderer on {@link AbstractTableMediator} <code>setDefaultRenderers()</code> 
 * @author gubatron
 *
 */
abstract public class FWAbstractJPanelTableCellRenderer extends JPanel implements TableCellRenderer {

    private boolean foundLabelsOnFirstPass = true;
    
    private boolean initializedDefaultMouseListeners = false;
    
    @Override
    public Component getTableCellRendererComponent(final JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        initializeDefaultMouseListeners(table);
        updateUIData(value, table, row, column);
        setOpaque(true);
        setEnabled(table.isEnabled());

        if (isSelected) {
            setBackground(ThemeMediator.TABLE_SELECTED_BACKGROUND_ROW_COLOR);
        } else {
            setBackground(row % 2 == 1 ? ThemeMediator.TABLE_ALTERNATE_ROW_COLOR : Color.WHITE);
        }

        //fix labels if you have any
        if (!foundLabelsOnFirstPass) {
            Component[] components = getComponents();
            boolean foundLabels = false;
            for (Component c : components) {
                if (c instanceof JLabel) {
                    ThemeMediator.fixLabelFont((JLabel) c);
                    foundLabels = true;
                }
            }
            foundLabelsOnFirstPass = foundLabels;
        }
        
        return this;
    }

    private void initializeDefaultMouseListeners(final JTable table) {
        if (!initializedDefaultMouseListeners) {
            initializedDefaultMouseListeners = true;
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (!e.getSource().equals(FWAbstractJPanelTableCellRenderer.this)) {
                            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(FWAbstractJPanelTableCellRenderer.this, e.getID(), e.getWhen(), e.getModifiers(), FWAbstractJPanelTableCellRenderer.this.getX() + e.getX(), FWAbstractJPanelTableCellRenderer.this.getY() + e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()));
                        }
                        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(table, e.getID(), e.getWhen(), e.getModifiers(), FWAbstractJPanelTableCellRenderer.this.getX() + e.getX(), FWAbstractJPanelTableCellRenderer.this.getY() + e.getY(), e.getClickCount(), false, e.getButton()));
                    } else {
                        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(table, e.getID(), e.getWhen(), e.getModifiers(), FWAbstractJPanelTableCellRenderer.this.getX() + e.getX(), FWAbstractJPanelTableCellRenderer.this.getY() + e.getY(), e.getClickCount(), true, e.getButton()));
                    }
                    e.consume();
                    FWAbstractJPanelTableCellRenderer.this.invalidate();
                }
            });
            
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    if (table.isEditing()) {
                        TableCellEditor editor = table.getCellEditor();
                        editor.cancelCellEditing();
                    }
                }
            });
        }
    }
    
    protected abstract void updateUIData(Object dataHolder, JTable table, int row, int column);

    protected boolean mouseIsOverRow(JTable table, int row) {
        boolean mouseOver = false;

        try {
            TableUI ui = table.getUI();
            if (ui instanceof SkinTableUI) {
                mouseOver = ((SkinTableUI) ui).getRowAtMouse() == row;
            }
        } catch (Throwable e) {
            // ignore
        }
        return mouseOver;
    }
    
    protected void syncFont(JTable table, JComponent c) {
        Font tableFont = table.getFont();
        if (tableFont != null && !tableFont.equals(c.getFont())) {
            c.setFont(tableFont);
        }
    }
    
    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if (p != null) {
            p = p.getParent();
        }

        // p should now be the JTable.
        boolean colorMatch = (back != null) && (p != null) &&
            back.equals(p.getBackground()) &&
                        p.isOpaque();
        return !colorMatch && super.isOpaque();
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void revalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void repaint(long tm, int x, int y, int width, int height) {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void repaint(Rectangle r) { }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @since 1.5
     */
    public void repaint() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName=="text"
                || propertyName == "labelFor"
                || propertyName == "displayedMnemonic"
                || ((propertyName == "font" || propertyName == "foreground")
                    && oldValue != newValue
                    && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }
    
    /**
     * A subclass of <code>DefaultTableCellRenderer</code> that
     * implements <code>UIResource</code>.
     * <code>DefaultTableCellRenderer</code> doesn't implement
     * <code>UIResource</code>
     * directly so that applications can safely override the
     * <code>cellRenderer</code> property with
     * <code>DefaultTableCellRenderer</code> subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    public static class UIResource extends DefaultTableCellRenderer
        implements javax.swing.plaf.UIResource
    {
    }    
}