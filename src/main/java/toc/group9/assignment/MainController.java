package toc.group9.assignment;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainController {

    // Home Tab
    @FXML
    TableView<Member> memberTable;
    @FXML
    TableColumn<Member, String> roleColumn;
    @FXML
    TableColumn<Member, String> idColumn;
    @FXML
    TableColumn<Member, String> nameColumn;
    @FXML
    TableColumn<Member, String> participantColumn;
    @FXML
    TableColumn<Member, String> taskColumn;

    // Automata Tab
    @FXML
    TextArea inputArea;
    @FXML
    TextArea outputArea1;
    @FXML
    TextArea outputArea2;
    @FXML
    Button checkButton;

    public void initialize() {
        addMembers();
    }

    @FXML // clear button
    private void clear() {
        inputArea.clear();
        outputArea1.clear();
        outputArea2.clear(); // TODO add any clear function as needed!!
    }

    // TODO NFA
    @FXML
    private void f1() {
        String string = "You have pressed f1 button.\n" +
                        "You wrote: " + inputArea.getText();
        outputArea1.setText(string);
        outputArea1.setEditable(false);
        checkButton.setDisable(true);

    }

    // TODO NFA w/o epsilon
    @FXML
    private void f2() {
        outputArea1.setText("you have pressed f2 button"); // for testing only
        outputArea1.setEditable(false);
        checkButton.setDisable(true);

    }

    // TODO DFA
    @FXML
    private void f3() {
        outputArea1.setText("you have pressed f3 button"); // for testing only
        outputArea1.setEditable(false);
        checkButton.setDisable(true);

    }

    @FXML
    private void f4() {
        outputArea1.setText("you have pressed f4 button, this TextArea is now editable"); // for testing only
        outputArea1.setEditable(true);
        checkButton.setDisable(false);
    }

    // TODO Test
    @FXML
    private void check() {
        outputArea2.setText("OK\nNO\nOK\n"); // for testing only
    }

    // adding members into memberTable
    private void addMembers() {
        ArrayList<Member> members = new ArrayList<>();
        members.add(new Member("leader", "Islam Tariqul", "1211300026", "?%", "F2"));
        members.add(new Member("member", "Adriana Batrisyia binti Hasnan", "1191102379", "?%", "F4"));
        members.add(new Member("member", "Hui Yen Ling", "1211307537", "?%", "F3"));
        members.add(new Member("member", "Vivian Wee Gek Ting", "1211306086", "?%", "F1"));
        
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        participantColumn.setCellValueFactory(new PropertyValueFactory<>("participant"));
        taskColumn.setCellValueFactory(new PropertyValueFactory<>("task"));

        memberTable.getItems().addAll(members);
    }

}
