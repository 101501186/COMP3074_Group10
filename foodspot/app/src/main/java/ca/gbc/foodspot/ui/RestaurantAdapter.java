package ca.gbc.foodspot.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import ca.gbc.foodspot.R;
import ca.gbc.foodspot.model.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Restaurant restaurant);
    }

    public interface OnFavoriteToggleListener {
        void onFavoriteToggle(Restaurant restaurant, boolean isFavorite);
    }

    private List<Restaurant> items = new ArrayList<>();
    private final OnItemClickListener clickListener;
    private final OnFavoriteToggleListener favListener;

    public RestaurantAdapter(OnItemClickListener clickListener,
                             OnFavoriteToggleListener favListener) {
        this.clickListener = clickListener;
        this.favListener = favListener;
    }

    public void setItems(List<Restaurant> list) {
        android.util.Log.d("RestaurantAdapter", "setItems called with " + (list != null ? list.size() : 0) + " items");
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
        android.util.Log.d("RestaurantAdapter", "After notifyDataSetChanged, itemCount: " + getItemCount());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        android.util.Log.d("RestaurantAdapter", "onBindViewHolder called for position: " + position + ", items size: " + items.size());
        if (position < 0 || position >= items.size()) {
            android.util.Log.w("RestaurantAdapter", "Invalid position: " + position);
            return;
        }
        Restaurant r = items.get(position);
        if (r != null) {
            holder.bind(r, clickListener, favListener);
        } else {
            android.util.Log.w("RestaurantAdapter", "Restaurant is null at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageThumb;
        TextView textName, textAddress, textMeta, textTags;
        ImageButton buttonFavorite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumb = itemView.findViewById(R.id.imageThumb);
            textName = itemView.findViewById(R.id.textName);
            textAddress = itemView.findViewById(R.id.textAddress);
            textMeta = itemView.findViewById(R.id.textMeta);
            textTags = itemView.findViewById(R.id.textTags);
            buttonFavorite = itemView.findViewById(R.id.buttonFavorite);

            itemView.setClickable(true);
            itemView.setFocusable(true);
        }

        void bind(Restaurant r,
                  OnItemClickListener clickListener,
                  OnFavoriteToggleListener favListener) {
            android.util.Log.d("RestaurantAdapter", "Binding restaurant: " + (r != null ? r.getName() + " (ID: " + r.getId() + ")" : "null"));

            itemView.setOnClickListener(null);

            itemView.setOnClickListener(v -> {
                try {
                    android.util.Log.d("RestaurantAdapter", "Card clicked!");
                    android.util.Log.d("RestaurantAdapter", "Restaurant: " + (r != null ? r.getName() : "null"));
                    android.util.Log.d("RestaurantAdapter", "ClickListener: " + (clickListener != null ? "not null" : "null"));
                    if (r != null && clickListener != null) {
                        android.util.Log.d("RestaurantAdapter", "Calling clickListener with restaurant ID: " + r.getId());
                        clickListener.onItemClick(r);
                    } else {
                        android.util.Log.d("RestaurantAdapter", "Skipping click - restaurant: " + (r != null) + ", listener: " + (clickListener != null));
                    }
                } catch (Exception e) {
                    android.util.Log.e("RestaurantAdapter", "Error in click listener", e);
                    e.printStackTrace();
                }
            });
            
            imageThumb.setImageResource(R.drawable.ic_restaurant_placeholder);
            textName.setText(r.getName() != null ? r.getName() : "");
            textAddress.setText(r.getAddress() != null ? r.getAddress() : "");
            String priceLevel = r.getPriceLevel() != null ? r.getPriceLevel() : "";
            String distance = r.getDistanceText() != null ? r.getDistanceText() : "";
            double rating = r.getRating() > 0 ? r.getRating() : 0.0;
            String meta = rating + 
                    (priceLevel.isEmpty() ? "" : " • " + priceLevel) +
                    (distance.isEmpty() ? "" : " • " + distance);
            textMeta.setText(meta);
            textTags.setText(r.getTags() != null ? r.getTags() : "");

            // Update heart icon based on favorite status
            if (r.isFavorite()) {
                buttonFavorite.setImageResource(R.drawable.ic_heart_filled);
                buttonFavorite.clearColorFilter();
            } else {
                buttonFavorite.setImageResource(R.drawable.ic_heart_outline);
                buttonFavorite.clearColorFilter();
            }

            // Set click listener for heart button - consume event to prevent card click
            buttonFavorite.setOnClickListener(v -> {
                v.setClickable(false);
                boolean newFavoriteState = !r.isFavorite();
                r.setFavorite(newFavoriteState);

                if (newFavoriteState) {
                    buttonFavorite.setImageResource(R.drawable.ic_heart_filled);
                    buttonFavorite.clearColorFilter();
                } else {
                    buttonFavorite.setImageResource(R.drawable.ic_heart_outline);
                    buttonFavorite.clearColorFilter();
                }
                
                if (favListener != null) {
                    favListener.onFavoriteToggle(r, newFavoriteState);
                }
                v.setClickable(true);
            });
        }
    }
}
