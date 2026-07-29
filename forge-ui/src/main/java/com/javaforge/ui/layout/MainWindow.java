package com.javaforge.ui.layout;

import com.javaforge.ui.theme.ThemeManager;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainWindow extends BorderPane {

    private final ThemeManager themeManager;
    private final MenuBarBuilder menuBar;
    private final StatusBar statusBar;
    private final ExplorerPanel explorer;
    private final EditorPanel editor;
    private final BottomPanel bottomPanel;

    public MainWindow(ThemeManager themeManager) {
        this.themeManager = themeManager;
        this.menuBar = new MenuBarBuilder(themeManager);
        this.statusBar = new StatusBar();
        this.explorer = new ExplorerPanel();
        this.editor = new EditorPanel();
        this.bottomPanel = new BottomPanel();

        buildLayout();
    }

    private void buildLayout() {
        setTop(menuBar);

        var sideSplit = new SplitPane();
        sideSplit.setOrientation(Orientation.HORIZONTAL);
        sideSplit.getItems().addAll(explorer, editor);
        SplitPane.setResizableWithParent(explorer, Boolean.FALSE);
        explorer.setPrefWidth(260);

        var mainSplit = new SplitPane();
        mainSplit.setOrientation(Orientation.VERTICAL);
        mainSplit.getItems().addAll(sideSplit, bottomPanel);
        mainSplit.setDividerPositions(0.72);

        setCenter(mainSplit);
        setBottom(statusBar);
    }

    public EditorPanel getEditor() {
        return editor;
    }

    public ExplorerPanel getExplorer() {
        return explorer;
    }

    public BottomPanel getBottomPanel() {
        return bottomPanel;
    }
}
