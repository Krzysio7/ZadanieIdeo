package com.example.opendataresource;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.opendataresource.fragments.TodayWeatherFragment;
import com.example.opendataresource.fragments.TomorrowWeatherFragment;
import com.example.opendataresource.utils.InternetStatusListener;
import com.example.opendataresource.utils.NDSpinner;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lapism.searchview.Search;
import com.lapism.searchview.widget.SearchAdapter;
import com.lapism.searchview.widget.SearchItem;
import com.lapism.searchview.widget.SearchView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements TodayWeatherFragment.OnCompleteListener, InternetStatusListener.OnlineOrOffline {
    private SearchView searchView;
    private String queryText;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference mGetReference = database.getReference();
    private static List<SearchItem> mSuggestionsList = new ArrayList<>();
    private static SearchAdapter searchViewAdapter;
    private RecyclerView mRecyclerView;
    private TodayWeatherFragment todayWeatherFragment;
    private TomorrowWeatherFragment tomorrowWeatherFragment;
    private LocationManager locationManager;
    private ActionBarDrawerToggle t;
    private List<String> favourites = new ArrayList<>();
    private ArrayAdapter<String> favSpinnerAdapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth user;
    private FirebaseFirestore city = FirebaseFirestore.getInstance();
    private View offlineTextView;
    private Locale myLocale;
    final String[] language = {"PL", "EN"};
    private int check = 0;
    private String currentCity;
    private boolean first = true;
    private InternetStatusListener inter;
    private Timer timer;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            super.onResume();
        }
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = user.getInstance();
        inter = new InternetStatusListener();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(inter, filter);
        offlineTextView = findViewById(R.id.offlineView);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        searchView = findViewById(R.id.search);
        searchView.setLogoIcon(R.drawable.ic_my_location_black_24dp);
        drawerLayoutInit();
        navViewInit(language);
        ViewPager mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        openCloseListenerInit();
        geoLocationInit();


    }


    private void openCloseListenerInit() {
        searchView.setOnOpenCloseListener(new Search.OnOpenCloseListener() {
            @Override
            public void onOpen() {
                searchViewListenersInit();
            }

            @Override
            public void onClose() {

            }
        });
    }

    private void geoLocationInit() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        searchView.setOnLogoClickListener(new Search.OnLogoClickListener() {
            @Override
            public void onLogoClick() {

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


                } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(myIntent, 1);
                } else {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = gcd.getFromLocation(latitude, longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (addresses.size() > 0) {
                            final String add = addresses.get(0).getLocality();
                            todayWeatherFragment.getWeather(add);
                            tomorrowWeatherFragment.getWeather(add);
                            checkForDataUpdate();
                            currentCity = add;
                            if (!favourites.contains(add)) {

                                favourites.add(add);
                                addCitytoFavouritesDatabase(add);
                            }
                            favSpinnerAdapter.notifyDataSetChanged();
                        }
                    }

                    locationManager.requestSingleUpdate(
                            LocationManager.GPS_PROVIDER,

                            new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                }

                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {

                                }

                                @Override
                                public void onProviderEnabled(String provider) {

                                }

                                @Override
                                public void onProviderDisabled(String provider) {

                                }
                            }, null);

                }

            }
        });
    }

    private void navViewInit(final String[] language) {
        navigationView = findViewById(R.id.nv);
        final NDSpinner langSpinner = (NDSpinner) navigationView.getMenu().findItem(R.id.language).getActionView();
        TextView username = navigationView.getHeaderView(0).findViewById(R.id.userNameDrawer);
        username.setText(user.getCurrentUser().getEmail());
        final ArrayAdapter<String> langSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, language);
        langSpinner.setAdapter(langSpinnerAdapter);
        langSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (++check > 1) {
                    Toast.makeText(MainActivity.this, language[position], Toast.LENGTH_SHORT).show();

                    if (language[position].equals("PL")) {
                        setLocale("pl");
                        langSpinnerAdapter.notifyDataSetChanged();
                    } else if (language[position].equals("EN")) {
                        setLocale("en");
                        langSpinnerAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        NDSpinner favSpinner = (NDSpinner) navigationView.getMenu().findItem(R.id.favourites).getActionView();
        favSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, favourites);
        favSpinner.setAdapter(favSpinnerAdapter);
        retrieveFavouritesFromDatabase();
        favSpinnerAdapter.notifyDataSetChanged();
        favSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String favouriteName = favourites.get(position);
                todayWeatherFragment.getWeather(favouriteName);
                tomorrowWeatherFragment.getWeather(favouriteName);
                checkForDataUpdate();
                currentCity = favouriteName;
                addToSharedPref(favouriteName, null);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.language:

                        break;
                    case R.id.favourites:

                        break;
                    case R.id.logOutItem:
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        MainActivity.this.finish();
                        break;
                    default:
                        return true;
                }

                return true;
            }
        });
    }

    private void drawerLayoutInit() {
        drawerLayout = findViewById(R.id.drawer_layout);
        t = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        t.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(t);
        t.syncState();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    private ValueEventListener valueEventListenerHandle() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mSuggestionsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    SearchItem item = new SearchItem(getApplicationContext());
                    String name = (String) ds.child("name").getValue();
                    item.setTitle(name);
                    String country = (String) ds.child("country").getValue();
                    item.setSubtitle(country);
                    mSuggestionsList.add(item);
                }

                searchViewAdapter.setSuggestionsList(mSuggestionsList);
                searchView.setAdapter(searchViewAdapter);
                searchViewAdapter.getFilter().filter(queryText);
                mRecyclerView.setVisibility(View.VISIBLE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


    }


    private void setupViewPager(ViewPager mViewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        todayWeatherFragment = new TodayWeatherFragment();
        adapter.addFragment(todayWeatherFragment, getString(R.string.todayFragmentText));
        tomorrowWeatherFragment = new TomorrowWeatherFragment();
        adapter.addFragment(tomorrowWeatherFragment, getString(R.string.tomorrow));
        mViewPager.setAdapter(adapter);
    }


    public void searchViewListenersInit() {
        searchView.setOnOpenCloseListener(null);
        searchViewAdapter = new SearchAdapter(getBaseContext());
        searchViewAdapter.setSuggestionsList(mSuggestionsList);
        searchViewAdapter.setOnSearchItemClickListener(new SearchAdapter.OnSearchItemClickListener() {
            @Override
            public void onSearchItemClick(int position, CharSequence title, CharSequence subtitle) {
                todayWeatherFragment.getWeather(title);

                if (!favourites.contains(title.toString())) {

                    favourites.add(title.toString());
                    addCitytoFavouritesDatabase(title);
                    favSpinnerAdapter.notifyDataSetChanged();
                }
                tomorrowWeatherFragment.getWeather(title);
                checkForDataUpdate();
                currentCity = title.toString();
                searchView.close();
            }
        });

        searchView.setAdapter(searchViewAdapter);


        searchView.setOnQueryTextListener(new Search.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(CharSequence query) {
                return false;
            }

            @Override
            public void onQueryTextChange(CharSequence newText) {

                queryText = newText.toString();
                Query mGetQuery = mGetReference.orderByChild("name").startAt(queryText).limitToFirst(5);
                mGetQuery.addListenerForSingleValueEvent(valueEventListenerHandle());


            }
        });


    }

    private void addCitytoFavouritesDatabase(CharSequence title) {
        Map<String, Object> cityy = new HashMap<>();
        cityy.put("city", title.toString());
        cityy.put("user", user.getUid());
        city.collection("cities").add(cityy).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void retrieveFavouritesFromDatabase() {
        city.collection("cities")
                .whereEqualTo("user", user.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                favourites.add(document.getData().get("city").toString());
                                favSpinnerAdapter.notifyDataSetChanged();

                            }
                        }
                    }
                });
    }

    @Override
    public void onComplete() {
        mRecyclerView = findViewById(R.id.search_recyclerView);
        CharSequence location = getSharedPref(getString(R.string.saved_high_score_key));

        if (location != null) {

            currentCity = location.toString();


            searchView.setText(location);

        }

    }

    private CharSequence getSharedPref(String key) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(key, null);
    }


    @Override
    public void onOnline() {
        offlineTextView.setVisibility(View.GONE);
        todayWeatherFragment.getWeather(currentCity);
        tomorrowWeatherFragment.getWeather(currentCity);
    }

    @Override
    public void onOffline() {
        offlineTextView.setVisibility(View.VISIBLE);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(inter);
    }

    public void setLocale(String lang) {
        addToSharedPref(null, lang);
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
        finish();
    }


    public void onRadioButtonClicked(View view) {


        tomorrowWeatherFragment.onClickRadio(view);
        todayWeatherFragment.onClickRadio(view);
    }


    public void addToSharedPref(CharSequence location, String lang) {
        if (location != null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_high_score_key), location.toString());
            editor.apply();
        } else {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.lang_key), lang);
            editor.apply();

        }
    }

    private void checkForDataUpdate() {
        if (timer != null) {
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    todayWeatherFragment.getWeather(getSharedPref(getString(R.string.saved_high_score_key)));
                    tomorrowWeatherFragment.getWeather(getSharedPref(getString(R.string.saved_high_score_key)));
                }
            };
            timer.schedule(task, 0, 60000);

        }
    }

}
