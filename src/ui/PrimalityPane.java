package ui;

import java.math.BigInteger;

import RSA.Util;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
		checkIsPrime = new Button("Check");
		checkIsPrime.setOnAction(action -> {
			try {
				result.setText("");
				BigInteger n = new BigInteger(inputField.getText().trim());
				log.reset("isPrime(" + n.toString() + ")");
				log.setIsActive(true);
				if (Util.isPrime(n)) {
					result.setText("The number is prime");
				} else {
					result.setText("The number is composite");
				}
				log.setIsActive(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		result = new Label();
		log = new StepTreeView("");
		Util.primeLog = log;
		getChildren().addAll(topLabel, inputField, checkIsPrime, log);
	}
}
