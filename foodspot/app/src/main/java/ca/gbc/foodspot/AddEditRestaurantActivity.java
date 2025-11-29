package ca.gbc.foodspot;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import ca.gbc.foodspot.db.DbHelper;
import ca.gbc.foodspot.model.Restaurant;

public class AddEditRestaurantActivity extends AppCompatActivity {

    private EditText editName, editAddress, editPhone,
            editRating, editPrice, editDistance, editTags, editDescription;
    private CheckBox checkFavorite;

    private DbHelper dbHelper;
    private Restaurant existing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_restaurant);

        dbHelper = DbHelper.getInstance(this);

        editName = findViewById(R.id.editName);
        editAddress = findViewById(R.id.editAddress);
        editPhone = findViewById(R.id.editPhone);
        editRating = findViewById(R.id.editRating);
        editPrice = findViewById(R.id.editPrice);
        editDistance = findViewById(R.id.editDistance);
        editTags = findViewById(R.id.editTags);
        editDescription = findViewById(R.id.editDescription);
        checkFavorite = findViewById(R.id.checkFavorite);
        Button buttonSave = findViewById(R.id.buttonSave);

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
        editRating.setText(String.valueOf(existing.getRating()));
        editPrice.setText(existing.getPriceLevel());
        editDistance.setText(existing.getDistanceText());
        editTags.setText(existing.getTags());
        editDescription.setText(existing.getDescription());
        checkFavorite.setChecked(existing.isFavorite());
    }

    private void save() {
        String name = editName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String address = editAddress.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String ratingText = editRating.getText().toString().trim();
        String price = editPrice.getText().toString().trim();
        String distance = editDistance.getText().toString().trim();
        String tags = editTags.getText().toString().trim();
        String desc = editDescription.getText().toString().trim();

        float rating = 0f;
        if (!TextUtils.isEmpty(ratingText)) {
            try {
                rating = Float.parseFloat(ratingText);
            } catch (NumberFormatException ignored) {}
        }

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


