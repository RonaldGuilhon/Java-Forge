package com.javaforge.ui.layout;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.util.Optional;

public class BottomPanel extends BorderPane {

    private final TabPane tabPane = new TabPane();
    private final TerminalPanel terminal;
    private final ConsolePanel console;

    public BottomPanel() {
        getStyleClass().add("bottom-panel");
        this.terminal = new TerminalPanel();
        this.console = new ConsolePanel();

        Tab terminalTab = new Tab("TERMINAL", terminal);
        terminalTab.setClosable(false);
        Tab consoleTab = new Tab("CONSOLE", console);
        consoleTab.setClosable(false);
        Tab problemsTab = new Tab("PROBLEMS");
        problemsTab.setClosable(false);
        Tab outputTab = new Tab("OUTPUT");
        outputTab.setClosable(false);

        tabPane.getTabs().addAll(problemsTab, consoleTab, outputTab, terminalTab);
        tabPane.getSelectionModel().select(terminalTab);

        setCenter(tabPane);
    }

    public void openTab(String name, Node content) {
        Optional<Tab> existing = tabPane.getTabs().stream()
            .filter(t -> t.getText().equals(name))
            .findFirst();
        if (existing.isPresent()) {
            tabPane.getSelectionModel().select(existing.get());
        } else {
            Tab tab = new Tab(name, content);
            tab.setClosable(true);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        }
    }

    public TerminalPanel getTerminal() {
        return terminal;
    }

    public ConsolePanel getConsole() {
        return console;
    }
}
