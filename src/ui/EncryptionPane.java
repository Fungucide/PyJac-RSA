package ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;

import RSA.OAEP;
import RSA.RSA;
import RSA.Util;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Popup;

public class EncryptionPane extends SplitPane {

	private InnerPane plainText;
	private InnerPane cypherText;

	public EncryptionPane() {
		super();
		plainText = new InnerPane("Public Exponent", true);
		cypherText = new InnerPane("Private Exponent", false);
		plainText.setOutputPane(cypherText);
		cypherText.setOutputPane(plainText);
		getItems().addAll(plainText, cypherText);
	}
}

class InnerPane extends GridPane {

	private InnerPane output;

	private Label keyLabel;
	private TextField keyField;
	private Label exponentLabel;
	private TextField exponentField;

	private Label asciiLabel;
	private TextArea asciiText;
	private Label hexLabel;
	private TextArea hexText;

	private CheckBox pad;
	private Button action;

	public InnerPane(String exponentText, boolean encrypt) {
		super();
		setupGrid();

		// Setup the input areas
		keyLabel = new Label("Public Key:");
		keyField = new TextField();
		exponentLabel = new Label(exponentText);
		exponentField = new TextField();
		add(keyLabel, 0, 0);
		add(keyField, 0, 1);
		add(exponentLabel, 1, 0);
		add(exponentField, 1, 1);

		// Setup the text areas
		asciiLabel = new Label("Text:");
		asciiText = new TextArea();
		hexLabel = new Label("Hex:");
		hexText = new TextArea();
		asciiText.setWrapText(true);
		hexText.setWrapText(true);
		hexText.textProperty().addListener((observable, oldValue, newValue) -> {
			if (hexText.isFocused()) {
				asciiText.setText(new String(Util.toAscii(newValue)));
			}
		});
		asciiText.textProperty().addListener((observable, oldValue, newValue) -> {
			if (asciiText.isFocused()) {
				hexText.setText(Util.toHex(newValue.getBytes()));
			}
		});

		add(asciiLabel, 0, 2);
		add(asciiText, 0, 3, 2, 1);
		add(hexLabel, 0, 4);
		add(hexText, 0, 5, 2, 1);

		pad = new CheckBox("Pad (OAEP SHA-1)");
		action = new Button(encrypt ? "Encrypt" : "Decrypt");
		action.setOnAction(action -> {
			try {
				BigInteger publicKey = new BigInteger(keyField.getText());
				BigInteger exponent = new BigInteger(exponentField.getText());
				byte[] res;
				byte[] text = Util.toAscii(hexText.getText());

				if (encrypt) {
					if (pad.isSelected()) {
						text = OAEP.pad(text, publicKey.bitLength());
					}
					res = RSA.encrypt(text, publicKey, exponent);
				} else {
					res = RSA.decrypt(text, publicKey, exponent);
					if (pad.isSelected()) {
						res = OAEP.unpad(res, publicKey.bitLength());
					}
				}
				output.setText(res);
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Encryption / Decryption Error");
				alert.setHeaderText(e.getMessage());
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				TextArea ta = new TextArea(sw.toString());
				ta.setEditable(false);
				alert.getDialogPane().setExpandableContent(ta);
				alert.showAndWait();
			}
		});
		add(pad, 0, 6);
		add(action, 0, 7);
	}

	public void setOutputPane(InnerPane output) {
		this.output = output;
	}

	public void setText(byte[] data) {
		hexText.setText(Util.toHex(data));
		asciiText.setText(new String(data));
	}

	private void setupGrid() {
		// Set padding and margins
		setHgap(5);
		setVgap(5);
		setPadding(new Insets(10, 10, 10, 10));

		// Set the size of the columns
		ColumnConstraints cc1 = new ColumnConstraints();
		ColumnConstraints cc2 = new ColumnConstraints();
		cc1.setPercentWidth(50);
		cc2.setPercentWidth(50);
		getColumnConstraints().addAll(cc1, cc2);
	}
}