package lk.ac.mrt.cse.dbs.simpleexpensemanager.control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class DBCtrl extends SQLiteOpenHelper {




    private static final int version=1;
    private static final String db_name="200031A.db";
    private static final String table1="accounts";
    private static final String table2="transactions";

    private static final String accountNo="accno";
    private static final String bankName="bankname";
    private static final String accountHolderName="accholder";
    private static final String balance="balance";

    public static final String id = "ID";
    public static final String type = "TYPE";
    public static final String date = "DATE";
    public static final String amount = "AMOUNT";


    public DBCtrl(@Nullable Context context) {
        super(context, db_name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String create_table1_query="CREATE TABLE "+table1+" ("+ accountNo +" TEXT PRIMARY KEY, "+ bankName +" TEXT, "+ accountHolderName+" TEXT, "+ balance +" REAL );";
        String create_table2_query="CREATE TABLE " +
                table2 + " (" +
                id + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                accountNo + " INTEGER," +
                type + " INTEGER," +
                date + " TEXT," +
                amount + " REAL" + ")";
        sqLiteDatabase.execSQL(create_table1_query);
        sqLiteDatabase.execSQL(create_table2_query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + table1);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + table2);
        onCreate(sqLiteDatabase);
    }

    public void insertAccountData(Account acc){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(accountNo, acc.getAccountNo());
        values.put(bankName, acc.getBankName());
        values.put(accountHolderName, acc.getAccountHolderName());
        values.put(balance, acc.getBalance());

        db.insert(table1, null, values);
        db.close();
    }


    public ArrayList<String> readAccountNumbers() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT "+accountNo+" FROM " + table1, null);

        ArrayList<String> acc_numbers = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                acc_numbers.add(cursor.getString(0));
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();
        return acc_numbers;

    }

    public ArrayList<Account> readAccounts() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor acc_cursor = db.rawQuery("SELECT * FROM " + table1, null);

        ArrayList<Account> accountList = new ArrayList<>();

        if (acc_cursor.moveToFirst()) {
            do {
                accountList.add(new Account(acc_cursor.getString(0),
                        acc_cursor.getString(1),
                        acc_cursor.getString(2),
                        acc_cursor.getDouble(3)));
            } while (acc_cursor.moveToNext());
        }
        acc_cursor.close();
        db.close();
        return accountList;

    }

    public Account checkForAccount(String accnumber){
        SQLiteDatabase db = getReadableDatabase();

        Cursor result = db.rawQuery("SELECT * FROM " + table1 +" WHERE "+accountNo+" =? ", new String[] {accnumber});

        if (result.moveToFirst()) {
            return new Account(result.getString(0),
                    result.getString(1),
                    result.getString(2),
                    result.getDouble(3));
        }

        db.close();
        return null;

    }
    public void deleteAccount(String accnumber){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(table1,accountNo+"=?",new String[]{accnumber});
    }

    public void updateAcc(String param_accountNum, ExpenseType expenseType, double amount){
        SQLiteDatabase db = getWritableDatabase();
        String query = "select " +balance+ " from "+ table1 +" where "+ accountNo +" = '"+ param_accountNum +"' ;";
        Cursor cursor = db.rawQuery(query,null);

        cursor.moveToFirst();
        double temp_balance = cursor.getDouble(0);
        switch (expenseType) {
            case EXPENSE:
                temp_balance  -= amount;
                break;
            case INCOME:
                temp_balance  += amount;
                break;
        }

        String query_updt = "UPDATE "+table1+" SET "+balance+" = "+ temp_balance +" WHERE "+accountNo+" = '"+param_accountNum+"' ;";
        db.execSQL(query_updt);
        cursor.close();
        db.close();
    }

    public boolean transLog(Date param_date, String accountNum, ExpenseType expenseType, double amount1){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(param_date);

        values.put(accountNo, accountNum);

        values.put(date, formattedDate.toString());
        values.put(type, expenseType == ExpenseType.EXPENSE ? 0 : 1);
        values.put(amount, amount1);

        if (db.insert(table2, null, values) == -1 )
            return false;
        else
            return true;
    }

    public List<Transaction> readAllTrans(){
        SQLiteDatabase db = getReadableDatabase();
        List<Transaction> trans_data = new ArrayList<>();
        String query = "SELECT * from "+table2;

        Cursor result = db.rawQuery(query, null);

        if (result.moveToFirst()) {
            do {
                Date temp_date = null;
                try {
                    temp_date = new SimpleDateFormat("dd-MM-yyyy").parse(result.getString(1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String temp_accountNo = result.getString(2);
                String temp_type = result.getString(3);
                ExpenseType temp_expenseType = null;

                if (temp_type.equals("EXPENSE")) {
                    temp_expenseType = ExpenseType.EXPENSE;
                }

                else if (temp_type.equals("INCOME")) {
                    temp_expenseType = ExpenseType.INCOME;
                }

                double temp_amount = result.getDouble(4);
                trans_data.add(new Transaction(temp_date, temp_accountNo, temp_expenseType, temp_amount));

            } while (result.moveToNext());
        }

        result.close();
        db.close();
        return  trans_data;

    }

    public List<Transaction> get_paginated_transaction_logs(int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Transaction> transList = new ArrayList<>();
        String query = "SELECT DATE,accno,TYPE,AMOUNT from "+table2;
        Cursor values = db.rawQuery(query, null);

        if(values.moveToFirst()){
            int i =1;
            do {
                try {
                    Date temp_date =new SimpleDateFormat("dd-MM-yyyy").parse(values.getString(0));
                    String temp_accountNumber = values.getString(1);
                    int temp_type = values.getInt(2);
                    ExpenseType temp_expenseType = null;

                    if (temp_type == 0) {
                        temp_expenseType = ExpenseType.EXPENSE;
                    }

                    else if (temp_type == 1) {
                        temp_expenseType = ExpenseType.INCOME;
                    }

                    double temp_amount = values.getDouble(3);
                    Transaction newTransaction = new Transaction(temp_date, temp_accountNumber, temp_expenseType, temp_amount);
                    transList.add(newTransaction);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                i+=1;

            } while (values.moveToNext() && i < limit);
        }
        values.close();
        db.close();
        return  transList;
    }

}
