package com.epicodus.carezoneshoppingapp;

/*TODO:
    Create put and delete requests to server
    Maybe pull to refresh
    Make id in sqlite database reflect id assigned to item on server
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShoppingListFragment extends Fragment implements View.OnClickListener {

    /*Globals*/

    @Bind(R.id.addItemButton) Button mAddItemButton;
    @Bind(R.id.shoppingListTableLayout) TableLayout mShoppingListTableLayout;

    private DatabaseHelper db;

    private Item selectedItem;
    private List<Item> mItems;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    /*Create View*/

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
        getDataFromServer();
    }

    /*Click Listeners*/

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
            default:
                break;
        }
    }

    /*Create Dialog Functions*/

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

                if(name.trim().length() == 0 || category.trim().length() == 0) {
                    Toast.makeText(getActivity(), "Please enter a name and category", Toast.LENGTH_SHORT).show();
                } else {
                    Item newItem = new Item(name, category);
                    long itemId = db.logItems(newItem);
                    newItem.setId(itemId);
                    String jsonString = makeJSONItem(newItem);
                    postToServer(jsonString);
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

    /*Get data and update view*/

    public void getDataFromServer() {
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
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mItems = db.getAllItemRecords();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTable();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                Log.d("jsonData", jsonData);
                db.deleteAllItemRecords();
                mItems = processJSON(jsonData);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTable();
                    }
                });
            }
        });
    }

    public void updateTable() {
        mShoppingListTableLayout.removeAllViews();
        Log.d("deleting", mItems.size()+"");

        if(mItems.size() > 0) {
            for(int i = 0; i < mItems.size(); i++) {
                Item thisItem = mItems.get(i);
                long itemId = db.logItems(thisItem);
                thisItem.setId(itemId);
                db.updateItem(thisItem);
                Log.d("updatedItemId", thisItem.getServerId()+"");
                TableRow row = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.item_table_row, null);
                ((TextView) row.findViewById(R.id.nameTextView)).setText(thisItem.getName());
                ((TextView) row.findViewById(R.id.categoryTextView)).setText(thisItem.getCategory());
                mShoppingListTableLayout.addView(row);
                row.setTag(i);
                row.setOnClickListener(this);
            }
        }
    }

    public ArrayList<Item> processJSON(String jsonString) {
        ArrayList<Item> items = new ArrayList<>();
        try {
            JSONArray responseJSON = new JSONArray(jsonString);
            for(int i = 0; i < responseJSON.length(); i++) {
                JSONObject itemObject = responseJSON.getJSONObject(i);
                String itemName = itemObject.getString("name");
                String itemCategory = itemObject.getString("category");
                long serverId = itemObject.getLong("id");
                Item item = new Item(itemName, itemCategory);
                item.setServerId(serverId);
                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }

    /*Post Data*/

    public String makeJSONItem(Item item) {
        JSONObject jsonItem = new JSONObject();
        JSONObject itemObject = new JSONObject();

        try {
            itemObject.put("name", item.getName());
            itemObject.put("category", item.getCategory());
            jsonItem.put("item", itemObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("item", jsonItem.toString());
        String jsonString = jsonItem.toString();
        return jsonString;
    }

    public void postToServer(String jsonString) {
        RequestBody body = RequestBody.create(JSON, jsonString);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        Request request = new Request.Builder()
                .header("X-CZ-Authorization", Constants.AUTH_TOKEN)
                .header("Accept", "application/json")
                .header("Content-type", "application/json")
                .url(Constants.BASE_URL)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                Log.d("PostData", jsonData);
                getDataFromServer();
            }
        });
    }
}
