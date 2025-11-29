package ca.gbc.foodspot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class AboutActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("About Us");

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
        bottomNav.setSelectedItemId(R.id.nav_about);

        setupMember(R.id.textMember1Email, "Henrique.custodio@georgebrown.ca");
        setupMember(R.id.textMember2Email, "Tyson.ward-dicks@georgebrown.ca");
        setupMember(R.id.textMember3Email, "Hossein.khanzadeh@georgebrown.ca");
    }

    private void setupMember(int id, String email) {
        TextView tv = findViewById(id);
        tv.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SENDTO);
            i.setData(Uri.parse("mailto:" + email));
            startActivity(i);
        });
    }

    private boolean onBottomNavSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        } else if (id == R.id.nav_map) {
            startActivity(new Intent(this, MapActivity.class));
            return true;
        } else if (id == R.id.nav_about) {
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
