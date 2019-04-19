package com.example.opendataresource;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.opendataresource.fragments.TenDaysWeather;
import com.example.opendataresource.fragments.TodayWeatherFragment;
import com.example.opendataresource.fragments.TomorrowWeatherFragment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lapism.searchview.widget.SearchView;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import async.LoadJSON;
import async.TaskCompleted;

public class MainActivity extends AppCompatActivity implements TodayWeatherFragment.OnCompleteListener, TaskCompleted {
    public TextView data;
    public String json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new LoadJSON(this).execute();


        SearchView searchView = findViewById(R.id.search);




// Sets searchable configuration defined in searchable.xml for this SearchView
//        SearchManager searchManager =
//                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                Log.i("cos", query);
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String query) {
//                Log.i("cos", query);
//                return true;
//            }
//        });

        ViewPager mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);




    }


    private void setupViewPager(ViewPager mViewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new TodayWeatherFragment(), "Today");
        adapter.addFragment(new TomorrowWeatherFragment(), "Tomorrow");
        adapter.addFragment(new TenDaysWeather(), "10 Days");
        mViewPager.setAdapter(adapter);
    }

    @Override
    public void onComplete() {
        data = findViewById(R.id.json);
//        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String url = "https://www.trafforddatalab.io/open_data/defibrillators/trafford_defibrillators.json";

// Request a string response from the provided URL.
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Display the first 500 characters of the response string.
                        data.setText(response.toString());
                        Log.i("cos", response.toString());


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                data.setText("That didn't work!");
                error.printStackTrace();
            }
        });

// Add the request to the RequestQueue.
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public void onTaskComplete(String result) {
        this.json=result;
        Type type = new TypeToken<List<City>>() {}.getType();
        Gson gson = new GsonBuilder().registerTypeAdapter(type, new City.CitiesListDeserializer("London")).create();
        List<City> list = null;

        list = gson.fromJson(json, type);

        System.out.println(list);
    }
}
