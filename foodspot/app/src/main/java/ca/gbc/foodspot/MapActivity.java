package ca.gbc.foodspot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import ca.gbc.foodspot.db.DbHelper;
import ca.gbc.foodspot.model.Restaurant;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private DbHelper dbHelper;

    private GoogleMap googleMap;
    private Restaurant restaurant;

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
        if (restaurant == null) {
            List<Restaurant> list = dbHelper.getRestaurants(false, false, "");
            if (!list.isEmpty()) {
                restaurant = list.get(0);
            }
        }

        if (restaurant != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(restaurant.getName());
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button buttonDirections = findViewById(R.id.buttonDirections);
        buttonDirections.setOnClickListener(v -> openDirections());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        if (restaurant == null) {
            Toast.makeText(this, "No restaurant to show on map.", Toast.LENGTH_SHORT).show();
            return;
        }

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
