package ca.gbc.foodspot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import ca.gbc.foodspot.db.DbHelper;
import ca.gbc.foodspot.model.Restaurant;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";
    private static final int REQ_PLACE_SEARCH = 2001;

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private DbHelper dbHelper;

    private GoogleMap googleMap;
    private Restaurant restaurant;
    private Button buttonDirections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbHelper = DbHelper.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Map");

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navView = findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(this::onBottomNavSelected);
        bottomNav.setSelectedItemId(R.id.nav_map);

        long id = getIntent().getLongExtra(EXTRA_RESTAURANT_ID, -1);
        if (id != -1) {
            restaurant = dbHelper.getRestaurantById(id);
        }

        if (restaurant != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(restaurant.getName());
        }

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        buttonDirections = findViewById(R.id.buttonDirections);
        if (buttonDirections != null) {
            if (restaurant != null) {
                buttonDirections.setEnabled(true);
                buttonDirections.setAlpha(1f);
                buttonDirections.setOnClickListener(v -> openDirections());
            } else {
                buttonDirections.setEnabled(false);
                buttonDirections.setAlpha(0.4f);
                buttonDirections.setOnClickListener(v ->
                        Toast.makeText(
                                this,
                                "Tap a restaurant pin or open a restaurant and choose \"View on Map\".",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Single-restaurant mode (opened from details)
        if (restaurant != null) {
            double lat = restaurant.getLatitude();
            double lng = restaurant.getLongitude();

            if (lat == 0.0 && lng == 0.0) {
                Toast.makeText(this,
                        "No coordinates saved for this restaurant. Directions will use address.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            LatLng position = new LatLng(lat, lng);
            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(restaurant.getName()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16f));
            return;
        }

        // Generic map mode – show all saved restaurants
        LatLng toronto = new LatLng(43.6532, -79.3832);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 12f));

        List<Restaurant> list = dbHelper.getRestaurants(false, false, "");
        if (list.isEmpty()) {
            Toast.makeText(this, "No saved restaurants to show on map.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Restaurant r : list) {
            double lat = r.getLatitude();
            double lng = r.getLongitude();
            if (lat == 0.0 && lng == 0.0) continue;

            LatLng pos = new LatLng(lat, lng);
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(r.getName())
                    .snippet(r.getAddress()));

            if (marker != null) {
                marker.setTag(r);
            }
        }

        // When user taps a pin, select that restaurant and enable directions
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Restaurant) {
                restaurant = (Restaurant) tag;

                if (buttonDirections != null) {
                    buttonDirections.setEnabled(true);
                    buttonDirections.setAlpha(1f);
                    buttonDirections.setOnClickListener(v -> openDirections());
                }

                marker.showInfoWindow();
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search_place) {
            launchPlaceSearch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchPlaceSearch() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER,
                Place.Field.LAT_LNG
        );

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setCountry("CA")
                .build(this);

        startActivityForResult(intent, REQ_PLACE_SEARCH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PLACE_SEARCH) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                onPlaceSelectedFromMap(place);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onPlaceSelectedFromMap(Place place) {
        if (googleMap != null && place.getLatLng() != null) {
            googleMap.clear();
            LatLng pos = place.getLatLng();
            googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(place.getName())
                    .snippet(place.getAddress()));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f));
        }

        new AlertDialog.Builder(this)
                .setTitle("Add to My Restaurants?")
                .setMessage("Do you want to save \"" + place.getName() + "\" to your list?")
                .setPositiveButton("Add", (dialog, which) -> savePlaceAsRestaurant(place))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void savePlaceAsRestaurant(Place place) {
        Restaurant r = new Restaurant();
        r.setName(place.getName());
        r.setAddress(place.getAddress());
        r.setPhone(place.getPhoneNumber());
        r.setRating(0f);
        r.setPriceLevel("");
        r.setDistanceText("");
        r.setTags("");
        r.setDescription("");
        r.setFavorite(false);

        if (place.getLatLng() != null) {
            r.setLatitude(place.getLatLng().latitude);
            r.setLongitude(place.getLatLng().longitude);
        }

        long id = dbHelper.insertRestaurant(r);
        r.setId(id);

        Toast.makeText(this, "Saved to My Restaurants", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void openDirections() {
        if (restaurant == null) return;

        String uriString;

        double lat = restaurant.getLatitude();
        double lng = restaurant.getLongitude();

        if (lat != 0.0 && lng != 0.0) {
            uriString = "google.navigation:q=" + lat + "," + lng;
        } else {
            String query = restaurant.getName() + " " + restaurant.getAddress();
            uriString = "google.navigation:q=" + Uri.encode(query);
        }

        Uri uri = Uri.parse(uriString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

    private boolean onBottomNavSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        } else if (id == R.id.nav_map) {
            return true;
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        if (item.getItemId() == R.id.drawer_home) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (item.getItemId() == R.id.drawer_favorites) {
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("filter_favorites", true));
        } else if (item.getItemId() == R.id.drawer_my_reviews) {
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("filter_reviews", true));
        }
        return true;
    }
}
