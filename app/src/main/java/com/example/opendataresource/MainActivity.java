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
import com.google.android.gms.tasks.OnSuccessListener;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import static com.example.opendataresource.R.id.search_recyclerView;

public class MainActivity extends AppCompatActivity implements TodayWeatherFragment.OnCompleteListener, InternetStatusListener.OnlineOrOffline {

    @BindView(R.id.vSearch)
    SearchView searchView;
    @BindView(R.id.vPager)
    ViewPager mViewPager;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(search_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.vNavigation)
    NavigationView navigationView;
    @BindView(R.id.tvOfflineMode)
    TextView offlineTextView;

    private String queryText;
    private static List<SearchItem> suggestionsList = new ArrayList<>();
    private static SearchAdapter searchViewAdapter;

    private TodayWeatherFragment todayWeatherFragment;
    private TomorrowWeatherFragment tomorrowWeatherFragment;

    private LocationManager locationManager;
    private ActionBarDrawerToggle drawerToggle;
    private List<String> favourites = new ArrayList<>();
    private ArrayAdapter<String> favSpinnerAdapter;

    private FirebaseAuth user;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference dbReference = database.getReference();
    private FirebaseFirestore city = FirebaseFirestore.getInstance();

    private Locale appLocale;
    final String[] language = {"PL", "EN"};
    private int initialLangSpinnerCallCount = 0;
    private int initialFavSpinnerCount = 0;
    private String currentCity;
    private InternetStatusListener internetStatusListener;
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        user = user.getInstance();
        internetStatusListener = new InternetStatusListener();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(internetStatusListener, filter);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        drawerLayoutInit();
        navViewInit(language);

        setupViewPager(mViewPager);
        tabLayout.setupWithViewPager(mViewPager);

