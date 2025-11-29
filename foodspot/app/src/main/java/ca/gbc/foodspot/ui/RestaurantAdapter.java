package ca.gbc.foodspot.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        items = list;
        notifyDataSetChanged();
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
        Restaurant r = items.get(position);
        holder.bind(r, clickListener, favListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageThumb;
        TextView textName, textAddress, textMeta, textTags;
        CheckBox checkFavorite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumb = itemView.findViewById(R.id.imageThumb);
            textName = itemView.findViewById(R.id.textName);
            textAddress = itemView.findViewById(R.id.textAddress);
            textMeta = itemView.findViewById(R.id.textMeta);
            textTags = itemView.findViewById(R.id.textTags);
            checkFavorite = itemView.findViewById(R.id.checkFavorite);
        }

        void bind(Restaurant r,
                  OnItemClickListener clickListener,
                  OnFavoriteToggleListener favListener) {
            imageThumb.setImageResource(R.drawable.ic_restaurant_placeholder);
            textName.setText(r.getName());
            textAddress.setText(r.getAddress());
            String meta = r.getRating() + " • " +
                    (r.getPriceLevel() == null ? "" : r.getPriceLevel()) +
                    (r.getDistanceText() == null ? "" : " • " + r.getDistanceText());
            textMeta.setText(meta);
            textTags.setText(r.getTags());

            checkFavorite.setOnCheckedChangeListener(null);
            checkFavorite.setChecked(r.isFavorite());

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(r);
            });

            checkFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (favListener != null) favListener.onFavoriteToggle(r, isChecked);
            });
        }
    }
}
