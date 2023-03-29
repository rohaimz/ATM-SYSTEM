package com.example.demo;

import javax.swing.*;

public class BankAccount
{
    public int accNumber = 0;
    public int accPasswd = 0;
    public int balance = 0;
    public StringBuilder miniStatement = new StringBuilder();
    public boolean freeze = false;
    private int failedAttempts;

    public BankAccount()
    {
    }
    
    public BankAccount(int a, int p, int b)
    {
        accNumber = a;
        accPasswd = p;
        balance = b;
        failedAttempts = 0;
        miniStatement.append("Account Number : "+a+"\n");
    }

    // withdraw money from the account. Return true if successful, or
    // false if the amount is negative, or less than the amount in the account 
    public boolean withdraw( int amount ) 
    { 
        Debug.trace( "BankAccount::withdraw: amount =" + amount ); 

        if (amount < 0 || balance < amount) {
            miniStatement.append("Failed Withdrawal : "+amount+"\n");
            return false;
        } else {
            balance = balance - amount;  // subtract amount from balance
            miniStatement.append("Withdrawn : "+amount+"\n");
            return true; 
        }
    }
    
    // deposit the amount of money into the account. Return true if successful,
    // or false if the amount is negative 
    public boolean deposit( int amount )
    { 
        Debug.trace( "LocalBank::deposit: amount = " + amount ); 
        if (amount < 0) {
            miniStatement.append("Failed Deposit : "+amount+"\n");
            return false;
        } else {
            balance = balance + amount;  // add amount to balance
            miniStatement.append("Deposit : "+amount+"\n");
            return true; 
        }
    }

    // Return the current balance in the account
    public int getBalance() 
    { 
        Debug.trace( "LocalBank::getBalance" ); 

        return balance;
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "accNumber=" + accNumber +
                ", accPasswd=" + accPasswd +
                ", balance=" + balance +
                '}';
    }

    public void freeze() {
        this.freeze = true;
        JOptionPane.showMessageDialog(null, "This account is frozen!");
        System.out.println("This account has been frozen due to too many failed login attempts. Please contact customer support for assistance.");
    }

    public void Unfreeze() {
        this.freeze = false;
        JOptionPane.showMessageDialog(null, "This account is now unfrozen");
        System.out.println("This account is now unfrozen");
    }

    public boolean isFreeze() {
        return freeze;
    }

    public int increment() {
        this.failedAttempts++;
        if (this.failedAttempts >= 5) {
            freeze();
        }
        return failedAttempts;
    }
}
