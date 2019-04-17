package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class DestinationActivity extends AppCompatActivity {

    ListView search_building;
    ArrayAdapter<String> adapter;
    TextView navigateMeTextView;
    String buildingButtons[] = new String [] {"Capen Library", "Lockwood Library", "Norton Hall", "Davis Hall", "Music Library"};
    SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_activity);

        search_building = findViewById(R.id.search_building);

        ArrayList<String> arrayBuildings = new ArrayList<String>();
        arrayBuildings.addAll(Arrays.asList(getResources().getStringArray(R.array.my_buildings)));

        adapter = new ArrayAdapter<String>(
                DestinationActivity.this,
                android.R.layout.simple_list_item_1,
                arrayBuildings
        );

        search_building.setAdapter(adapter);

        search_building.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(buildingButtons[position] == "Capen Library"){
                    Intent intent = new Intent(DestinationActivity.this, CapenFloor1Plan.class);
                    startActivity(intent);
                } else if(buildingButtons[position] == "Lockwood Library"){
                    Intent intent = new Intent(DestinationActivity.this, CapenFloor1Plan.class);
                    startActivity(intent);
                } else if(buildingButtons[position] == "Norton Hall"){
                    Intent intent = new Intent(DestinationActivity.this, CapenFloor1Plan.class);
                    startActivity(intent);
                } else if(buildingButtons[position] == "Davis Hall"){
                    Intent intent = new Intent(DestinationActivity.this, CapenFloor1Plan.class);
                    startActivity(intent);
                } else if(buildingButtons[position] == "Music Library"){
                    Intent intent = new Intent(DestinationActivity.this, CapenFloor1Plan.class);
                    startActivity(intent);
                }
            }
        });

        navigateMeTextView = findViewById(R.id.textView1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_building_menu, menu);
        MenuItem item = menu.findItem(R.id.search_building);
        searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Search Buildings...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}
