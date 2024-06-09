package com.davidhavel;

import static com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.download.Download;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.navigation.event.FrameLoadFinished;
import com.teamdev.jxbrowser.view.javafx.BrowserView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Main extends Application {

    private static final String SEARXNG_URL = "https://search.next-web-studio.tech/";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        EngineOptions options = EngineOptions.newBuilder(HARDWARE_ACCELERATED)
                .licenseKey("OK6AEKNYF1OQ677UTUZ5Y7RD57O3MDI3AK1A92OJM7VPWHG24UZ18ZP9M7I3XT4BP0WBYZTETW0VYCE390TKB1IOYWUO9PBF97QQM0UY2N94Y5WWMMARMR0K6214XBA5YQ98F48KLC9AV9AUF")
                .build();
        Engine engine = Engine.newInstance(options);

        TabPane tabPane = new TabPane();
        Map<Tab, Browser> tabBrowserMap = new HashMap<>();

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        Button newTabButton = new Button("New Tab");
        newTabButton.setOnAction(e -> {
            createNewTab(tabPane, tabBrowserMap, engine);
        });

        HBox buttonBar = new HBox(newTabButton);
        buttonBar.getStyleClass().add("button-bar");
        root.setTop(buttonBar);

        createNewTab(tabPane, tabBrowserMap, engine);

        Scene scene = new Scene(root, 1280, 800);

        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setTitle("JxBrowser JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> engine.close());

        MenuItem newTabMenuItem = new MenuItem("New Tab");
        newTabMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
        newTabMenuItem.setOnAction(e -> {
            createNewTab(tabPane, tabBrowserMap, engine);
        });

        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");
        Menu fileMenu = new Menu("File");
        fileMenu.getStyleClass().add("file-menu");
        fileMenu.getItems().add(newTabMenuItem);
        menuBar.getMenus().add(fileMenu);

        root.setTop(menuBar);

        scene.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN).match(e)) {
                createNewTab(tabPane, tabBrowserMap, engine);
            }
            if (new KeyCodeCombination(KeyCode.W, KeyCombination.META_DOWN).match(e)) {
                Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                if (currentTab != null) {
                    Browser browser = tabBrowserMap.get(currentTab);
                    if (browser != null) {
                        browser.close();
                        tabBrowserMap.remove(currentTab);
                    }
                    tabPane.getTabs().remove(currentTab);
                }
            }
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN, KeyCombination.META_DOWN).match(e)) {
                Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                if (currentTab != null) {
                    Browser browser = tabBrowserMap.get(currentTab);
                    if (browser != null) {
                        String url = browser.url();
                        StringSelection stringSelection = new StringSelection(url);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                    }
                }
            }
        });
    }

    private void createNewTab(TabPane tabPane, Map<Tab, Browser> tabBrowserMap, Engine engine) {
        Browser browser = engine.newBrowser();
        BrowserView view = BrowserView.newInstance(browser);

        browser.navigation().loadUrl(SEARXNG_URL);

        Tab tab = new Tab("New Tab");
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        tabBrowserMap.put(tab, browser);


        Button backButton = new Button("Zpět");
        backButton.getStyleClass().add("nav-button");
        backButton.setOnAction(e -> {
            if (browser.navigation().canGoBack()) {
                browser.navigation().goBack();
            }
        });

        Button forwardButton = new Button("Dopředu");
        forwardButton.getStyleClass().add("nav-button");
        forwardButton.setOnAction(e -> {
            if (browser.navigation().canGoForward()) {
                browser.navigation().goForward();
            }
        });

        Button refreshButton = new Button("Refreshovat");
        refreshButton.getStyleClass().add("nav-button");
        refreshButton.setOnAction(e -> browser.navigation().reload());

        Button homeButton = new Button("Domů");
        homeButton.getStyleClass().add("nav-button");
        homeButton.setOnAction(e -> browser.navigation().loadUrl(SEARXNG_URL));

        TextField urlBar = new TextField(SEARXNG_URL);
        urlBar.getStyleClass().add("url-bar");
        urlBar.setPrefWidth(800);

        browser.navigation().on(FrameLoadFinished.class, event -> Platform.runLater(() -> {
            urlBar.setText(browser.url());
            tab.setText(browser.title());
        }));

        urlBar.setOnAction(e -> {
            String input = urlBar.getText();
            try {
                new URL(input);
                browser.navigation().loadUrl(input);
            } catch (MalformedURLException ex) {
                String searchUrl = SEARXNG_URL + "search?q=" + URLEncoder.encode(input, StandardCharsets.UTF_8);
                browser.navigation().loadUrl(searchUrl);
            }
        });

        Button goButton = new Button("Hledej!");
        goButton.getStyleClass().add("nav-button");
        goButton.setOnAction(e -> {
            String input = urlBar.getText();
            try {
                new URL(input);

                browser.navigation().loadUrl(input);
            } catch (MalformedURLException ex) {

                String searchUrl = SEARXNG_URL + "search?q=" + URLEncoder.encode(input, StandardCharsets.UTF_8);
                browser.navigation().loadUrl(searchUrl);
            }
        });


        HBox buttonBar = new HBox(backButton, forwardButton, refreshButton, homeButton, urlBar, goButton);
        buttonBar.getStyleClass().add("button-bar");

        BorderPane tabContent = new BorderPane();
        tabContent.setTop(buttonBar);
        tabContent.setCenter(view);

        tab.setContent(tabContent);

        tab.setOnClosed(e -> {
            browser.close();
            tabBrowserMap.remove(tab);
        });
    }
}
