package ui;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class StepTreeView extends TreeView<String> {

	private String rootMessage;
	private boolean active;
	private TreeItem<String> currentItem;

	public StepTreeView(String rootMessage) {
		super();
		this.rootMessage = rootMessage;
		active = false;
		reset();
	}

	public void stepIn(String message) {
		if (active) {
			TreeItem<String> item = new TreeItem<String>(message);
			currentItem.getChildren().add(item);
			currentItem = item;
		}
	}

	public void log(String message) {
		if (active) {
			currentItem.getChildren().add(new TreeItem<String>(message));
		}
	}

	public void stepOut() {
		if (active) {
			currentItem = currentItem.getParent();
		}
	}

	public void appendToCurrent(String message) {
		if (active) {
			currentItem.setValue(currentItem.getValue() + message);
		}
	}

	public void setIsActive(boolean active) {
		this.active = active;
	}

	public void reset() {
		reset(rootMessage);
	}

	public void reset(String message) {
		currentItem = new TreeItem<String>(message);
		currentItem.setExpanded(true);
		setRoot(currentItem);
	}
}
