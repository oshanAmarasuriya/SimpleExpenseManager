package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;

import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.DBCtrl;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistentTransactionDAO implements TransactionDAO {
    private Context context;

    public PersistentTransactionDAO(Context context){
        this.context=context;
        dbctrl = new DBCtrl(this.context);
    }
    DBCtrl dbctrl;

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
       dbctrl.transLog(date,accountNo,expenseType,amount);
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {

        return dbctrl.readAllTrans();
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {

        return dbctrl.get_paginated_transaction_logs(limit);
    }
}
