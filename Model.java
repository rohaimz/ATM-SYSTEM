package com.example.demo;

// The model represents all the actual content and functionality of the app
// For the ATM, it keeps track of the information shown in the display
// (the title and two message boxes), and the interaction with the bank, executes
// commands provided by the controller and tells the view to update when
// something changes


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Model
{
    // the ATM model is always in one of three states - waiting for an account number,
    // waiting for a password, or logged in and processing account requests.
    // We use string values to represent each state:
    // (the word 'final' tells Java we won't ever change the values of these variables)
    final String ACCOUNT_NO = "account_no";
    final String PASSWORD = "password";
    final String LOGGED_IN = "logged_in";
    final String FORGET_PASSWORD = "forget_password";
    final String DEPOSIT="Deposit";
    final String WITHDRAW="Withdraw";


    // variables representing the ATM model
    String state = ACCOUNT_NO;      // the state it is currently in
    int  number = 0;                // current number displayed in GUI (as a number, not a string)
    Bank  bank = null;              // The ATM talks to a bank, represented by the Bank object.
    int accNumber = -1;             // Account number typed in
    int accPasswd = -1;             // Password typed in
    // These three are what are shown on the View display
    String title = "Espresso Banking";      // The contents of the title message
    String display1 = null;         // The contents of the Message 1 box (a single line)
    String display2 = null;         // The contents of the Message 2 box (may be multiple lines)

    // The other parts of the model-view-controller setup
    public View view;
    public Controller controller;

    // Model constructor - we pass it a Bank object representing the bank we want to talk to
    public Model(Bank b)
    {
        Debug.trace("Model::<constructor>");
        bank = b;
    }

    // Initialising the ATM (or resetting after an error or logout)
    // set state to ACCOUNT_NO, number to zero, and display message
    // provided as argument and standard instruction message
    public void initialise(String message) {
        setState(ACCOUNT_NO);
        number = 0;
        display1 = message;
        display2 =  "Enter your account number\n" +
                "Then, click \"Enter\"";
    }

    // this method is used to change state
    public void setState(String newState)
    {
        if ( !state.equals(newState) )
        {
            String oldState = state;
            state = newState;
            Debug.trace("Model::setState: changed state from "+ oldState + " to " + newState);
        }
    }

    // These methods are called by the Controller to change the Model
    // when particular buttons are pressed on the GUI

    // process a number key (the key is specified by the label argument)
    public void processNumber(String label)
    {
        // turn the first char of the label into an int
        // and update the number variable with it
        char c = label.charAt(0);
        number = number * 10 + c-'0';           // Build number
        // show the new number in the display
        display1 = "" + number;
        display();  // update the GUI
    }

    // process the Clear button - reset the number (and number display string)
    public void processClear()
    {
        // clear the number stored in the model
        number = 0;
        display1 = "";
        display();  // update the GUI
    }

    // process the Enter button
    // this is the most complex operation - the Enter key causes the ATM to change state
    // from account number, to password, to logged_in and back to account number
    // (when you log out)

    public void processEnter()
    {
        // Enter was pressed - what we do depends on what state the ATM is already in
        switch ( state )
        {
            case ACCOUNT_NO:
                // we were waiting for a complete account number - save the number we have
                // reset the typed in number to 0 and change to the state where we are expecting
                // a password
                accNumber = number;
                number = 0;
                setState(PASSWORD);
                display1 = "";
                display2 = "Now enter your password\n" +
                        "Followed by \"Enter\"";
                break;
            case PASSWORD:
                // we were waiting for a password - save the number we have as the password
                // and then contact the bank with accNumber and accPasswd to try and login to
                // an account
                accPasswd = number;
                number = 0;
                display1 = "";

                if (!bank.isValid(accNumber)){
                    display2 = "Account not found";
                    JOptionPane.showMessageDialog(null, display2);
                    break;
                }
                // check if the account is frozen
                if (bank.isFrozen(accNumber)) {
                    display2 = "Your account is frozen. Please contact the bank.";
                    break;
                }

                // now check the account/password combination. If it's ok go into the LOGGED_IN
                // state, otherwise increment the failed login counter and go back to the start
                // (by re-initializing) if the counter exceeds 5
                if (bank.login(accNumber, accPasswd)) {
                    setState(LOGGED_IN);
                    display2 = "Accepted\n" +
                            "Please enter the transaction you require";
                } else {
                    int failedAttempts = bank.incrementFailedAttempts(accNumber);
                    System.out.println("Failed attempt: "+failedAttempts);
                    if (failedAttempts < 5) {
                        System.out.println("FAILED!");
                        display2 = "Unknown account/password\n" +
                                "You have " + (5 - failedAttempts) + " attempts left";
                        // show a message dialog box to notify the user that the password was incorrect
                        JOptionPane.showMessageDialog(null, display2);
                    } else {
                        bank.freezeAccount(accNumber);
                        System.out.println("Frozen!");
                        display2 = "Too many failed attempts. Your account has been frozen.\n" +
                                "Please contact the bank.";
                        JOptionPane.showMessageDialog(null, display2);
                    }
                    initialise("");
                }
                break;
            case LOGGED_IN:
                break;
            case FORGET_PASSWORD:
                try{
                    bank.account.accPasswd = number;
                    display2 = "Password changed successfully";
                    Debug.trace(bank.account.toString());
                    display();
                } catch (Exception exception) {
                    System.out.println(exception.getMessage());
                }
                break;
            case DEPOSIT:
                bank.deposit(number);
                display2="Deposited amount ="+number;
                display();
                break;
            case WITHDRAW:
                if(bank.getBalance() < number){
                    display2 = "Not enough balance to withdrawal";
                }
                else {
                    bank.withdraw(number);
                    display2="Withdrawal amount ="+number;
                    view.update();
                }
                display();
                break;
            default:
        }
        display();  // update the GUI
    }

    public void processAdmin() {
        JFrame adminFrame = new JFrame("Admin Panel");
        adminFrame.setSize(400, 300);
        adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Admin Panel");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, c);

        JLabel cashLabel = new JLabel("Add Cash:");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        panel.add(cashLabel, c);

        JTextField cashInputField = new JTextField(10); // create text field to take input for cash
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(cashInputField, c);

        JButton addCashButton = new JButton("Add");
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        addCashButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cashInput = cashInputField.getText(); // get input for cash as string
                double cashToAdd = Double.parseDouble(cashInput); // parse input as double
                bank.atmDeposit(cashToAdd); // pass cash to add to atmDeposit() method
                JOptionPane.showMessageDialog(null, "Balance after loading cash: " + bank.atmCash());
            }
        });
        panel.add(addCashButton, c);

        JLabel removeLabel = new JLabel("Remove Cash:");
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        panel.add(removeLabel, c);

        JTextField removeInputField = new JTextField(10); // create text field to take input for cash
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(removeInputField, c);

        JButton removeCashButton = new JButton("Remove");
        c.gridx = 2;
        c.gridy = 2;
        c.anchor = GridBagConstraints.EAST;
        removeCashButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cashInput = removeInputField.getText(); // get input for cash as string
                double cashToRemove = Double.parseDouble(cashInput); // parse input as double
                if ((bank.atmCash() - cashToRemove) <= 0) {
                    JOptionPane.showMessageDialog(null, "Invalid amount: not enough cash in system");
                } else{
                    bank.atmWithdraw(cashToRemove); // pass cash to remove to atmWithdraw() method
                    JOptionPane.showMessageDialog(null, "Balance after removing cash: " + bank.atmCash());
                }

            }
        });
        panel.add(removeCashButton, c);

        JLabel freezeLabel = new JLabel("Freeze Account:");
        c.gridx = 0;
        c.gridy = 3;
        c.anchor = GridBagConstraints.WEST;
        panel.add(freezeLabel, c);

        JTextField freezeInputField = new JTextField(10); // create text field to take input for account
        c.gridx = 1;
        c.gridy = 3;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(freezeInputField, c);

        JButton freezeButton = new JButton("Freeze");
        c.gridx = 2;
        c.gridy = 3;
        c.anchor = GridBagConstraints.EAST;
        freezeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String account = freezeInputField.getText(); // get input for account as string
                if (bank.isValid(Integer.parseInt(account))) { // pass account to freezeAccount() method
                    if (bank.isFrozen(Integer.parseInt(account))){
                        JOptionPane.showMessageDialog(null, "Account " + account + " already frozen.");

                    }else {
                        bank.freezeAccount(Integer.parseInt(account));
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "Account " + account + " not found.");
                }
            }
        });
        panel.add(freezeButton, c);

        JLabel unfreezeLabel = new JLabel("Unfreeze Account:");
        c.gridx = 0;
        c.gridy = 4;
        c.anchor = GridBagConstraints.WEST;
        panel.add(unfreezeLabel, c);

        JTextField unfreezeInputField = new JTextField(10); // create text field to take input for account
        c.gridx = 1;
        c.gridy = 4;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(unfreezeInputField, c);

        JButton unfreezeButton = new JButton("Unfreeze");
        c.gridx = 2;
        c.gridy = 4;
        c.anchor = GridBagConstraints.EAST;
        unfreezeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String account = unfreezeInputField.getText(); // get input for account as string
                if(bank.isValid(Integer.parseInt(account))) {
                    if (!bank.isFrozen(Integer.parseInt(account))){
                        JOptionPane.showMessageDialog(null, "Account " + account + " in now unfrozen.");
                    }else {
                        // pass account to unfreezeAccount() method
                        bank.UnfreezeAccount(Integer.parseInt(account));
                    }

                }
                else {
                    JOptionPane.showMessageDialog(null, "Account " + account + " not found.");
                }
            }
        });
        panel.add(unfreezeButton, c);

        JButton showCash = new JButton("Show Cash");
        showCash.setAlignmentX(Component.CENTER_ALIGNMENT);
        showCash.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Total ATM Cash balance: "+bank.atmCash());
            }
        });
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(showCash, c);

        adminFrame.getContentPane().add(panel);
        adminFrame.setVisible(true);
    }



    // Withdraw button - check we are logged in and if so try and withdraw some money from
    // the bank (number is the amount showing in the interface display)
    public void processWithdraw()
    {
        if (bank.loggedIn() ) {
            display1 = "Withdraw";
            display2 = "Current Balance : "+bank.getBalance()+ "\nEnter amount followed by ENT";
            setState(WITHDRAW);
            display();
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }

    // Deposit button - check we are logged in and if so try and deposit some money into
    // the bank (number is the amount showing in the interface display)
    public void processDeposit()
    {
        if (bank.loggedIn()) {
            display1 = "Deposit";
            display2 = "Current Balance : "+bank.getBalance()+ "\nEnter amount followed by ENT";

            setState(DEPOSIT);
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }

    // Balance button - check we are logged in and if so access the current balance
    // and display it
    public void processBalance()
    {
        if (bank.loggedIn() ) {
            number = 0;
            display1 = "Balance";
            display2 = "Your balance is: " + bank.getBalance();
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }

    // Finish button - check we are logged in and if so log out
    /*

    public void processFinish()
    {
        if (state.equals(LOGGED_IN) ) {
            // go back to the log in state
            setState(ACCOUNT_NO);
            number = 0;
            display1 = "Finished";
            display2 = "Welcome: Enter your account number";
            bank.logout();
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }
    */

    // Any other key results in an error message and a reset of the GUI
    public void processUnknownKey(String action)
    {
        // unknown button, or invalid for this state - reset everything
        Debug.trace("Model::processUnknownKey: unknown button \"" + action + "\", re-initialising");
        // go back to initial state
        initialise("Invalid command");
        display();
    }

    // This is where the Model talks to the View, by calling the View's update method
    // The view will call back to the model to get new information to display on the screen
    public void display()
    {
        Debug.trace("Model::display");
        view.update();
    }

    public void forgetPassword(){
        setState(FORGET_PASSWORD);
        display1 = "Forgot Password";
        display2 = "Now enter new password\n" +"Followed by \"Ent\"";
        display();
    }

    public void miniStatement() {
        display1 = "Mini Statement";
        display2 = bank.account.miniStatement.toString() + "\nNet Balance : " + bank.account.getBalance()+"\n";
        display();
    }

    public void logout() {
        setState(ACCOUNT_NO);
        bank.logout();
        display1 = "Welcome to ATM";
        display2 = "Account Logged out\n\nEnter your account number\n" + "Followed by Ent";
        display();

    }
}
