/**
 * $Source:$
 * $Id:$
 */
package net.redyeti.codereview;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.intellij.ide.util.PropertiesComponent;

/**
 * A combo box that maintains a persistent history
 *
 * @author <a href="mailto:chris_overseas@hotmail.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class ComboBoxWithStoredHistory extends JComboBox {

  private Model model;
  private String propertyName;
  private int historySize = 10;

  public ComboBoxWithStoredHistory() {
    this("defaultComboHistory");
  }

  public ComboBoxWithStoredHistory(String propertyName) {
    this.propertyName = propertyName;
    model = new Model();
    setModel(model);
  }

  public void setText(String text) {
    setSelectedItem(text);
  }

  public String getText() {
    return getSelectedItem() == null ? "" : getSelectedItem().toString();
  }

  public void setHistorySize(int size) {
    historySize = size;
  }

  public void setHistory(List<String> history) {
    model.setItems(history);
  }

  public List<String> getHistory() {
    return model.getItems();
  }

  public int getHistorySize() {
    return historySize;
  }

  public void saveCurrentState() {
    String text = getText();
    model.addElement(text);
    List<String> items = model.getItems();
    StringBuilder builder = new StringBuilder(1000);
    for (String item : items) {
      builder.append('\n').append(item);
    }
    PropertiesComponent.getInstance().setValue(propertyName, builder.toString());
  }

  public void reset() {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    String history = propertiesComponent.getValue(propertyName);
    if (history != null) {
      String[] items = history.split("\n");
      List<String> result = new ArrayList<String>();
      for (String item : items) {
        if (item != null && item.length() > 0) {
          result.add(item);
        }
      }
      setHistory(result);
      if (getItemCount() > 0)
        setSelectedIndex(0);
    }
  }

  public class Model extends AbstractListModel implements MutableComboBoxModel {

    private List<String> items = new ArrayList<String>();
    private Object selectedItem;

    public String getElementAt(int index) {
      return items.get(index);
    }

    public int getSize() {
      return items.size();
    }

    public void addElement(Object item) {
      String newItem = (String) item;
      newItem = newItem.trim();
      if (0 == newItem.length()) {
        return;
      }
      removeElement(newItem);
      insertElementAt(newItem, 0);
      if (getSize() >= getHistorySize()) {
        removeElementAt(getSize() - 1);
      }
      fireContentsChanged();
    }

    public void insertElementAt(Object item, int index) {
      items.add(index, (String) item);
      fireContentsChanged();
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public void removeElement(Object obj) {
      if (items.remove(obj))
        fireContentsChanged();
    }

    public void removeElementAt(int index) {
      items.remove(index);
      fireContentsChanged();
    }

    public void fireContentsChanged() {
      fireContentsChanged(this, -1, -1);
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public boolean contains(Object item) {
      return items.contains(item);
    }

    public void setItems(List<String> items) {
      if (getSize() > getHistorySize())
        this.items = items.subList(0, getHistorySize());
      else
        this.items = items;
      fireContentsChanged();
    }

    public List<String> getItems() {
      return items;
    }

    public void setSelectedItem(Object item) {
      if ((selectedItem != null && !selectedItem.equals(item)) ||
          selectedItem == null && item != null) {
        selectedItem = item;
        fireContentsChanged(this, -1, -1);
      }
    }

    public Object getSelectedItem() {
      return selectedItem;
    }
  }
}
