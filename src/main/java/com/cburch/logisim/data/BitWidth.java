/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.data;

import com.cburch.logisim.gui.generic.ComboBox;
import com.cburch.logisim.util.StringGetter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Component;
import javax.swing.DefaultComboBoxModel;

public class BitWidth implements Comparable<BitWidth> {
  static class Attribute extends com.cburch.logisim.data.Attribute<BitWidth> {
    private final BitWidth[] choices;
    private ComboBox combo;
    private class BitWidthComboBoxModel extends DefaultComboBoxModel<BitWidth> {
        private int customWidths = 0;
        public void setSelectedItem(Object obj) {
          if(obj instanceof BitWidth bw) {
            int i = getIndexOf(bw);
            if(i < 0) {
              if(bw.getWidth() > Math.min(64,choices[choices.length-1].getWidth())) {
                // assume highest bit width is at the bottom
                super.setSelectedItem(getElementAt(getSize() - 1));
              } else {
                // non-standard bitwidths go at the top, but only save 3
                if(customWidths >= 3) {
                  removeElementAt(2);
                  customWidths --;
                }
                insertElementAt(bw,0);
                customWidths ++;
                super.setSelectedItem(bw);
              }
            } else {
              super.setSelectedItem(bw);
            }
          } else if (obj instanceof String s) {
            try {
              int i = Integer.valueOf(s);
              if( i > choices[choices.length-1].getWidth()) {
                super.setSelectedItem(getElementAt(getSize() - 1));
              } else if( i <= 0) {
                super.setSelectedItem(getElementAt(0));
              } else {
                setSelectedItem(choices[i-1]);
              }
            } catch (NumberFormatException nfe) {}
          }
        }
        public void addElement(BitWidth bw) {
          if(bw.getWidth() > 64) return;
          super.addElement(bw);
        }
        public void insertElementAt(BitWidth bw,int i) {
          if(bw.getWidth() > 64) return;
          super.insertElementAt(bw,i);
        }
    }
    public Attribute(String name, StringGetter disp) {
      super(name, disp);
      ensurePrefab();
      choices = prefab;
    }

    public Attribute(String name, StringGetter disp, int min, int max) {
      super(name, disp);
      choices = new BitWidth[max - min + 1];
      for (int i = 0; i < choices.length; i++) {
        choices[i] = BitWidth.create(min + i);
      }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Component getCellEditor(BitWidth value) {
      if(combo != null) {
        System.out.println("Returned existing combobox.");
        combo.setSelectedItem(value);
        return combo;
      }
      combo = new ComboBox<BitWidth>( new BitWidthComboBoxModel() );
      //Current value added to top of dropdown menu
      if (value != null) {
        int wid = value.getWidth();
        
        if (wid != 1 && wid % 8 != 0) {
          // non-standard width to combobox
          combo.addItem(value);
        }
      }

      //Dropdown menu items
      if(choices.length < 8) {
        // small max width
        for(int i = 0; i < choices.length; i++) {
          combo.addItem(choices[i]);
        }
      } else {
        // large max width
        combo.addItem(BitWidth.ONE);
        for(int i = 7; i < choices.length; i+=8) {
          combo.addItem(choices[i]);
        }
      }

      combo.setMaximumRowCount(combo.getItemCount());
      combo.setEditable(true);
      // always select current value
      combo.setSelectedItem(value);
      return combo;
    }

    @Override
    public BitWidth parse(String value) {
      return BitWidth.parse(value);
    }
  }

  public static BitWidth create(int width) {
    ensurePrefab();
    if (width <= 0) {
      if (width == 0) {
        return UNKNOWN;
      } else {
        throw new IllegalArgumentException("width " + width + " must be positive");
      }
    } else if (width - 1 < prefab.length) {
      return prefab[width - 1];
    } else {
      return new BitWidth(width);
    }
  }

  private static void ensurePrefab() {
    if (prefab == null) {
      prefab = new BitWidth[Math.min(64, Value.MAX_WIDTH)];
      prefab[0] = ONE;
      for (int i = 1; i < prefab.length; i++) {
        prefab[i] = new BitWidth(i + 1);
      }
    }
  }

  public static BitWidth parse(String str) {
    if (str == null || str.length() == 0) {
      throw new NumberFormatException("Width string cannot be null");
    }
    if (str.charAt(0) == '/') str = str.substring(1);
    return create(Integer.parseInt(str));
  }

  public static final BitWidth UNKNOWN = new BitWidth(0);

  public static final BitWidth ONE = new BitWidth(1);

  public static final int MAXWIDTH = 64;
  public static final int MINWIDTH = 1;

  private static BitWidth[] prefab = null;

  final int width;

  private BitWidth(int width) {
    this.width = width;
  }

  @Override
  public int compareTo(BitWidth other) {
    return this.width - other.width;
  }

  @Override
  public boolean equals(Object otherObj) {
    return (otherObj instanceof BitWidth other)
           ? this.width == other.width
           : false;
  }

  public long getMask() {
    if (width == 0) return 0;
    else if (width == MAXWIDTH) return -1L;
    else return (1L << width) - 1;
  }

  public int getWidth() {
    return width;
  }

  @Override
  public int hashCode() {
    return width;
  }

  @Override
  public String toString() {
    return "" + width;
  }
}
