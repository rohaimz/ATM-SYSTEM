package com.example.demo;
import java.util.ArrayList;

// Bank class - simple implementation of an ATM, with a list of bank accounts, and
// a current account that we are logged in to.

public class Bank
{
    // Instance variables containing the bank information
    ArrayList<BankAccount> accounts = new ArrayList<>(); // arraylist to hold the bank accounts
    BankAccount account = null; // currently logged in account ('null' if no-one is logged in)
    double cash = 1000;


    // Constructor method - this provides a couple of example bank accounts to work with
    public Bank()
    {
        Debug.trace( "Bank::<constructor>");
    }

    // A method to create new BankAccounts
    public BankAccount makeBankAccount(int accNumber, int accPasswd, int balance)
    {
        return new BankAccount(accNumber, accPasswd, balance);
    }

    // A method to add a new bank account to the bank - it returns true if it succeeds
    // or false if it fails (it never fails, in this implementation)
    public boolean addBankAccount(BankAccount a)
    {
        accounts.add(a);
        Debug.trace( "Bank::addBankAccount: added " +
                a.accNumber +" "+ a.accPasswd +" �"+ a.balance);
        return true;
    }

    // a variant of addBankAccount which makes the account and adds it all in one go.
    public boolean addBankAccount(int accNumber, int accPasswd, int balance)
    {
        return addBankAccount(makeBankAccount(accNumber, accPasswd, balance));
    }

    public void updateBankAccount(BankAccount oldAccount,BankAccount newAccount){
        int index = accounts.indexOf(oldAccount);
        accounts.set(index,newAccount);
    }

    // Check whether the current saved account and password correspond to 
    // an actual bank account, and if so login to it (by setting 'account' to it)
    // and return true. Otherwise, reset the account to null and return false
    public boolean login(int newAccNumber, int newAccPasswd)
    {
        Debug.trace( "Bank::login: accNumber = " + newAccNumber);
        logout(); // logout of any previous account

        //Search arraylist for bank account

        for (BankAccount b: accounts) {
            if (b.accNumber == newAccNumber && b.accPasswd == newAccPasswd) {
                // found the right account
                Debug.trace( "Bank::login: logged in, accNumber = " + newAccNumber );
                account = b;
                return true;
            }
        }

        // not found - return false
        account = null;
        return false;
    }

    // Reset the bank to a 'logged out' state
    public void logout()
    {
        if (loggedIn())
        {
            Debug.trace( "Bank::logout: logging out, accNumber = " + account.accNumber);
            account = null;
        }
    }

    // test whether the bank is logged in to an account or not
    public boolean loggedIn()
    {
        if (account == null)
        {
            return false;
        } else {
            return true;
        }
    }

    // try to deposit money into the account (by calling the deposit method on the 
    // BankAccount object)
    public boolean deposit(int amount)
    {
        if (loggedIn()) {
            atmDeposit(amount);
            return account.deposit(amount);
        } else {
            return false;
        }
    }

    // try to withdraw money into the account (by calling the withdraw method on the 
    // BankAccount object)
    public boolean withdraw(int amount)
    {
        if (loggedIn()) {
            atmWithdraw(amount);
            return account.withdraw(amount);

        } else {
            return false;
        }
    }

    public void atmWithdraw(double x){
        cash-=x;
    }

    public double atmCash(){
        return cash;
    }

    public void atmDeposit(double x){
        cash+=x;
    }
    // get the account balance (by calling the balance method on the 
    // BankAccount object)
    public int getBalance()
    {
        if (loggedIn()) {
            return account.getBalance();
        } else {
            return -1; // use -1 as an indicator of an error
        }
    }

    public boolean isFrozen(int newAccNumber) {
        for (BankAccount b: accounts) {
            if (b.accNumber == newAccNumber) {
                // found the right account
                Debug.trace( "Bank::Freeze: Freezing accNumber = " + newAccNumber );
                account = b;
            }
        }
        return account.isFreeze();
    }

    public int incrementFailedAttempts(int acc) {
        for (BankAccount b: accounts) {
            if (b.accNumber == acc) {
                // found the right account
                Debug.trace( "Bank::Increment: Failed attempt of:  " + acc );
                account = b;
            }
        }
        return account.increment();


    }


    public void freezeAccount(int newAccNumber) {
        for (BankAccount b: accounts) {
            if (b.accNumber == newAccNumber) {
                // found the right account
                Debug.trace( "Bank::Freeze: Freezing accNumber = " + newAccNumber );
                account = b;
                b.freeze();
            }
        }
    }

    public void UnfreezeAccount(int newAccNumber) {
        for (BankAccount b: accounts) {
            if (b.accNumber == newAccNumber) {
                // found the right account
                Debug.trace( "Bank::Unfreeze: Unfreezing accNumber = " + newAccNumber );
                account = b;
                b.Unfreeze();
            }
        }
    }


    public boolean isValid(int accNumber) {
        for (BankAccount b: accounts) {
            if (b.accNumber == accNumber) {
                // found the right account
                Debug.trace( "Bank::Found: True for " + accNumber );
                System.out.println("True");
                return true;
            }
        }
        System.out.println("False");
        return false;
    }
}