        geoLocationInit();
        openCloseListenerInit();
        searchView.setLogoIcon(R.drawable.ic_my_location_black_24dp);
    }

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

        searchView.setOnLogoClickListener(() -> {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(myIntent, 1);

            } else {

                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (location != null) {

                    decodeLocation(location);
                }

                locationManager.requestSingleUpdate(

                        LocationManager.GPS_PROVIDER,

                        getLocationListener(), null);
            }

        });
    }

    private LocationListener getLocationListener() {
        return new LocationListener() {
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
        };
    }

    private void decodeLocation(Location location) {

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

            final String cityDecoded = addresses.get(0).getLocality();
            todayWeatherFragment.getTodayWeather(cityDecoded);
            tomorrowWeatherFragment.getTomorrowWeather(cityDecoded);
            addToSharedPref(cityDecoded, null);
            updateSearchViewText(cityDecoded);
            checkForDataUpdate();
            currentCity = cityDecoded;

            if (!favourites.contains(cityDecoded)) {

                favourites.add(cityDecoded);
                addCityToFavouritesDatabase(cityDecoded);

            }
            favSpinnerAdapter.notifyDataSetChanged();
        }
    }

    private void updateSearchViewText(String cityDecoded) {
        searchView.setQuery(cityDecoded, false);
        searchView.clearFocus();
        searchView.close();
        recyclerView.setVisibility(View.GONE);
        suggestionsList.clear();

    }

    private void navViewInit(final String[] language) {

        langSpinnerInit(language);

        favSpinnerInit();


        navigationView.setNavigationItemSelectedListener(menuItem -> {

            int id = menuItem.getItemId();
            switch (id) {
                case R.id.itemLanguage:

                    break;
                case R.id.itemFavourites:

                    break;
                case R.id.itemLogOut:
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    MainActivity.this.finish();
                    break;
                default:
                    return true;
            }

            return true;
        });
    }

    private void favSpinnerInit() {
        NDSpinner favSpinner = (NDSpinner) navigationView.getMenu().findItem(R.id.itemFavourites).getActionView();
        favSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, favourites);
        favSpinner.setAdapter(favSpinnerAdapter);
        retrieveFavouritesFromDatabase();
        favSpinnerAdapter.notifyDataSetChanged();
        favSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (++initialFavSpinnerCount > 1) {
                    String favouriteName = favourites.get(position);
                    todayWeatherFragment.getTodayWeather(favouriteName);
                    tomorrowWeatherFragment.getTomorrowWeather(favouriteName);
                    checkForDataUpdate();
                    currentCity = favouriteName;
                    addToSharedPref(favouriteName, null);

                    updateSearchViewText(favouriteName);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void langSpinnerInit(String[] language) {

        final NDSpinner langSpinner = (NDSpinner) navigationView.getMenu().findItem(R.id.itemLanguage).getActionView();
        TextView username = navigationView.getHeaderView(0).findViewById(R.id.tvHeaderUsername);
        username.setText(user.getCurrentUser().getEmail());
        final ArrayAdapter<String> langSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, language);
        langSpinner.setAdapter(langSpinnerAdapter);
        langSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (++initialLangSpinnerCallCount > 1) {
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
    }

    private void drawerLayoutInit() {

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ValueEventListener valueEventListenerHandle() {
        return new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                suggestionsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    SearchItem item = new SearchItem(getApplicationContext());
                    String name = (String) ds.child("name").getValue();
                    item.setTitle(name);
                    String country = (String) ds.child("country").getValue();
                    item.setSubtitle(country);
                    suggestionsList.add(item);
                }

                searchViewAdapter.setSuggestionsList(suggestionsList);
                searchView.setAdapter(searchViewAdapter);
                searchViewAdapter.getFilter().filter(queryText);
                recyclerView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        todayWeatherFragment = new TodayWeatherFragment();
        adapter.addFragment(todayWeatherFragment, getString(R.string.todayFragmentText));
        tomorrowWeatherFragment = new TomorrowWeatherFragment();
        adapter.addFragment(tomorrowWeatherFragment, getString(R.string.tomorrow));
        viewPager.setAdapter(adapter);
    }


    public void searchViewListenersInit() {

        searchView.setOnOpenCloseListener(null);
        searchViewAdapter = new SearchAdapter(getBaseContext());
        searchViewAdapter.setSuggestionsList(suggestionsList);
        searchViewAdapter.setOnSearchItemClickListener((position, title, subtitle) -> {


            todayWeatherFragment.getTodayWeather(title);
            tomorrowWeatherFragment.getTomorrowWeather(title);
            if (!favourites.contains(title.toString())) {

                favourites.add(title.toString());
                addCityToFavouritesDatabase(title);
                favSpinnerAdapter.notifyDataSetChanged();

            }

            tomorrowWeatherFragment.getTomorrowWeather(title);
            checkForDataUpdate();
            currentCity = title.toString();
            addToSharedPref(currentCity, null);
            updateSearchViewText(currentCity);

        });

        searchView.setAdapter(searchViewAdapter);
        updateSearchViewText(currentCity);
        searchViewAdapter.notifyDataSetChanged();


        searchView.setOnQueryTextListener(new Search.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(CharSequence query) {

                return false;
            }

            @Override
            public void onQueryTextChange(CharSequence newText) {
                suggestionsList.clear();

                if (getSharedPref(getString(R.string.saved_high_score_key)) == null ||
                        !newText.toString().equals(getSharedPref(getString(R.string.saved_high_score_key)).toString())) {

                    queryText = newText.toString();
                    Query mGetQuery = dbReference.orderByChild("name").startAt(queryText).limitToFirst(5);
                    mGetQuery.addListenerForSingleValueEvent(valueEventListenerHandle());
                }
            }
        });


    }

    private void addCityToFavouritesDatabase(CharSequence title) {
        Map<String, Object> favCityMap = new HashMap<>();
        favCityMap.put("city", title.toString());
        favCityMap.put("user", user.getUid());
        city.collection("cities").add(favCityMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {


            @Override
            public void onSuccess(DocumentReference documentReference) {


            }
        }).addOnFailureListener(e -> {

        });
    }

    private void retrieveFavouritesFromDatabase() {
        city.collection("cities")
                .whereEqualTo("user", user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            favourites.add(document.getData().get("city").toString());
                            favSpinnerAdapter.notifyDataSetChanged();

                        }
                    }
                });
    }

    @Override
    public void onComplete() {

        CharSequence location = getSharedPref(getString(R.string.saved_high_score_key));

        if (location != null) {

            currentCity = location.toString();

            updateSearchViewText(currentCity);
        }

    }

    public CharSequence getSharedPref(String key) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(key, null);
    }

    @Override
    public void onOnline() {
        offlineTextView.setVisibility(View.GONE);
        if (currentCity != null) {
            todayWeatherFragment.getTodayWeather(currentCity);
            tomorrowWeatherFragment.getTomorrowWeather(currentCity);
        }
    }

    @Override
    public void onOffline() {
        offlineTextView.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(internetStatusListener);
    }

    public void setLocale(String lang) {
        addToSharedPref(null, lang);
        appLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(appLocale);
        res.updateConfiguration(conf, displayMetrics);
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
        finish();
    }

    @Optional
    @OnClick({R.id.radioC, R.id.radioF})
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
                    todayWeatherFragment.getTodayWeather(getSharedPref(getString(R.string.saved_high_score_key)));
                    tomorrowWeatherFragment.getTomorrowWeather(getSharedPref(getString(R.string.saved_high_score_key)));
                }
            };

            timer.schedule(task, 0, 60000);

        }
    }

}
