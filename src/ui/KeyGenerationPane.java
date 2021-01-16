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

public class KeyGenerationPane extends VBox {

	private Label keySizeLabel;
	private TextField keySizeField;
	private String[] fields = new String[] { "p", "q", "n", "m", "e", "d" };
	private Label[] labels;
	private TextArea[] textAreas;
	private Button generate;

	public KeyGenerationPane() {
		super();

		keySizeLabel = new Label("Key size (bit size of p and q):");
		keySizeField = new TextField("512");
		keySizeField.textProperty().addListener((observer, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				keySizeField.setText(newValue.replaceAll("[^\\d]", ""));
			}
		});
		getChildren().addAll(keySizeLabel, keySizeField);

		labels = new Label[fields.length];
		textAreas = new TextArea[fields.length];
		for (int i = 0; i < fields.length; i++) {
			labels[i] = new Label(fields[i] + ":");
			textAreas[i] = new TextArea();
			textAreas[i].setWrapText(true);
			getChildren().addAll(labels[i], textAreas[i]);
		}

		textAreas[4].setText("65537");

		generate = new Button("Generate Key");
		generate.setOnAction(action -> {
			try {
				BigInteger p, q, n, m, e, d;
				// If p or q are empty set them.
				if (textAreas[0].getText().isBlank()) {
					p = Util.randomPrime(Integer.parseInt(keySizeField.getText()));
					textAreas[0].setText(p.toString());
				} else {
					p = new BigInteger(textAreas[0].getText());
				}
				if (textAreas[1].getText().isBlank()) {
					q = Util.randomPrime(Integer.parseInt(keySizeField.getText()));
					textAreas[1].setText(q.toString());
				} else {
					q = new BigInteger(textAreas[1].getText());
				}
				n = p.multiply(q);
				m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
				if (textAreas[4].getText().isBlank()) {
					e = Util.randomBigInteger(16);
					e = e.divide(e.gcd(m));
					textAreas[4].setText(e.toString());
				} else {
					e = new BigInteger(textAreas[4].getText());
				}
				d = e.modInverse(m);

				textAreas[2].setText(n.toString());
				textAreas[3].setText(m.toString());
				textAreas[5].setText(d.toString());
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Key Generation Error");
				alert.setHeaderText(e.getMessage());
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				TextArea ta = new TextArea(sw.toString());
				ta.setEditable(false);
				alert.getDialogPane().setExpandableContent(ta);
				alert.showAndWait();
			}
		});
		getChildren().add(generate);
	}
}
