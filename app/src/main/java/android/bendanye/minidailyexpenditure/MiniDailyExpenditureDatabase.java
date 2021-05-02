package android.bendanye.minidailyexpenditure;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bendanye.common.android.datastorage.AbstractFileDatabaseStorage;

/**
 * Created by benjamin ng on 22/8/2015.
 */
public class MiniDailyExpenditureDatabase extends AbstractFileDatabaseStorage<Expense> {

    public List<String> getAll() {
        List<String> result = new ArrayList<>();
        File directory = this.getDirectory();
        Log.d("loadDirectory", directory.getAbsolutePath());

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                result.add(file.getName().replace(".txt", ""));
            }
        }

        return result;
    }

    public List<String> getAllExclude(String fileName) {
        List<String> result = new ArrayList<>();
        File directory = this.getDirectory();
        Log.d("loadDirectory", directory.getAbsolutePath());

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                result.add(file.getName().replace(".txt", ""));
            }
        }

        result.remove(fileName);

        return result;
    }

    public boolean delete(String fileName) {
        boolean result = false;
        File directory = this.getDirectory();
        Log.d("loadDirectory", directory.getAbsolutePath());
        File file = new File(directory, fileName + ".txt");
        if(file.exists()) {
            file.delete();
            result = true;
        }

        return result;
    }

    @Override
    protected String getOutput(List<Expense> records)
    {
        StringBuilder stringBuffer = new StringBuilder();
        for (Expense expense : records)
        {
            String printOutput = expense.getDescription() + ";" + expense.getAmount() + ";";

            stringBuffer.append(printOutput).append("\n");
        }

        return stringBuffer.toString();
    }

    @Override
    protected String getAppFileDirectory() {
        return "MyFiles";
    }

    @Override
    protected Expense getInput(String inputString) {
        String[] strArray = inputString.split(";");
        String description = strArray.length >= 1 ? strArray[0] : "";
        String amount = strArray.length >= 2 ? strArray[1] : "";
        return new Expense(description, amount);
    }
}
