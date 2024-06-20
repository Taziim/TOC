package toc.group9.assignment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

    // Help Tab
    @FXML
    ImageView helpImg;
    @FXML
    Button prevButton, nextButton;
    private ArrayList<Image> images;
    private int index;

    // data
    ArrayList<String> states, alphabet, acceptStates;
    String startState = null;
    boolean hasEpsilon, isDigit, hasCheckType;
    HashMap<String, HashMap<String, ArrayList<String>>> nfaTransitions; // key: state(nonterminal), value: [key: alphabet(terminal), value: nextStates(nonterminal)]
    HashMap<String, HashMap<String, ArrayList<String>>> dfaTransitions;

    public void initialize() {
        addMembers();

        images = new ArrayList<>();
        images.add(new Image(getClass().getResource("manual/help0.png").toExternalForm()));
        images.add(new Image(getClass().getResource("manual/help1.png").toExternalForm()));
        index = 0;
    }

    @FXML // Import RG txt file to inputArea
    private void importRG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select regular grammar text file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));

        Stage stage = (Stage) inputArea.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                Path filePath = file.toPath();
                String importedRG = Files.readString(filePath);

                inputArea.setText(importedRG);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML // clear button
    private void clear() {
        inputArea.clear();
        outputArea1.clear();
        outputArea2.clear();
        initialiseData();
    }

    @FXML // F1: RG to NFA
    private void f1() {
        isF4(false);
        initialiseData();

        try {
            parseRG(inputArea.getText().trim());

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
            ArrayList<String> nfaAlphabet = new ArrayList<>();
            nfaAlphabet.addAll(alphabet);
            if (hasEpsilon)
                nfaAlphabet.add("ε");

            int[] maxWidths = new int[nfaAlphabet.size()];
            Arrays.fill(maxWidths, 0);

            for (String state : states) {
                for (int i = 0; i < nfaAlphabet.size(); i++) {
                    String a = nfaAlphabet.get(i);

                    if (nfaTransitions.containsKey(state) && nfaTransitions.get(state).containsKey(a)) {
                        int width = nfaTransitions.get(state).get(a).toString().length();
                        maxWidths[i] = width > maxWidths[i] ? width : maxWidths[i];
                    }
                }
            }
            
            StringBuilder nfaTable = new StringBuilder();
            nfaTable.append(" δNFA |");
            for (int i = 0; i < nfaAlphabet.size(); i++) {
                String text = maxWidths[i] == 0 ? nfaAlphabet.get(i) + "|" : String.format("%-" + maxWidths[i] + "s|", nfaAlphabet.get(i));
                nfaTable.append(text);
            }
            nfaTable.append("\n");

            for (String state : states) {
                if (state.equals(startState)) {
                    if(acceptStates.contains(state))
                        nfaTable.append(" *->");
                    else
                        nfaTable.append("  ->");
                } else if (acceptStates.contains(state)) {
                    nfaTable.append("   *");
                } else {
                    nfaTable.append("    ");
                }
                nfaTable.append(state).append(" |");

                for (int i = 0; i < nfaAlphabet.size(); i++) {
                    String a = nfaAlphabet.get(i);
                    if (nfaTransitions.containsKey(state) && nfaTransitions.get(state).containsKey(a)) {
                        String nextStates = "{" + String.join(", ", nfaTransitions.get(state).get(a)) + "}";
                        String text = maxWidths[i] == 0 ? nextStates + "|" : String.format("%-" + maxWidths[i] + "s|", nextStates);
                        nfaTable.append(text);
                    } else {
                        String text = maxWidths[i] == 0 ? "{ }|" : String.format("%-" + maxWidths[i] + "s|", "{ }");
                        nfaTable.append(text);
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
        initialiseData();

        try {
            parseRG(inputArea.getText().trim());

            // Compute epsilon-closures for all states
            HashMap<String, Set<String>> epsilonClosure = new HashMap<>();
            for (String state : states) {
                Set<String> nextStateSet = new HashSet<>();
                nextStateSet.add(state); // Start with the state itself
                Set<String> epsClosure = epsilonClosure(nextStateSet, nfaTransitions);
                epsilonClosure.put(state, epsClosure);
            }
    
            // Construct new transitions without epsilon
            HashMap<String, HashMap<String, ArrayList<String>>> newTransitions = new HashMap<>();
            for (String state : states) {
                for (String terminal : alphabet) {
                    Set<String> reachableStates = new HashSet<>();
                    for (String epsState : epsilonClosure.get(state)) {
                        if (nfaTransitions.containsKey(epsState) && nfaTransitions.get(epsState).containsKey(terminal)) {
                            reachableStates.addAll(nfaTransitions.get(epsState).get(terminal));
                        }
                    }
                    if (!reachableStates.isEmpty()) {
                        newTransitions.putIfAbsent(state, new HashMap<>());
                        newTransitions.get(state).put(terminal, new ArrayList<>(reachableStates));
                    }
                }
            }
    
            // Update accept states based on epsilon-closures
            Set<String> newAcceptStates = new HashSet<>(acceptStates);
    
            // NFA formal definition
            StringBuilder nfaDesc = new StringBuilder();
            nfaDesc.append("M = (Q, Σ, δ, p0, F)\n");
            nfaDesc.append("Q = { ").append(String.join(", ", states)).append(" }\n");
            nfaDesc.append("Σ = { ").append(String.join(", ", alphabet)).append(" }\n");
            nfaDesc.append("δ: Q x Σ -> Pow(Q)\n");
            nfaDesc.append("p0 = ").append(startState).append("\n");
            nfaDesc.append("F = { ").append(String.join(", ", newAcceptStates)).append(" }\n");
            outputArea1.setText(nfaDesc.toString());
    
            // NFA transition table
            int[] maxWidths = new int[alphabet.size()];
            Arrays.fill(maxWidths, 0);

            for (String state : states) {
                for (int i = 0; i < alphabet.size(); i++) {
                    String a = alphabet.get(i);
                    if (newTransitions.containsKey(state) && newTransitions.get(state).containsKey(a)) {
                        Set<String> nextStateSet = new HashSet<>();
                        for (String nextState : newTransitions.get(state).get(a)) {
                            nextStateSet.addAll(epsilonClosure.get(nextState));
                        }

                        int width = nextStateSet.toString().length();
                        maxWidths[i] = width > maxWidths[i] ? width : maxWidths[i];
                    }
                }
            }

            StringBuilder nfaTable = new StringBuilder();
            nfaTable.append(" δNFA |");
            for (int i = 0; i < alphabet.size(); i++) {
                String text = maxWidths[i] == 0 ? alphabet.get(i) + "|" : String.format("%-" + maxWidths[i] + "s|", alphabet.get(i));
                nfaTable.append(text);
            }
            nfaTable.append("\n");
    
            for (String state : states) {
                if (state.equals(startState)) {
                    if (newAcceptStates.contains(state))
                        nfaTable.append(" *->");
                    else
                        nfaTable.append("  ->");
                } else if (newAcceptStates.contains(state)) {
                    nfaTable.append("   *");
                } else {
                    nfaTable.append("    ");
                }
                nfaTable.append(state).append(" |");
    
                for (int i = 0; i < alphabet.size(); i++) {
                    String a = alphabet.get(i);
                    if (newTransitions.containsKey(state) && newTransitions.get(state).containsKey(a)) {
                        Set<String> nextStateSet = new HashSet<>();
                        for(String nextState : newTransitions.get(state).get(a)){
                            nextStateSet.addAll(epsilonClosure.get(nextState));
                        }

                        String nextStates = "{" + String.join(", ", nextStateSet) + "}";
                        String text = maxWidths[i] == 0 ? nextStates + "|" : String.format("%-" + maxWidths[i] + "s|", nextStates);
                        nfaTable.append(text);
                    } else {
                        String text = maxWidths[i] == 0 ? "{ }|" : String.format("%-" + maxWidths[i] + "s|", "{ }");
                        nfaTable.append(text);
                    }
                }
    
                nfaTable.append("\n");
            }
    
            outputArea2.setText(nfaTable.toString());


        } catch (IllegalArgumentException e) {
            outputArea1.setText(e.getMessage());
        }
    }

    @FXML // F3: NFA to DFA
    private void f3() {
        isF4(false);
        initialiseData();

        try {
            parseRG(inputArea.getText().trim());

            // Create the initial state of the DFA
            Set<String> initialDFAState = epsilonClosure(Collections.singleton(startState), nfaTransitions);
            String initialStateName = stateSetToString(initialDFAState);

            // Initialize DFA transitions with the initial state
            dfaTransitions.put(initialStateName, new HashMap<>());

            // Queue for processing DFA states
            Queue<Set<String>> queue = new LinkedList<>();
            queue.add(initialDFAState);

            // Process each DFA state
            while (!queue.isEmpty()) {
                Set<String> currentState = queue.poll();
                String currentStateName = stateSetToString(currentState);

                // Check if current state contains any accept state of the NFA
                boolean isAcceptState = currentState.stream().anyMatch(acceptStates::contains);

                // Process each symbol in the alphabet
                for (String symbol : alphabet) {
                    Set<String> nextState = new HashSet<>();
                    for (String state : currentState) {
                        if (nfaTransitions.containsKey(state) && nfaTransitions.get(state).containsKey(symbol)) {
                            nextState.addAll(nfaTransitions.get(state).get(symbol));
                        }
                    }

                    // Calculate epsilon closure of the next state
                    Set<String> epsilonClosure = epsilonClosure(nextState, nfaTransitions);
                    String nextStateName = stateSetToString(epsilonClosure);

                    // Add transition to DFA
                    if (!dfaTransitions.containsKey(currentStateName)) {
                        dfaTransitions.put(currentStateName, new HashMap<>());
                    }
                    dfaTransitions.get(currentStateName).put(symbol, new ArrayList<>(epsilonClosure));

                    // If nextStateName is a new DFA state, add to queue for processing
                    if (!dfaTransitions.containsKey(nextStateName)) {
                        queue.add(epsilonClosure);
                    }
                }
            }

            // Output the DFA transition table
            StringBuilder dfaTable = new StringBuilder();
            dfaTable.append("DFA Transition Table:\n");

            // Print alphabet headers
            dfaTable.append("\t");
            for (String symbol : alphabet) {
                dfaTable.append(symbol).append("\t");
            }
            dfaTable.append("\n");

            // Print DFA states and transitions
            for (String state : dfaTransitions.keySet()) {
                dfaTable.append(state).append("\t");
                Map<String, ArrayList<String>> transitions = dfaTransitions.get(state);
                for (String symbol : alphabet) {
                    if (transitions.containsKey(symbol)) {
                        dfaTable.append(transitions.get(symbol)).append("\t");
                    } else {
                        dfaTable.append("{}\t");
                    }
                }
                dfaTable.append("\n");
            }

            // Display DFA transition table in outputArea2
            outputArea2.setText(dfaTable.toString());

        } catch (IllegalArgumentException e) {
            outputArea1.setText(e.getMessage());
        }
    }

    @FXML // Testing strings (up to 5 strings at once)
    private void testStrings() {
        if (dfaTransitions == null || dfaTransitions.isEmpty()) {
            outputArea2.setText("Please Generate the DFA first.");
            return;
        }

        String[] testStrings = outputArea1.getText().trim().split("\\n");
        if (testStrings.length == 0 || testStrings.length > 5) {
            outputArea2.setText("Please Input strings between 1 to 5 to check.");
            return;
        }

        StringBuilder result = new StringBuilder();
        for (String testString : testStrings) {
            boolean accepted = simulateDFA(testString);
            result.append(testString).append("\t").append(accepted ? "OK" : "NO").append("\n");
        }

        outputArea2.setText(result.toString());
    }

    private boolean simulateDFA(String testString) {
        String currentState = startState;
        for (char symbol : testString.toCharArray()) {
            String strSymbol = Character.toString(symbol);
            if (!dfaTransitions.containsKey(currentState) || !dfaTransitions.get(currentState).containsKey(strSymbol)) {
                return false;
            }
            currentState = stateSetToString(new HashSet<>(dfaTransitions.get(currentState).get(strSymbol)));
        }
        return acceptStates.contains(currentState);
    }


    private void initialiseData() {
        states = new ArrayList<>();
        alphabet = new ArrayList<>();
        startState = null;
        acceptStates = new ArrayList<>();
        nfaTransitions = new HashMap<>();
        dfaTransitions = new HashMap<>();
        hasEpsilon = false;
        isDigit = false;
        hasCheckType = false;
    }

    private void parseRG(String input) throws IllegalArgumentException {
        // split to LHS & RHS
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
                    if (!acceptStates.contains(left))
                        acceptStates.add(left);
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

                    // add transitions (alphabet & nextState)
                    nfaTransitions.putIfAbsent(left, new HashMap<>());
                    nfaTransitions.get(left).putIfAbsent(terminal, new ArrayList<>());
                    nfaTransitions.get(left).get(terminal).add(nonterminal);

                    Collections.sort(alphabet); // sort alphabet a-z or 0-9

                }
            }
        }

        if (acceptStates.isEmpty())
            throw new IllegalArgumentException("Invalid grammar: missing accepting states");
    }

    private void isF4(boolean b) { // outputArea1 will be editable for F4
        outputArea1.clear();
        outputArea2.clear();
        
        outputArea1.setEditable(b ? true : false);
        checkButton.setDisable(b ? false : true);

        Font defaultFont = Font.getDefault();
        Font tablefont = Font.font("Monospaced", FontWeight.NORMAL, FontPosture.REGULAR, 15);
        outputArea2.setFont(b ? defaultFont : tablefont);
        
    }

    @FXML
    private void prevImg() {
        if(index > 0) {
            index--;
            updateButtons();
            helpImg.setImage(images.get(index));
        }
    }

    @FXML
    private void nextImg() {
        if(index < images.size() - 1) {
            index++;
            updateButtons();
            helpImg.setImage(images.get(index));
        }
    }

    private void updateButtons() {
        prevButton.setDisable(index == 0);
        nextButton.setDisable(index == images.size() - 1);
    }

    // adding members data into memberTable
    private void addMembers() {
        ArrayList<Member> members = new ArrayList<>();
        members.add(new Member("leader", "Islam Tariqul", "1211300026", "25%", "F4"));
        members.add(new Member("member", "Adriana Batrisyia binti Hasnan", "1191102379", "25%", "F3"));
        members.add(new Member("member", "Hui Yen Ling", "1211307537", "25%", "F2"));
        members.add(new Member("member", "Vivian Wee Gek Ting", "1211306086", "25%", "F1"));
        
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        participantColumn.setCellValueFactory(new PropertyValueFactory<>("participant"));
        taskColumn.setCellValueFactory(new PropertyValueFactory<>("task"));

        memberTable.getItems().addAll(members);
    }

    private Set<String> epsilonClosure(Set<String> nextStateSet, HashMap<String, HashMap<String, ArrayList<String>>> nfaTransitions) {
        Set<String> epsilonClosure = new HashSet<>(nextStateSet);
        Queue<String> queue = new LinkedList<>(nextStateSet);

        while (!queue.isEmpty()) {
            String state = queue.poll();
            if (nfaTransitions.containsKey(state) && nfaTransitions.get(state).containsKey("ε")) {
                for (String nextState : nfaTransitions.get(state).get("ε")) {
                    if (!epsilonClosure.contains(nextState)) {
                        epsilonClosure.add(nextState);
                        queue.add(nextState);
                    }   
                }
            }
        }

        return epsilonClosure;
    }

    private String stateSetToString(Set<String> stateSet) {
        List<String> sortedStates = new ArrayList<>(stateSet);
        Collections.sort(sortedStates);
        return String.join("", sortedStates);
    }
}
