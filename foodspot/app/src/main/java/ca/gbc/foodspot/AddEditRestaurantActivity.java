package ca.gbc.foodspot;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import ca.gbc.foodspot.db.DbHelper;
import ca.gbc.foodspot.model.Restaurant;

public class AddEditRestaurantActivity extends AppCompatActivity {

    private static final int REQ_AUTOCOMPLETE = 1001;

    private EditText editName, editAddress, editPhone, editPrice, editDistance, editTags, editDescription;
    private RatingBar ratingBar;
    private CheckBox checkFavorite;

    private DbHelper dbHelper;
    private Restaurant existing;

    private double selectedLat = 0.0;
    private double selectedLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_restaurant);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = DbHelper.getInstance(this);

        editName = findViewById(R.id.editName);
        editAddress = findViewById(R.id.editAddress);
        editPhone = findViewById(R.id.editPhone);
        ratingBar = findViewById(R.id.ratingBar);
        editPrice = findViewById(R.id.editPrice);
        editDistance = findViewById(R.id.editDistance);
        editTags = findViewById(R.id.editTags);
        editDescription = findViewById(R.id.editDescription);
        checkFavorite = findViewById(R.id.checkFavorite);
        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> save());

        Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(v -> finish());

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        editAddress.setFocusable(false);
        editAddress.setClickable(true);
        editAddress.setOnClickListener(v -> launchAddressAutocomplete());

        long id = getIntent().getLongExtra("restaurant_id", -1);
        if (id != -1) {
            existing = dbHelper.getRestaurantById(id);
            if (existing != null) fillFields();
            setTitle("Edit Restaurant");
        } else {
            setTitle("Add Restaurant");
        }

        buttonSave.setOnClickListener(v -> save());
    }

    private void fillFields() {
        editName.setText(existing.getName());
        editAddress.setText(existing.getAddress());
        editPhone.setText(existing.getPhone());
        ratingBar.setRating(existing.getRating());
        editPrice.setText(existing.getPriceLevel());
        editDistance.setText(existing.getDistanceText());
        editTags.setText(existing.getTags());
        editDescription.setText(existing.getDescription());
        checkFavorite.setChecked(existing.isFavorite());

        selectedLat = existing.getLatitude();
        selectedLng = existing.getLongitude();
    }

    private void launchAddressAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
        );

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(this);

        startActivityForResult(intent, REQ_AUTOCOMPLETE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                if (place.getAddress() != null) {
                    editAddress.setText(place.getAddress());
                }
                if (place.getLatLng() != null) {
                    selectedLat = place.getLatLng().latitude;
                    selectedLng = place.getLatLng().longitude;
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this,
                        "Address error: " + status.getStatusMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void save() {
        String name = editName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String address = editAddress.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        float rating = ratingBar.getRating();
        String price = editPrice.getText().toString().trim();
        String distance = editDistance.getText().toString().trim();
        String tags = editTags.getText().toString().trim();
        String desc = editDescription.getText().toString().trim();

        if (existing == null) {
            existing = new Restaurant();
        }

        existing.setName(name);
        existing.setAddress(address);
        existing.setPhone(phone);
        existing.setRating(rating);
        existing.setPriceLevel(price);
        existing.setDistanceText(distance);
        existing.setTags(tags);
        existing.setDescription(desc);
        existing.setFavorite(checkFavorite.isChecked());

        existing.setLatitude(selectedLat);
        existing.setLongitude(selectedLng);

        if (existing.getId() == 0) {
            long id = dbHelper.insertRestaurant(existing);
            existing.setId(id);
        } else {
            dbHelper.updateRestaurant(existing);
        }

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
