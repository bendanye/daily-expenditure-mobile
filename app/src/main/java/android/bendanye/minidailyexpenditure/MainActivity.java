package android.bendanye.minidailyexpenditure;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import bendanye.common.android.datastorage.DataStorageException;

public class MainActivity extends AppCompatActivity {

    private static final String DATE_FORMAT = "dd-MMM-yyyy (EEE)";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

    private TableLayout tableLayout;

    private String selectedDate;
    private Calendar myCalendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener date;
    private TextView dateTextView;

    private MiniDailyExpenditureDatabase database = new MiniDailyExpenditureDatabase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            dateTextView = (TextView) findViewById(R.id.dateTextView);
            tableLayout = (TableLayout) findViewById(R.id.tablelayout);

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                save();
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateAndTable();
            }

        };

        updateDateTextView();

        dateTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        buildTable();
    }

    private void updateDateAndTable() {
        updateDateTextView();
        removeExistingTableRows();
        buildTable();
    }

    private void updateDateTextView() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        selectedDate = dateFormat.format(myCalendar.getTime());
        dateTextView.setText(selectedDate);
    }

    private void removeExistingTableRows() {
        List<TableRow> rowsToDelete = new ArrayList<>();

        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View tmpRow = tableLayout.getChildAt(i);

            if (tmpRow instanceof TableRow) {
                TableRow row = (TableRow) tmpRow;

                if (row.getChildAt(0) instanceof TextView) {
                    rowsToDelete.add(row);
                }
            }
        }

        for(TableRow row : rowsToDelete) {
            tableLayout.removeView(row);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {

            case R.id.about:
                Intent i = new Intent(this, AboutActivity.class);
                startActivity(i);
                break;
            case R.id.exit:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void buildTable() {

        try {
            List<Expense> records = loadRecords(selectedDate);
            for(Expense expense : records) {
                tableLayout.addView(buildTableRow(expense));
            }

        } catch (DataStorageException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<Expense> loadRecords(String selectedDate) throws DataStorageException {
        List<Expense> results = database.load(selectedDate);

        if (results.isEmpty()) {
            results.add(new Expense(getString(R.string.row_description_breakfast), null));
            results.add(new Expense(getString(R.string.row_description_lunch), null));
            results.add(new Expense(getString(R.string.row_description_teabreak), null));
            results.add(new Expense(getString(R.string.row_description_dinner), null));
        }

        return results;
    }

    private TableRow buildTableRow(Expense expense) {
        final TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

        EditText descriptionEditView = new EditText(this);
        descriptionEditView.setInputType(InputType.TYPE_CLASS_TEXT);
        descriptionEditView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        descriptionEditView.setText(expense.getDescription());
        descriptionEditView.setEms(8);
        row.addView(descriptionEditView);

        EditText amountEditText = new EditText(this);
        amountEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        amountEditText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        amountEditText.setText(expense.getAmount());
        amountEditText.setEms(4);
        row.addView(amountEditText);

        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.ic_delete_black_24dp);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableLayout.removeView(row);
            }
        });
        row.addView(imageView);

        return row;
    }

    public void onClickPrevButton(View view) {
        save();
        myCalendar.add(Calendar.DAY_OF_MONTH, -1);
        updateDateAndTable();
    }

    public void onClickNextButton(View view) {
        save();
        myCalendar.add(Calendar.DAY_OF_MONTH, 1);
        updateDateAndTable();
    }

    public void onClickAdd(View view) {
        tableLayout.addView(buildTableRow(new Expense(null, null)));
    }

    public void onClickSave(View view) {
        save();
    }

    private void save() {
        List<Expense> records = new ArrayList<>();

        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View tmpRow = tableLayout.getChildAt(i);

            if (tmpRow instanceof TableRow) {
                TableRow row = (TableRow) tmpRow;

                if (row.getChildAt(0) instanceof TextView) {
                    TextView descriptionEditView = (TextView) row.getChildAt(0);
                    Log.d("descriptionEditView", descriptionEditView.getText().toString());

                    TextView amountEditText = (TextView) row.getChildAt(1);
                    Log.d("amountEditText", amountEditText.getText().toString());

                    Expense expense = new Expense(descriptionEditView.getText().toString(), amountEditText.getText().toString());
                    records.add(expense);
                }
            }
        }

        try {
            database.save(selectedDate,records);

            //---display file saved message---
            Toast.makeText(getBaseContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
        }

        catch (DataStorageException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickViewTodayButton(View view) {
        save();
        myCalendar = Calendar.getInstance();
        updateDateAndTable();
    }

    public void onClickViewEnteredDatesButton(View view) {
        List<String> beforeSortDates = database.getAll();
        try {

            final List<String> dates = sort(beforeSortDates, false);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick a date to view");

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    dates);

            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    save();

                    String selectedDate = dates.get(item);

                    try {
                        Date formatDate = DATE_FORMATTER.parse(selectedDate);

                        myCalendar.setTime(formatDate);
                        updateDateAndTable();
                    } catch (ParseException e) {
                        Toast.makeText(getBaseContext(), "Unable to select the date, " + selectedDate, Toast.LENGTH_LONG).show();
                    }
                }
            });

            builder.setCancelable(true);

            AlertDialog alert = builder.create();
            alert.show();
        } catch (ParseException e) {
            Toast.makeText(getBaseContext(), "Problem click the button!", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickDeleteEnteredDatesButton(View view) {
        List<String> beforeSortDates = database.getAllExclude(selectedDate);

        try {
            final List<String> dates = sort(beforeSortDates, true);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick a date to delete");

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    dates);

            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    String selectedDate = dates.get(item);

                    database.delete(selectedDate);

                    Toast.makeText(getBaseContext(), "Delete " + selectedDate + " successfully!", Toast.LENGTH_LONG).show();
                }
            });

            builder.setCancelable(true);

            AlertDialog alert = builder.create();
            alert.show();

        } catch (ParseException e) {
            Toast.makeText(getBaseContext(), "Problem click the button!", Toast.LENGTH_LONG).show();
        }

    }

	private List<String> sort(List<String> dates, boolean ascendingOrder) throws ParseException {
		List<String> result = new ArrayList<>();

		List<Date> tmp = new ArrayList<>();

		for(String date: dates){
			Date formatDate = DATE_FORMATTER.parse(date);
			tmp.add(formatDate);
		}

        Collections.sort(tmp);

        if (!ascendingOrder) {
            Collections.reverse(tmp);
        }

		for(Date date: tmp) {
			result.add(DATE_FORMATTER.format(date));
		}

		return result;
	}
}
