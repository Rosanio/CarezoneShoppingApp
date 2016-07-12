package com.epicodus.carezoneshoppingapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ShoppingListFragment extends Fragment implements View.OnClickListener {
    @Bind(R.id.addItemButton) Button mAddItemButton;
    @Bind(R.id.shoppingListTableLayout) TableLayout mShoppingListTableLayout;

    private DatabaseHelper db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_list, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mAddItemButton.setOnClickListener(this);
        db = new DatabaseHelper(getActivity());
        updateTable();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.addItemButton:
                openAddItemDialog();
                break;
            default:
                break;
        }
    }

    public void openAddItemDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View subView = inflater.inflate(R.layout.fragment_add_item, null);
        Log.d("its", "working");

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
                    Item newItem = new Item(name, category, dateString, 1);
                    db.logItems(newItem);
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
        List<Item> items = db.getAllItemRecords();
        if(items.size() > 0) {
            for(int i = 0; i < items.size(); i++) {
                Item thisItem = items.get(i);
                TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.item_table_row, null);
                ((TextView) row.findViewById(R.id.nameTextView)).setText(thisItem.getName());
                ((TextView) row.findViewById(R.id.categoryTextView)).setText(thisItem.getCategory());
                mShoppingListTableLayout.addView(row);
                Toast.makeText(getActivity(), "It works! " + thisItem.getName() + " " + thisItem.getCategory() , Toast.LENGTH_SHORT).show();
            }
        }
    }
}
