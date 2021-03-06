package org.lunatics.edititem;

import org.lunatics.datamodel.TodoData;
import org.lunatics.datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class EditItemController {

    @FXML
    private TextField shortDescriptionField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private DatePicker deadlinePicker;

    public void setFields(TodoItem item) {
        shortDescriptionField.setText(item.getShortDescription());
        descriptionArea.setText(item.getDetails());
        deadlinePicker.setValue(item.getDeadline());
    }

    public TodoItem getUpdateTodoItem() {
        String shortDescription = shortDescriptionField.getText().trim();
        String details = descriptionArea.getText();
        LocalDate deadline = deadlinePicker.getValue();
        return new TodoItem(shortDescription, details, deadline);
    }

    public void updateItem(TodoItem oldItem, TodoItem updatedItem) {
        TodoData.getInstance().updateTodoItem(oldItem, updatedItem);
    }
}
