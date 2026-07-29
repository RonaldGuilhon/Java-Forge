package com.javaforge.ui.layout;

import com.javaforge.ai.chat.AIChatPanel;
import com.javaforge.database.explorer.DatabaseExplorerPanel;
import com.javaforge.git.GitPanel;
import com.javaforge.marketplace.ui.MarketplacePanel;
import com.javaforge.server.ui.ServerPanel;
import com.javaforge.ui.theme.ThemeManager;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class MainWindow extends BorderPane {

    private final ThemeManager themeManager;
    private final MenuBarBuilder menuBar;
    private final StatusBar statusBar;
    private final ExplorerPanel explorer;
    private final EditorPanel editor;
    private final BottomPanel bottomPanel;
    private final AIChatPanel aiChatPanel;
    private final DatabaseExplorerPanel databasePanel;
    private final GitPanel gitPanel;
    private final ServerPanel serverPanel;
    private final MarketplacePanel marketplacePanel;

    private SplitPane mainSplit;
    private SplitPane sideSplit;
    private boolean aiPanelVisible = false;

    public MainWindow(ThemeManager themeManager) {
        this.themeManager = themeManager;
        this.aiChatPanel = new AIChatPanel();
        this.databasePanel = new DatabaseExplorerPanel();
        this.gitPanel = new GitPanel();
        this.serverPanel = new ServerPanel();
        this.marketplacePanel = new MarketplacePanel();
        this.menuBar = new MenuBarBuilder(themeManager, this);
        this.statusBar = new StatusBar();
        this.explorer = new ExplorerPanel();
        this.editor = new EditorPanel();
        this.bottomPanel = new BottomPanel();

        themeManager.addListener(sheets -> {
            boolean dark = themeManager.getCurrentTheme().isDark();
            bottomPanel.getTerminal().setDarkMode(dark);
            aiChatPanel.setDarkMode(dark);
        });

        buildLayout();
    }

    private void buildLayout() {
        setTop(menuBar);

        sideSplit = new SplitPane();
        sideSplit.setOrientation(Orientation.HORIZONTAL);
        sideSplit.getItems().addAll(explorer, editor);
        SplitPane.setResizableWithParent(explorer, Boolean.FALSE);
        explorer.setPrefWidth(260);

        mainSplit = new SplitPane();
        mainSplit.setOrientation(Orientation.VERTICAL);
        mainSplit.getItems().addAll(sideSplit, bottomPanel);
        mainSplit.setDividerPositions(0.72);

        setCenter(mainSplit);
        setBottom(statusBar);
    }

    public void toggleAiPanel() {
        if (aiPanelVisible) {
            sideSplit.getItems().remove(aiChatPanel);
            aiPanelVisible = false;
        } else {
            if (!sideSplit.getItems().contains(aiChatPanel)) {
                sideSplit.getItems().add(aiChatPanel);
            }
            aiChatPanel.setPrefWidth(350);
            aiPanelVisible = true;
        }
    }

    public void openDatabaseExplorer() {
        bottomPanel.openTab("DATABASE", databasePanel);
    }

    public void openGitPanel() {
        bottomPanel.openTab("GIT", gitPanel);
    }

    public void openServerPanel() {
        bottomPanel.openTab("SERVER", serverPanel);
    }

    public void openMarketplace() {
        bottomPanel.openTab("MARKETPLACE", marketplacePanel);
    }

    public AIChatPanel getAiChatPanel() {
        return aiChatPanel;
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
