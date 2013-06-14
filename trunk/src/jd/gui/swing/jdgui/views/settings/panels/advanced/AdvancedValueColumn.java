package jd.gui.swing.jdgui.views.settings.panels.advanced;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.config.annotations.EnumLabel;
import org.appwork.swing.exttable.ExtColumn;
import org.appwork.swing.exttable.columns.ExtCheckColumn;
import org.appwork.swing.exttable.columns.ExtCompoundColumn;
import org.appwork.swing.exttable.columns.ExtSpinnerColumn;
import org.appwork.swing.exttable.columns.ExtTextAreaColumn;
import org.appwork.swing.exttable.columns.ExtTextColumn;
import org.appwork.utils.reflection.Clazz;
import org.appwork.utils.swing.dialog.Dialog;
import org.jdownloader.actions.AppAction;
import org.jdownloader.controlling.contextmenu.gui.ExtPopupMenu;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;
import org.jdownloader.settings.advanced.AdvancedConfigEntry;
import org.jdownloader.settings.advanced.RangeValidator;

import sun.swing.SwingUtilities2;

public class AdvancedValueColumn extends ExtCompoundColumn<AdvancedConfigEntry> {

    private static final long                              serialVersionUID = 1L;
    private ExtTextColumn<AdvancedConfigEntry>             stringColumn;
    private ExtCheckColumn<AdvancedConfigEntry>            booleanColumn;
    private ExtTextAreaColumn<AdvancedConfigEntry>         defaultColumn;
    private java.util.List<ExtColumn<AdvancedConfigEntry>> columns;
    private ExtSpinnerColumn<AdvancedConfigEntry>          longColumn;
    private ExtTextColumn<AdvancedConfigEntry>             enumColumn;

    public AdvancedValueColumn() {
        super(_GUI._.AdvancedValueColumn_AdvancedValueColumn_object_());
        columns = new ArrayList<ExtColumn<AdvancedConfigEntry>>();
        initColumns();
    }

    @Override
    public boolean isEnabled(AdvancedConfigEntry obj) {
        return true;
    }

    @Override
    public boolean isHidable() {
        return false;
    }

