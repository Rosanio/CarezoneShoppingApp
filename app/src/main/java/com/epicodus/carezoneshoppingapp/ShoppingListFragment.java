package com.epicodus.carezoneshoppingapp;

/*TODO:
    Create post request to server
    Create put and delete requests to server
    Consider how to add items on server to database without creating dups
    Maybe pull to refresh
 */

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShoppingListFragment extends Fragment implements View.OnClickListener {
    private List<Item> mItems;

    @Bind(R.id.addItemButton) Button mAddItemButton;
    @Bind(R.id.clearDatabaseButton) Button mClearDatabaseButton;
    @Bind(R.id.shoppingListTableLayout) TableLayout mShoppingListTableLayout;

    private DatabaseHelper db;

    private Item selectedItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_list, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mAddItemButton.setOnClickListener(this);
        mClearDatabaseButton.setOnClickListener(this);
        db = new DatabaseHelper(getActivity());
        updateTable();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.addItemButton:
                openAddItemDialog();
                break;
            case R.id.shoppingListTableRow:
                int position = (int) v.getTag();
                selectedItem = mItems.get(position);
                openUpdateItemDialog();
                break;
            case R.id.deleteItemButton:
                db.deleteItemRecord(selectedItem.getId());
                updateTable();
                break;
            case R.id.clearDatabaseButton:
                db.deleteAllItemRecords();
                updateTable();
            default:
                break;
        }
    }

    public void openAddItemDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View subView = inflater.inflate(R.layout.fragment_add_item, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add an Item");
        builder.setView(subView);

        final EditText nameEditText = (EditText) subView.findViewById(R.id.itemNameEditText);
        final EditText categoryEditText = (EditText) subView.findViewById(R.id.itemCategoryEditText);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameEditText.getText().toString();
                String category = categoryEditText.getText().toString();

                if(name.trim().length() > 0 && category.trim().length() == 0) {
                    Toast.makeText(getActivity(), "Please enter a name and category", Toast.LENGTH_SHORT).show();
                } else {
                    //Update local and online storage
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    Date date = new Date();
                    String dateString = dateFormat.format(date);
                    Item newItem = new Item(name, category);
                    long itemId = db.logItems(newItem);
                    newItem.setId(itemId);
                    db.updateItem(newItem);
                    updateTable();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    public void openUpdateItemDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View subView = inflater.inflate(R.layout.fragment_update_item, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add an Item");
        builder.setView(subView);

        final EditText updateNameEditText = (EditText) subView.findViewById(R.id.updateNameEditText);
        final EditText updateCategoryEditText = (EditText) subView.findViewById(R.id.updateCategoryEditText);
        Button deleteButton = (Button) subView.findViewById(R.id.deleteItemButton);

        updateNameEditText.setText(selectedItem.getName());
        updateCategoryEditText.setText(selectedItem.getCategory());

        deleteButton.setOnClickListener(this);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = updateNameEditText.getText().toString();
                String category = updateCategoryEditText.getText().toString();

                if(name.trim().length() > 0 && category.trim().length() == 0) {
                    Toast.makeText(getActivity(), "Please enter a name and category", Toast.LENGTH_SHORT).show();
                } else {
                    //Update local and online storage
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    Date date = new Date();
                    String dateString = dateFormat.format(date);
                    selectedItem.setName(name);
                    selectedItem.setCategory(category);
                    db.updateItem(selectedItem);
                    selectedItem = null;
                    updateTable();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    public void updateTable() {
        mShoppingListTableLayout.removeAllViews();
        mItems = db.getAllItemRecords();
        getDataFromServer(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.d("jsonData", jsonData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        if(mItems.size() > 0) {
            for(int i = 0; i < mItems.size(); i++) {
                Item thisItem = mItems.get(i);
                TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.item_table_row, null);
                ((TextView) row.findViewById(R.id.nameTextView)).setText(thisItem.getName());
                ((TextView) row.findViewById(R.id.categoryTextView)).setText(thisItem.getCategory());
                mShoppingListTableLayout.addView(row);
                row.setTag(i);
                row.setOnClickListener(this);
            }
        }
    }

    public void getDataFromServer(Callback callback) {
        //Used OkHttp because it is somewhat familiar and seems to work just fine for this purpose
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        Request request = new Request.Builder()
                .header("X-CZ-Authorization", Constants.AUTH_TOKEN)
                .header("Accept", "application/json")
                .url(Constants.BASE_URL)
                .build();

        Call call = client.newCall(request);
        call.enqueue(callback);
    }
}
