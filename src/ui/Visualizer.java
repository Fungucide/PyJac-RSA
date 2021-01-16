package ui;

import RSA.Util;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Visualizer extends Application {

	private SplitPane splitPane;
	private TabPane tabPane;
	private Tab isPrimeTab;
	private Tab keyGenerationTab;
	private Tab encryptionTab;
	private BorderPane borderPane;
	private Scene scene;
	private StepTreeView globalLog;

	static void start(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		splitPane = new SplitPane();
		tabPane = new TabPane();
		setupTabs();
		globalLog = Util.globalLog;
		globalLog.setIsActive(true);
		borderPane = new BorderPane(globalLog);

		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.getItems().addAll(tabPane, borderPane);

		splitPane.setDividerPosition(0, 0.75);
		scene = new Scene(splitPane, 1080, 720);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void setupTabs() {
		isPrimeTab = new Tab("Primality Check", new PrimalityPane());
		tabPane.getTabs().add(isPrimeTab);
		keyGenerationTab = new Tab("Key Generation", new KeyGenerationPane());
		tabPane.getTabs().add(keyGenerationTab);
		encryptionTab = new Tab("Encryption", new EncryptionPane());
		tabPane.getTabs().add(encryptionTab);

		tabPane.tabClosingPolicyProperty().set(TabClosingPolicy.UNAVAILABLE);
	}

}
