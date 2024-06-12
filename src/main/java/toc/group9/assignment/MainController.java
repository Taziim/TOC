package toc.group9.assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class MainController {

    // Home Tab
    @FXML
    TableView<Member> memberTable;
    @FXML
    TableColumn<Member, String> roleColumn, idColumn, nameColumn, participantColumn, taskColumn;

    // Automata Tab
    @FXML
    TextArea inputArea, outputArea1, outputArea2;
    @FXML
    Button checkButton;

    // TODO Help Tab

    public void initialize() {
        addMembers();

        Font font = Font.font("Monospaced", FontWeight.NORMAL, FontPosture.REGULAR, 15);
        outputArea2.setFont(font);
    }

    @FXML // TODO import button
    private void importRG() {

    }

    @FXML // clear button
    private void clear() {
        inputArea.clear();
        outputArea1.clear();
        outputArea2.clear();
    }

    @FXML // F1: RG to NFA
    private void f1() {
        isF4(false);

        ArrayList<String> states = new ArrayList<>();;
        ArrayList<String> alphabet = new ArrayList<>();;
        String startState = null;
        ArrayList<String> acceptStates = new ArrayList<>();;
        HashMap<String, HashMap<String, ArrayList<String>>> transitions = new HashMap<>();; // key: state(nonterminal), value: [key: alphabet(terminal), value: nextStates(nonterminal)]
        boolean hasEpsilon = false, isDigit = false, hasCheckType = false;

        // get whole input
        String input = inputArea.getText().trim();

        if (input.isBlank()) {
            outputArea1.setText("Please enter a valid regular grammar.");
        }

        // RG to NFA logic
        try {
            // parse RG (split to LHS & RHS)
            String[] rules = input.split("\\n");
            for (String rule : rules) {

                String[] parts = rule.split("->");

                // ensure theres only LHS & RHS
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid grammar: Each rule must contain a single arrow(->).");
                }

                // add LHS to states(variables/nonterminals)
                String left = parts[0].trim();
                if (left.isEmpty() || !Character.isUpperCase(left.charAt(0))) {
                    throw new IllegalArgumentException("Invalid grammar: left-hand side must be a variable");
                }

                if (!states.contains(left))
                    states.add(left);

                // set first LHS as start state
                if (startState == null)
                    startState = left;

                // parse RHS (get terminals, nonterminals & transtions)
                String[] rights = parts[1].trim().split("\\|");
                for (String right : rights) {

                    right = right.trim();
                    if (right.isEmpty()) 
                        throw new IllegalArgumentException("Invalid grammar: right-hand side is missing");
                    

                    // ensure RG is right regular
                    if (!right.matches("ε|[a-z0-9]?[A-Z]?")) 
                        throw new IllegalArgumentException("Invalid grammar: each rule should be right regular.");
                    

                    // add state as accept state if theres ε
                    if (right.equals("ε")) { // removed as it will affect transition table: || right.matches("[a-z0-9]")
                        if (!acceptStates.contains(left)) acceptStates.add(left);
                        hasEpsilon = true;

                    } else {
                        String terminal = right.length() > 1 ? right.substring(0, 1) : "ε";
                        String nonterminal = right.length() > 1 ? right.substring(1) : right;

                        // add alphabet(terminal)
                        if (!terminal.equals("ε") && !alphabet.contains(terminal)) {
                            if (!hasCheckType) {
                                isDigit = Character.isDigit(terminal.charAt(0));
                                hasCheckType = true;
                            } else if (isDigit != Character.isDigit(terminal.charAt(0))) {
                                throw new IllegalArgumentException("Invalid grammar: terminal symbols should be consistent (all digits or all letters)");
                            }
                            alphabet.add(terminal);
                        }

                        /*
                         * add state(nonterminal) - removed as it may messed up states order (thus only
                         * depend on LHS)
                         * if (!nonterminal.isEmpty() && !states.contains(nonterminal))
                         * states.add(nonterminal);
                         */

                        // add transitions (alphabet & nextState)
                        transitions.putIfAbsent(left, new HashMap<>());
                        transitions.get(left).putIfAbsent(terminal, new ArrayList<>());
                        transitions.get(left).get(terminal).add(nonterminal);
                    }
                }
            }

            Collections.sort(alphabet); // sort alphabet a-z or 0-9

            // NFA formal definition
            StringBuilder nfaDesc = new StringBuilder();
            nfaDesc.append("M = (Q, Σ, δ, p0, F)\n");
            nfaDesc.append("Q = { ").append(String.join(", ", states)).append(" }\n");
            nfaDesc.append("Σ = { ").append(String.join(", ", alphabet)).append(" }\n");
            nfaDesc.append("δ: Q x Σε -> Pow(Q)\n");
            nfaDesc.append("po = ").append(startState).append("\n");
            nfaDesc.append("F = { ").append(String.join(", ", acceptStates)).append(" }\n");
            outputArea1.setText(nfaDesc.toString());

            // NFA transition table
            int columnWidth = 10;
            for(String state : states) {
                for(String a: alphabet) {
                    if(transitions.containsKey(state) && transitions.get(state).containsKey(a)) {
                        int length = String.join(",", transitions.get(state).get(a)).length() + 4;
                        columnWidth = length > columnWidth ? length : columnWidth;
                    }
                }
            }
            
            StringBuilder nfaTable = new StringBuilder();
            if (hasEpsilon)
                alphabet.add("ε");
            nfaTable.append("δNFA | ").append(String.join("  |  ", alphabet));
            nfaTable.append("  |\n");

            for (String state : states) {
                if (state.equals(startState)) {
                    nfaTable.append(" ->");
                } else if (acceptStates.contains(state)) {
                    nfaTable.append("  *");
                } else {
                    nfaTable.append("   ");
                }
                nfaTable.append(state).append(" |");

                for (String a : alphabet) {
                    if(transitions.containsKey(state) && transitions.get(state).containsKey(a)) {
                        nfaTable.append(" {");
                        nfaTable.append(String.join(", ", transitions.get(state).get(a)));
                        nfaTable.append("} |");
                    } else {
                        nfaTable.append("  ∅  |");
                    }
                }

                nfaTable.append("\n");

            }

            outputArea2.setText(nfaTable.toString());

        } catch (IllegalArgumentException e) {
            outputArea1.setText(e.getMessage());
        }
    }

    @FXML // F2: ε-NFA to NFA without ε
    private void f2() {
        isF4(false);

        outputArea1.setText("you have pressed f2 button"); // for testing only pls remove

    }

    @FXML // F3: NFA to DFA
    private void f3() {
        isF4(false);

        outputArea1.setText("you have pressed f3 button"); // for testing only pls remove

    }

    @FXML
    private void f4() {
        isF4(true);
        outputArea1.setText("you have pressed f4 button, this TextArea is now editable"); // for testing only pls remove
    }

    @FXML // F4: test String
    private void check() {
        outputArea2.setText("OK\nNO\nOK\n"); // for testing only pls remove
    }

    // need user input when testing string so outputArea1 is editable
    private void isF4(boolean b) {
        outputArea1.setEditable(b ? true : false);
        checkButton.setDisable(b ? false : true);
    }

    // adding members data into memberTable
    private void addMembers() {
        ArrayList<Member> members = new ArrayList<>();
        members.add(new Member("leader", "Islam Tariqul", "1211300026", "25%", "F2"));
        members.add(new Member("member", "Adriana Batrisyia binti Hasnan", "1191102379", "25%", "F3"));
        members.add(new Member("member", "Hui Yen Ling", "1211307537", "25%", "F4"));
        members.add(new Member("member", "Vivian Wee Gek Ting", "1211306086", "25%", "F1"));
        
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        participantColumn.setCellValueFactory(new PropertyValueFactory<>("participant"));
        taskColumn.setCellValueFactory(new PropertyValueFactory<>("task"));

        memberTable.getItems().addAll(members);
    }
}