    private void initColumns() {
        stringColumn = new ExtTextColumn<AdvancedConfigEntry>(getName()) {
            private static final long serialVersionUID = 1L;
            {
                rendererField.setHorizontalAlignment(SwingConstants.RIGHT);

            }

            @Override
            public boolean isEditable(final AdvancedConfigEntry obj) {
                return obj.isEditable();
            }

            @Override
            public String getStringValue(AdvancedConfigEntry value) {
                return value.getValue() + "";
            }

            @Override
            protected String getTooltipText(AdvancedConfigEntry obj) {
                return obj.getDescription();
            }

            @Override
            protected void setStringValue(String value, AdvancedConfigEntry object) {
                object.setValue(value);
                AdvancedValueColumn.this.getModel().getTable().repaint();
            }
        };
        register(stringColumn);
        defaultColumn = new ExtTextAreaColumn<AdvancedConfigEntry>(getName()) {
            private static final long serialVersionUID = 1L;

            {
                renderer.setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            protected String getTooltipText(AdvancedConfigEntry obj) {
                return obj.getDescription();
            }

            @Override
            public boolean isEditable(final AdvancedConfigEntry obj) {
                return obj.isEditable();
            }

            @Override
            public String getStringValue(AdvancedConfigEntry value) {
                return JSonStorage.toString(value.getValue());
            }

            @Override
            protected void setStringValue(String value, AdvancedConfigEntry object) {

                Object newV = JSonStorage.restoreFromString(value, new TypeRef<Object>(object.getType()) {
                }, null);
                if (newV != null) {
                    object.setValue(newV);
                    AdvancedValueColumn.this.getModel().getTable().repaint();
                } else {
                    if (!"null".equalsIgnoreCase(value.trim())) {
                        Dialog.getInstance().showErrorDialog("'" + value + "' is not a valid '" + object.getTypeString() + "'");
                    }
                }

            }
        };
        register(defaultColumn);
        booleanColumn = new ExtCheckColumn<AdvancedConfigEntry>(getName()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected boolean getBooleanValue(AdvancedConfigEntry value) {
                return (Boolean) value.getValue();
            }

            @Override
            public boolean isEditable(final AdvancedConfigEntry obj) {
                return obj.isEditable();
            }

            {
                this.renderer.setHorizontalAlignment(SwingConstants.RIGHT);
                renderer.setHorizontalTextPosition(SwingConstants.LEFT);

                this.editor.setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            protected void setBooleanValue(boolean value, AdvancedConfigEntry object) {
                object.setValue(value);
                AdvancedValueColumn.this.getModel().getTable().repaint();
            }
        };
        register(booleanColumn);

        longColumn = new ExtSpinnerColumn<AdvancedConfigEntry>(getName()) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEditable(final AdvancedConfigEntry obj) {
                return obj.isEditable();
            }

            @Override
            protected SpinnerNumberModel getModel(AdvancedConfigEntry value, Number n) {
                SpinnerNumberModel ret = super.getModel(value, n);

                if (value.getValidator() != null) {
                    if (value.getValidator() instanceof RangeValidator) {

                        if (Clazz.isDouble(n.getClass())) {
                            ret.setMaximum((double) ((RangeValidator) value.getValidator()).getMax());
                            ret.setMinimum((double) ((RangeValidator) value.getValidator()).getMin());
                            ret.setStepSize((double) ((RangeValidator) value.getValidator()).getSteps());
                        } else if (Clazz.isFloat(n.getClass())) {
                            ret.setMaximum((float) ((RangeValidator) value.getValidator()).getMax());
                            ret.setMinimum((float) ((RangeValidator) value.getValidator()).getMin());
                            ret.setStepSize((float) ((RangeValidator) value.getValidator()).getSteps());
                        } else if (Clazz.isLong(n.getClass())) {
                            ret.setMaximum((long) ((RangeValidator) value.getValidator()).getMax());
                            ret.setMinimum((long) ((RangeValidator) value.getValidator()).getMin());
                            ret.setStepSize((long) ((RangeValidator) value.getValidator()).getSteps());
                        } else if (Clazz.isInteger(n.getClass())) {
                            ret.setMaximum((int) ((RangeValidator) value.getValidator()).getMax());
                            ret.setMinimum((int) ((RangeValidator) value.getValidator()).getMin());
                            ret.setStepSize((int) ((RangeValidator) value.getValidator()).getSteps());
                        } else if (Clazz.isShort(n.getClass())) {
                            ret.setMaximum((short) ((RangeValidator) value.getValidator()).getMax());
                            ret.setMinimum((short) ((RangeValidator) value.getValidator()).getMin());
                            ret.setStepSize((short) ((RangeValidator) value.getValidator()).getSteps());
                        } else if (Clazz.isByte(n.getClass())) {
                            ret.setMaximum((byte) ((RangeValidator) value.getValidator()).getMax());
                            ret.setMinimum((byte) ((RangeValidator) value.getValidator()).getMin());
                            ret.setStepSize((byte) ((RangeValidator) value.getValidator()).getSteps());
                        }

                    }
                }
                return ret;

            }

            @Override
            protected Number getNumber(AdvancedConfigEntry value) {
                return (Number) value.getValue();
            }

            @Override
            protected void setNumberValue(Number value, AdvancedConfigEntry object) {
                object.setValue(value);
                AdvancedValueColumn.this.getModel().getTable().repaint();
            }

            @Override
            public String getStringValue(AdvancedConfigEntry value) {
                return value.getValue() + "";
            }

        };
        register(longColumn);

        enumColumn = new ExtTextColumn<AdvancedConfigEntry>(getName(), null) {
            private static final long serialVersionUID = 1L;
            {
                renderer.removeAll();
                renderer.setLayout(new MigLayout("ins 0", "[grow,fill]0[12]5", "[grow,fill]"));
                renderer.add(rendererField);
                renderer.add(rendererIcon);

            }

            @Override
            public void configureRendererComponent(final AdvancedConfigEntry value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

                this.rendererIcon.setIcon(this.getIcon(value));
                String str = this.getStringValue(value);
                if (str == null) {
                    // under substance, setting setText(null) somehow sets the label
                    // opaque.
                    str = "";
                }

                if (this.getTableColumn() != null) {
                    this.rendererField.setText(SwingUtilities2.clipStringIfNecessary(this.rendererField, this.rendererField.getFontMetrics(this.rendererField.getFont()), str, this.getTableColumn().getWidth() - 18 - 5));
                } else {
                    this.rendererField.setText(str);
                }

            }

            public boolean onSingleClick(final MouseEvent e, final AdvancedConfigEntry value) {

                ExtPopupMenu popup = new ExtPopupMenu();
                try {
                    Object[] values = (Object[]) value.getType().getMethod("values", new Class[] {}).invoke(null, new Object[] {});
                    for (final Object o : values) {
                        popup.add(new JMenuItem(new AppAction() {
                            {

                                EnumLabel lbl = value.getType().getDeclaredField(o.toString()).getAnnotation(EnumLabel.class);
                                if (lbl != null) {
                                    setName(lbl.value());
                                } else {
                                    setName(o.toString());
                                }
                                if (value.getValue() == o) {
                                    setIconKey("enabled");
                                }

                            }

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                value.setValue(o);
                                AdvancedValueColumn.this.getModel().getTable().repaint();
                            }

                        }));
                    }

                    Rectangle bounds = getModel().getTable().getCellRect(getModel().getTable().rowAtPoint(new Point(e.getX(), e.getY())), getModel().getTable().columnAtPoint(new Point(e.getX(), e.getY())), true);
                    Dimension pref = popup.getPreferredSize();
                    popup.setPreferredSize(new Dimension(Math.max(pref.width, bounds.width), pref.height));
                    popup.show(getModel().getTable(), bounds.x, bounds.y + bounds.height);
                    return true;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                return false;
            }

            protected int getSelectedIndex(AdvancedConfigEntry value) {
                return ((Enum<?>) value.getValue()).ordinal();
            }

            protected void setSelectedIndex(int value, AdvancedConfigEntry object) {

                Object[] values;
                try {
                    values = (Object[]) object.getType().getMethod("values", new Class[] {}).invoke(null, new Object[] {});
                    object.setValue(values[value]);
                    AdvancedValueColumn.this.getModel().getTable().repaint();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            protected Icon getIcon(final AdvancedConfigEntry value) {
                return NewTheme.I().getIcon("popdownButton", -1);
            }

            @Override
            public String getStringValue(AdvancedConfigEntry value) {

                try {

                    EnumLabel lbl = value.getType().getDeclaredField(value.getValue().toString()).getAnnotation(EnumLabel.class);
                    if (lbl != null) {

                    return lbl.value(); }
                } catch (Exception e) {

                }
                return value.getValue().toString();
            }
        };
        register(enumColumn);
    }

    private void register(ExtColumn<AdvancedConfigEntry> col) {
        columns.add(col);

    }

    @Override
    public String getSortString(AdvancedConfigEntry o1) {
        return null;
    }

    @Override
    public ExtColumn<AdvancedConfigEntry> selectColumn(AdvancedConfigEntry object) {
        if (object == null) return defaultColumn;
        if (Clazz.isBoolean(object.getType())) {
            return booleanColumn;
        } else if (object.getType() == String.class) {
            return stringColumn;
        } else if (Clazz.isDouble(object.getType()) || Clazz.isFloat(object.getType()) || Clazz.isLong(object.getType()) || Clazz.isInteger(object.getType()) || Clazz.isByte(object.getType())) {
            return longColumn;
        } else if (Enum.class.isAssignableFrom(object.getType())) {
            return enumColumn;
        } else {
            return defaultColumn;
        }

    }

}
