package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;
import android.database.Cursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.DBCtrl;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;



public class PersistentAccountDAO implements AccountDAO {
    private Context context;
    private PersistentAccountDAO(){
        dbctrl = new DBCtrl(context);
    }
    public PersistentAccountDAO(Context context){
        this.context=context;


        dbctrl = new DBCtrl(this.context);
    }
    DBCtrl dbctrl;


   // private final Map<String, Account> accounts=new HashMap<>();

    @Override
    public List<String> getAccountNumbersList() {
     //   return new ArrayList<>(accounts.keySet());
       return dbctrl.readAccountNumbers();

    }

    @Override
    public List<Account> getAccountsList() {
        return dbctrl.readAccounts();

    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        Account res=dbctrl.checkForAccount(accountNo);
        if(res==null){
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }else{
            return res;
        }




    }

    @Override
    public void addAccount(Account account) {
        dbctrl.insertAccountData(account);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        dbctrl.deleteAccount(accountNo);
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        dbctrl.updateAcc(accountNo,expenseType,amount);
    }
}
