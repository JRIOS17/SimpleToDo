package com.example.simpletodo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import org.apache.commons.io.FileUtils;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items;

    Button addButton;
    EditText editTextBox;
    RecyclerView rvItems;
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.addButton);
        editTextBox = findViewById(R.id.editTextBox);
        rvItems = findViewById(R.id.itemList);

        loadItems();
        /*
        items = new ArrayList<>();
        items.add("Buy Milk");
        items.add("Go to gym");
        items.add("Have Fun!");
        */
        ItemsAdapter.OnLongClickListener onLongClickListener = new ItemsAdapter.OnLongClickListener(){
            @Override
            public void onItemLongClick(int position) {
                //Delete the item from the model
                String removedItem = items.get(position);
                items.remove(position);
                //Notify the adapter at which position we deleted the item
                itemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), removedItem + " was removed", Toast.LENGTH_SHORT).show();
                writeItems();
            }
        };

        ItemsAdapter.OnClickListener clickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.d("MainActivity", "Single Click at position: " + position);
                //create the new edit activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);

                //pass in the relevant data to the other activity
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);

                //display the edit activity
                startActivityForResult(i, EDIT_TEXT_CODE);
            }
        };

        itemsAdapter = new ItemsAdapter(items, onLongClickListener, clickListener);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoItem = editTextBox.getText().toString();
                //Add the item to the model
                items.add(todoItem);
                //notify the adapter that we've added an item
                itemsAdapter.notifyItemInserted(items.size()-1);
                editTextBox.setText("");
                Toast.makeText(getApplicationContext(), todoItem + " was added", Toast.LENGTH_SHORT).show();
                writeItems();
            }
        });
    }

    //to handle the result of the edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            //retrieve updated text value
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);

            //extract the original position of the edited item
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);

            //Update the model with the new text
            items.set(position, itemText);

            //notify the adapter so that they know the recycler change
            itemsAdapter.notifyItemChanged(position);

            //persist the changes
            writeItems();
            Toast.makeText(getApplicationContext(), "Item Updated", Toast.LENGTH_SHORT).show();

        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

    private File getDataFile(){
        return new File(getFilesDir(), "data.txt");
    }

    //will load items by reading data from our 'data.txt' file
    private void loadItems(){
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading items", e);
            items = new ArrayList<>();
        }
    }

    //will write items onto our data.txt file
    private void writeItems(){
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e("Main Activity", "Error Writing items", e);
        }
    }
}