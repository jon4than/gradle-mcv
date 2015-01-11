package ucar.unidata.idv.ui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ucar.unidata.util.GuiUtils;

// need to check for mem leaks
public class DataTreeSearch {

    private final Object SEARCH_MUTEX = new Object();

    private static final String SEARCH_ICON_PATH = "/auxdata/ui/icons/Search16.gif";

    private static final String CANCEL_ICON_PATH = "/auxdata/ui/icons/cancel.gif";

    private Icon searchIcon = GuiUtils.getImageIcon(SEARCH_ICON_PATH, true);

    private Icon cancelIcon = GuiUtils.getImageIcon(CANCEL_ICON_PATH, true);;

    private DataTree dataTree;

    private JTextField searchField;

    private JButton searchButton;

    private JPanel searchFieldPanel;

    private JPanel searchPanel;

    private boolean showingSearchField = false;

    public DataTreeSearch(DataTree dataTree) {
        this.dataTree = dataTree;
    }

    public JPanel getSearcherComponent() {
        dataTree.getTree().addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if ( !showingSearchField) {
                    if (((e.getKeyCode() == KeyEvent.VK_F)
                        && e.isControlDown()) || (e.getKeyCode()
                        == KeyEvent.VK_SLASH)) {
                        searchButtonPressed();
                    }
                } else {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        searchButtonPressed();
                    }
                }
            }
        });

        searchField = new JTextField("", 7);
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    searchButtonPressed();
                } else {
                    doSearch(e.getKeyCode() != KeyEvent.VK_ENTER);
                }
            }
        });

        searchFieldPanel = new JPanel(new CardLayout());
        searchFieldPanel.add("empty", new JLabel(" "));
        searchFieldPanel.add("field", searchField);

        searchButton = GuiUtils.makeImageButton(SEARCH_ICON_PATH, this, "searchButtonPressed");

        if (searchIcon == null) {
            searchIcon = GuiUtils.getImageIcon(SEARCH_ICON_PATH, true);
        }

        if (cancelIcon == null) {
            cancelIcon = GuiUtils.getImageIcon(CANCEL_ICON_PATH, true);
        }
        searchPanel = GuiUtils.centerRight(searchFieldPanel, searchButton);

        return searchPanel;
    }

    public void searchButtonPressed() {
        CardLayout cardLayout = (CardLayout)searchFieldPanel.getLayout();
        if (!showingSearchField) {
            cardLayout.show(searchFieldPanel, "field");
            searchField.requestFocus();
            searchButton.setIcon(cancelIcon);
        } else {
            searchButton.setIcon(searchIcon);
            cardLayout.show(searchFieldPanel, "empty");
        }
        showingSearchField = !showingSearchField;
    }

    public void doSearch(boolean andClear) {
        synchronized (SEARCH_MUTEX) {
            String s = searchField.getText().trim();
            if (s.isEmpty()) {
                dataTree.clearSearchState();
                searchField.setBackground(Color.WHITE);
                return;
            }

            if (andClear) {
                dataTree.clearSearchState();
            }

            if (dataTree.doSearch(s, searchButton)) {
                searchField.setBackground(Color.WHITE);
            } else {
                dataTree.clearSearchState();
                if (dataTree.doSearch(s, searchButton)) {
                    searchField.setBackground(Color.WHITE);
                } else {
                    searchField.setBackground(DataSelector.COLOR_BADSEARCH);
                }
            }
        }
    }
}