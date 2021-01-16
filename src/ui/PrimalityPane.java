package ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;

import RSA.Util;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;

public class PrimalityPane extends VBox {

	private Label topLabel;
	private TextField inputField;
	private Button checkIsPrime;
	private Label result;
	private StepTreeView log;

	public PrimalityPane() {
		super();
		topLabel = new Label("Enter a number and check if it is prime:");
		inputField = new TextField();
		inputField.textProperty().addListener((observer, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				inputField.setText(newValue.replaceAll("[^\\d]", ""));
			}
		});
		checkIsPrime = new Button("Check");
		checkIsPrime.setOnAction(action -> {
			try {
				result.setText("");
				BigInteger n = new BigInteger(inputField.getText().trim());
				log.reset();
				log.setIsActive(true);
				if (Util.isPrime(n)) {
					result.setText("The number is prime");
				} else {
					result.setText("The number is composite");
				}
				log.setIsActive(false);
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Prime Checker Error");
				alert.setHeaderText(e.getMessage());
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				TextArea ta = new TextArea(sw.toString());
				ta.setEditable(false);
				alert.getDialogPane().setExpandableContent(ta);
				alert.showAndWait();
			}
		});
		result = new Label();
		log = Util.primeLog;
		getChildren().addAll(topLabel, inputField, checkIsPrime, log);
	}
}
