package ca.gbc.foodspot;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ca.gbc.foodspot.db.DbHelper;
import ca.gbc.foodspot.model.Restaurant;
import ca.gbc.foodspot.ui.RestaurantAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private SearchView searchView;
    private RestaurantAdapter adapter;
    private DbHelper dbHelper;

    private boolean filterFavorites;
    private boolean filterReviews;
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DbHelper.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Restaurants");

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(this::onBottomNavSelected);
        bottomNav.setSelectedItemId(R.id.nav_home);

        RecyclerView recycler = findViewById(R.id.recyclerRestaurants);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RestaurantAdapter(
                restaurant -> {
                    Intent i = new Intent(MainActivity.this, RestaurantDetailActivity.class);
                    i.putExtra("restaurant_id", restaurant.getId());
                    startActivity(i);
                },
                (restaurant, isFavorite) -> {
                    restaurant.setFavorite(isFavorite);
                    dbHelper.updateRestaurant(restaurant);
                    loadData();
                }
        );
        recycler.setAdapter(adapter);

        searchView = findViewById(R.id.searchView);
        setupSearch();

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AddEditRestaurantActivity.class);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                loadData();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                loadData();
                return true;
            }
        });
    }

    private void loadData() {
        List<Restaurant> list =
                dbHelper.getRestaurants(filterFavorites, filterReviews, currentQuery);
        adapter.setItems(list);
    }

    private boolean onBottomNavSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            return true;
        } else if (id == R.id.nav_map) {
            startActivity(new Intent(this, MapActivity.class));
            return true;
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.drawer_home) {
            filterFavorites = false;
            filterReviews = false;
        } else if (id == R.id.drawer_favorites) {
            filterFavorites = true;
            filterReviews = false;
        } else if (id == R.id.drawer_my_reviews) {
            filterFavorites = false;
            filterReviews = true;
        } else if (id == R.id.drawer_settings) {
            // stub
        } else if (id == R.id.drawer_help) {
            // stub
        }

        loadData();
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
