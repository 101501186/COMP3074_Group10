package ca.gbc.foodspot;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import ca.gbc.foodspot.db.DbHelper;
import ca.gbc.foodspot.model.Restaurant;

public class RestaurantDetailActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private Restaurant restaurant;

    private TextView textName, textMeta, textAddress, textDistance,
            textDescription, textPhone, textTags;
    private CheckBox checkFavorite;

    private RatingBar detailRatingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        dbHelper = DbHelper.getInstance(this);

        ImageView image = findViewById(R.id.imageRestaurant);
        detailRatingBar = findViewById(R.id.detailRatingBar);
        textName = findViewById(R.id.textName);
        textMeta = findViewById(R.id.textMeta);
        textAddress = findViewById(R.id.textAddress);
        textDistance = findViewById(R.id.textDistance);
        textDescription = findViewById(R.id.textDescription);
        textPhone = findViewById(R.id.textPhone);
        textTags = findViewById(R.id.textTags);
        checkFavorite = findViewById(R.id.checkFavorite);
        Button buttonMap = findViewById(R.id.buttonMap);
        Button buttonShare = findViewById(R.id.buttonShare);
        Button buttonEdit = findViewById(R.id.buttonEdit);
        Button buttonDelete = findViewById(R.id.buttonDelete);

        detailRatingBar.setRating(restaurant.getRating());

        image.setImageResource(R.drawable.ic_restaurant_placeholder);

        long id = getIntent().getLongExtra("restaurant_id", -1);
        if (id == -1) {
            finish();
            return;
        }

        restaurant = dbHelper.getRestaurantById(id);
        if (restaurant == null) {
            finish();
            return;
        }

        setTitle("Restaurant Details");
        bindData();

        checkFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            restaurant.setFavorite(isChecked);
            dbHelper.updateRestaurant(restaurant);
        });

        buttonMap.setOnClickListener(v -> openInMaps());
        buttonShare.setOnClickListener(v -> share());
        buttonEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, AddEditRestaurantActivity.class);
            i.putExtra("restaurant_id", restaurant.getId());
            startActivity(i);
        });
        buttonDelete.setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (restaurant != null) {
            restaurant = dbHelper.getRestaurantById(restaurant.getId());
            if (restaurant != null) bindData();
        }
    }

    private void bindData() {
        textName.setText(restaurant.getName());
        String meta = restaurant.getRating() + " • " + restaurant.getPriceLevel();
        textMeta.setText(meta);
        textAddress.setText(restaurant.getAddress());
        textDistance.setText(restaurant.getDistanceText());
        textDescription.setText(restaurant.getDescription());
        textPhone.setText(restaurant.getPhone());
        textTags.setText(restaurant.getTags());
        checkFavorite.setChecked(restaurant.isFavorite());
    }

    private void openInMaps() {
        String query = restaurant.getName() + " " + restaurant.getAddress();
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

    private void share() {
        String text = restaurant.getName() + "\n" +
                restaurant.getAddress() + "\n" +
                "Phone: " + restaurant.getPhone() + "\n" +
                "Rating: " + restaurant.getRating() + " " + restaurant.getPriceLevel() + "\n" +
                "Tags: " + restaurant.getTags() + "\n\n" +
                restaurant.getDescription();

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Restaurant: " + restaurant.getName());
        i.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(i, "Share via"));
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Delete " + restaurant.getName() + "?")
                .setPositiveButton("Delete", (DialogInterface dialog, int which) -> {
                    dbHelper.deleteRestaurant(restaurant.getId());
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
