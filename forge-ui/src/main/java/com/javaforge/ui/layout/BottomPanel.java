package com.javaforge.ui.layout;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class BottomPanel extends BorderPane {

    private final TabPane tabPane = new TabPane();
    private final TerminalPanel terminal;
    private final ConsolePanel console;

    public BottomPanel() {
        getStyleClass().add("bottom-panel");
        this.terminal = new TerminalPanel();
        this.console = new ConsolePanel();

        var terminalTab = new Tab("TERMINAL", terminal);
        terminalTab.setClosable(false);
        var consoleTab = new Tab("CONSOLE", console);
        consoleTab.setClosable(false);
        var problemsTab = new Tab("PROBLEMS");
        problemsTab.setClosable(false);
        var outputTab = new Tab("OUTPUT");
        outputTab.setClosable(false);

        tabPane.getTabs().addAll(problemsTab, consoleTab, outputTab, terminalTab);
        tabPane.getSelectionModel().select(terminalTab);

        setCenter(tabPane);
    }

    public TerminalPanel getTerminal() {
        return terminal;
    }

    public ConsolePanel getConsole() {
        return console;
    }
}
