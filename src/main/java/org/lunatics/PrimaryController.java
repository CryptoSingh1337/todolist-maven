package org.lunatics;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.collections.ObservableList;
import org.lunatics.datamodel.*;
import org.lunatics.additem.AddItemController;
import org.lunatics.edititem.EditItemController;
import javafx.application.Platform;
import javafx.collections.transformation.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.net.URL;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.stream.*;

public class PrimaryController {

    @FXML
    private ListView<TodoItem> todoListView;

    @FXML
    private TextArea itemDetailTextArea;

    @FXML
    private Label deadlineLabel;

    @FXML
    private BorderPane mainBorderPane;

    private ContextMenu listContextMenu;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editButton;

    @FXML
    private ToggleButton filterToggleButton;

    @FXML
    private ComboBox<String> fontList;

    @FXML
    private ComboBox<Integer> fontSizeList;

    private FilteredList<TodoItem> filteredList;
    private Predicate<TodoItem> wantAllItems;
    private Predicate<TodoItem> wantTodayItems;
    private StringBuilder s;

    public void initialize() {

        setupContextMenu();
        setupEachButton();
        setupFullView();
        setupPredicates();
        setupFonts();

        itemDetailTextArea.setStyle("-fx-background-color: #ACACAC");

        filteredList = new FilteredList<>(TodoData.getInstance().getTodoItems(), wantAllItems);

        // Sorting TodoItems based on Deadline
        SortedList<TodoItem> sortedList = new SortedList<>(filteredList, Comparator.comparing(TodoItem::getDeadline));

        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();
        

        // Defining each cell of the List View
        todoListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            this.setText(null);
                        } else {
                            this.setText(item.getShortDescription());
                            if (item.getDeadline().isBefore(LocalDate.now().plusDays(1))) {
                                this.setTextFill(Color.RED);
                            } else if (item.getDeadline().equals(LocalDate.now().plusDays(1))) {
                                this.setTextFill(Color.BROWN);
                            } else {
                                this.setTextFill(Color.BLACK);
                            }
                            this.getStyleClass().add("bold");
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );
                return cell;
            }

        });
        fontSizeList.getSelectionModel().select(2);
        s = new StringBuilder();
    }

    private void setupContextMenu() {
        // Creating context Menu
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        MenuItem editMenuItem = new MenuItem("Edit");

        // Defining event handler on each context menu item
        deleteMenuItem.setOnAction(actionEvent -> {
            TodoItem item = todoListView.getSelectionModel().getSelectedItem();
            deleteItem(item);
        });

        editMenuItem.setOnAction(actionEvent -> {
            TodoItem item = todoListView.getSelectionModel().getSelectedItem();
            showEditItemDialog(item);
        });

        // Adding each menu item to the context menu
        listContextMenu.getItems().addAll(editMenuItem, deleteMenuItem);
    }

    private void setupEachButton() {
        // Defining event handler on each button
        deleteButton.setOnAction(actionEvent -> {
            TodoItem item = todoListView.getSelectionModel().getSelectedItem();
            if (item == null) {
                showError("Error", "No TodoItem is selected.");
            } else {
                deleteItem(item);
            }
        });
        editButton.setOnAction(actionEvent -> {
            TodoItem item = todoListView.getSelectionModel().getSelectedItem();
            if (item == null) {
                showError("Error", "No TodoItem is selected.");
            } else {
                showEditItemDialog(item);
            }
        });
    }

    /**
     * This method will setup full view of the TodoItem which is selected in the text area.
     */
    private void setupFullView() {
        todoListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                itemDetailTextArea.setText(item.getDetails());
                DateTimeFormatter df = DateTimeFormatter.ofPattern("dd MMMM, yyyy");
                deadlineLabel.setText("Due: " + df.format(item.getDeadline()));
            }
        });
    }

    private void setupPredicates() {
        wantAllItems = todoItem -> true;
        wantTodayItems = item -> item.getDeadline().equals(LocalDate.now());
    }

    /**
     * Setup Fonts and size
     **/
    private void setupFonts() {
        List<Integer> range = IntStream.range(12, 30).boxed().collect(Collectors.toList());
        fontSizeList.getItems().addAll(range);
        fontSizeList.getSelectionModel().select(0);

        fontList.getItems().addAll(Font.getFontNames());
        fontList.getSelectionModel().select(0);
        fontList.setCellFactory((ListView<String> listView) -> new ListCell<String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    this.setText(item);
                    this.setFont(new Font(item, 12));
                }
            }
        });
    }

    @FXML
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("org/lunatics/icon/add.png"));
        dialog.setTitle("Add New Todo Item");
        dialog.setHeaderText("Use this Dialog to add new Todo Item");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("addItem.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            AddItemController controller = fxmlLoader.getController();
            TodoItem newItem = controller.processResult();
            if (newItem == null) {
                showError("Empty fields", "Short Description and Description should not be empty.");
            } else {
                todoListView.getSelectionModel().select(newItem);
            }
        }
    }

    @FXML
    public void showEditItemDialog(TodoItem oldItem) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("org/lunatics/icon/edit.png"));
        dialog.setTitle("Edit Todo Item");
        dialog.setHeaderText("Use this Dialog to edit Todo Item");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("editItem.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        EditItemController controller = fxmlLoader.getController();
        controller.setFields(oldItem);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TodoItem updatedItem = controller.getUpdateTodoItem();
            controller.updateItem(oldItem, updatedItem);
        }
    }

    public void deleteItem(TodoItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("org/lunatics/icon/confirm.png"));
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm, or cancel to Back out.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TodoData.getInstance().deleteTodoItem(item);
        }
    }

    public void showError(String error, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("org/lunatics/icon/error.png"));
        alert.setHeaderText(error);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }

    @FXML
    public void gettingStarted() {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URL("https://github.com/CryptoSingh1337").toURI());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void about() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("About");
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("org/lunatics/icon/team.png"));
        alert.setHeaderText("Developers: ");
        alert.setContentText("Saransh, Shivang, Mukund");
        alert.getButtonTypes().removeAll(ButtonType.OK, ButtonType.CANCEL);
        alert.getButtonTypes().add(ButtonType.CLOSE);
        alert.show();
    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                deleteItem(selectedItem);
            }
        }
    }

    @FXML
    public void handleFilterButton() {
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if (filterToggleButton.isSelected()) {
            filteredList.setPredicate(wantTodayItems);
            if (filteredList.isEmpty()) {
                itemDetailTextArea.clear();
                deadlineLabel.setText("");
            } else if (filteredList.contains(selectedItem)) {
                todoListView.getSelectionModel().select(selectedItem);
            } else {
                todoListView.getSelectionModel().selectFirst();
            }
        } else {
            filteredList.setPredicate(wantAllItems);
            todoListView.getSelectionModel().select(selectedItem);
            todoListView.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void handleFontChange() {
        s.setLength(0);
        s.append("-fx-font-family: ").append("\"").append(fontList.getValue()).append("\";")
                .append("-fx-font-size: ").append(fontSizeList.getValue()).append(";")
                .append("-fx-background-color: ").append("#000000");
        itemDetailTextArea.setStyle(s.toString());
    }
}
