package ca.gbc.foodspot;

import android.content.DialogInterface;
import android.content.Intent;
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
        try {
            setContentView(R.layout.activity_restaurant_detail);

            // Enable toolbar back button
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }

                toolbar.setNavigationOnClickListener(v -> finish());
            }

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
            TextView buttonMap = findViewById(R.id.buttonMap);
            Button buttonShare = findViewById(R.id.buttonShare);
            Button buttonEdit = findViewById(R.id.buttonEdit);
            Button buttonDelete = findViewById(R.id.buttonDelete);

            if (image != null) {
                image.setImageResource(R.drawable.ic_restaurant_placeholder);
            }

            long id = getIntent().getLongExtra("restaurant_id", -1);
            if (id == -1 || id == 0) {
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

            if (checkFavorite != null) {
                checkFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (restaurant != null) {
                        restaurant.setFavorite(isChecked);
                        dbHelper.updateRestaurant(restaurant);
                    }
                });
            }

            if (detailRatingBar != null) {
                detailRatingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
                    if (!fromUser || restaurant == null) return;

                    restaurant.setRating(rating);
                    dbHelper.updateRestaurant(restaurant);

                    String priceLevel = restaurant.getPriceLevel() != null ? restaurant.getPriceLevel() : "";
                    String meta = rating + (priceLevel.isEmpty() ? "" : " • " + priceLevel);
                    if (textMeta != null) {
                        textMeta.setText(meta);
                    }

                    Toast.makeText(this, "Rating updated", Toast.LENGTH_SHORT).show();
                });
            }

            if (buttonMap != null) {
                buttonMap.setOnClickListener(v -> openMapScreen());
            }
            if (buttonShare != null) {
                buttonShare.setOnClickListener(v -> share());
            }
            if (buttonEdit != null) {
                buttonEdit.setOnClickListener(v -> {
                    if (restaurant != null) {
                        Intent i = new Intent(this, AddEditRestaurantActivity.class);
                        i.putExtra("restaurant_id", restaurant.getId());
                        startActivity(i);
                    }
                });
            }
            if (buttonDelete != null) {
                buttonDelete.setOnClickListener(v -> confirmDelete());
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
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
        if (restaurant == null) return;
        
        if (textName != null) {
            textName.setText(restaurant.getName() != null ? restaurant.getName() : "");
        }
        
        String priceLevel = restaurant.getPriceLevel() != null ? restaurant.getPriceLevel() : "";
        double rating = restaurant.getRating() > 0 ? restaurant.getRating() : 0.0;
        String meta = rating + (priceLevel.isEmpty() ? "" : " • " + priceLevel);
        
        if (textMeta != null) {
            textMeta.setText(meta);
        }
        
        if (textAddress != null) {
            textAddress.setText(restaurant.getAddress() != null ? restaurant.getAddress() : "");
        }
        
        String distance = restaurant.getDistanceText() != null ? restaurant.getDistanceText() : "";
        if (textDistance != null) {
            textDistance.setText(distance);
        }
        
        if (textDescription != null) {
            textDescription.setText(restaurant.getDescription() != null ? restaurant.getDescription() : "");
        }
        
        if (textPhone != null) {
            textPhone.setText(restaurant.getPhone() != null ? restaurant.getPhone() : "");
        }
        
        if (textTags != null) {
            textTags.setText(restaurant.getTags() != null ? restaurant.getTags() : "");
        }
        
        if (checkFavorite != null) {
            checkFavorite.setChecked(restaurant.isFavorite());
        }

        if (detailRatingBar != null) {
            detailRatingBar.setRating((float) rating);
        }
    }

    private void openMapScreen() {
        if (restaurant == null) return;
        
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_RESTAURANT_ID, restaurant.getId());
        startActivity(intent);
    }

    private void share() {
        if (restaurant == null) return;
        
        String name = restaurant.getName() != null ? restaurant.getName() : "";
        String address = restaurant.getAddress() != null ? restaurant.getAddress() : "";
        String phone = restaurant.getPhone() != null ? restaurant.getPhone() : "";
        String priceLevel = restaurant.getPriceLevel() != null ? restaurant.getPriceLevel() : "";
        String tags = restaurant.getTags() != null ? restaurant.getTags() : "";
        String description = restaurant.getDescription() != null ? restaurant.getDescription() : "";
        
        String text = name + "\n" +
                address + "\n" +
                "Phone: " + phone + "\n" +
                "Rating: " + restaurant.getRating() + " " + priceLevel + "\n" +
                "Tags: " + tags + "\n\n" +
                description;

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Restaurant: " + name);
        i.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(i, "Share via"));
    }

    private void confirmDelete() {
        if (restaurant == null) return;
        
        String name = restaurant.getName() != null ? restaurant.getName() : "this restaurant";
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Delete " + name + "?")
                .setPositiveButton("Delete", (DialogInterface dialog, int which) -> {
                    if (restaurant != null) {
                        dbHelper.deleteRestaurant(restaurant.getId());
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
