package org.secuso.privacyfriendlyfinance.domain.legacy;

import android.content.Context;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.secuso.privacyfriendlyfinance.domain.FinanceDatabase;
import org.secuso.privacyfriendlyfinance.domain.model.Category;
import org.secuso.privacyfriendlyfinance.domain.model.Transaction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MigrationFromUnencrypted {
    public static final String TRANSACTION_DB_NAME = "PF_FinanceManager_DB";
    public static final String CATEGORY_DB_NAME = "PF_FinanceManager_DB_Categories";

    public static boolean legacyDatabaseExists(Context context) {
        File databaseFile = new File(context.getApplicationInfo().dataDir + "/databases/" + TRANSACTION_DB_NAME);
        return databaseFile.exists() && databaseFile.isFile();
    }

    public static void deleteLegacyDatabase(Context context) {
        File databaseDir = new File(context.getApplicationInfo().dataDir + "/databases");
        File[] files = databaseDir.listFiles();
        if(files != null) {
            for(File f: files) {
                if(!f.isDirectory()) {
                    if (f.getName().startsWith(TRANSACTION_DB_NAME) || f.getName().startsWith(CATEGORY_DB_NAME)) {
                        f.delete();
                    }
                }
            }
        }
    }

    public static void migrateTo(FinanceDatabase target, Context context) {
        String dbDir = context.getApplicationInfo().dataDir + "/databases/";
        DatabaseExporter categoryExporter = new DatabaseExporter(dbDir + CATEGORY_DB_NAME, "category");
        DatabaseExporter transactionExporter = new DatabaseExporter(dbDir + TRANSACTION_DB_NAME, "transaction");
        System.out.println("categoryJson1");
        try {
            List<String> categoryNames = new ArrayList<>();
            JSONArray categories = categoryExporter.dbToJSON().getJSONObject("category").getJSONArray("CategoryData");
            for (int i = 0; i < categories.length(); ++i) {
                String categoryName = categories.getJSONObject(i).getString("categoryName");
                if (categoryName != "Standard") categoryNames.add(categoryName);
            }
            JSONArray transactions = transactionExporter.dbToJSON().getJSONObject("transaction").getJSONArray("FinanceData");
            for (int i = 0; i < transactions.length(); ++i) {
                String categoryName = transactions.getJSONObject(i).getString("transactionCategory");
                if (categoryName != "Standard") categoryNames.add(categoryName);
            }

            for (String categoryName : categoryNames) {
                Category category = target.categoryDao().getByName(categoryName);
                if (category == null) {
                    target.categoryDao().insert(new Category(categoryName));
                }
            }

            for (int i = 0; i < transactions.length(); ++i) {
                JSONObject sourceTransaction = transactions.getJSONObject(i);
                Transaction transaction = new Transaction();
                transaction.setAccountId(0L);
                transaction.setName(sourceTransaction.getString("transactionName"));
                long amount = (long) (sourceTransaction.getDouble("transactionAmount") * 100);
                if (sourceTransaction.getInt("transactionType") == 0) {
                    amount = -amount;
                }
                transaction.setAmount(amount);
                transaction.setDate(LocalDate.parse(
                        sourceTransaction.getString("transactionDate"),
                        DateTimeFormat.forPattern("dd/MM/yyyy")
                ));
                Category category = target.categoryDao().getByName(sourceTransaction.getString("transactionCategory"));
                if (category != null) {
                    transaction.setCategoryId(category.getId());
                }
                target.transactionDao().insert(transaction);
            }


//
//            JSONObject transactionJson = transactionExporter.dbToJSON();
//            System.out.println(categoryJson.toString(1));
            System.out.println(transactions.toString(1));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}