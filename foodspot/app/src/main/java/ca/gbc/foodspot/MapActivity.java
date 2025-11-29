package ca.gbc.foodspot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import ca.gbc.foodspot.db.DbHelper;
import ca.gbc.foodspot.model.Restaurant;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private DbHelper dbHelper;

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

        Button buttonDirections = findViewById(R.id.buttonDirections);
        buttonDirections.setOnClickListener(v -> openDirections());
    }

    private void openDirections() {
        List<Restaurant> list = dbHelper.getRestaurants(false, false, "");
        if (list.isEmpty()) return;
        Restaurant first = list.get(0);
        String query = first.getName() + " " + first.getAddress();
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
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
        // For this assignment, main filtering is controlled from MainActivity
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
