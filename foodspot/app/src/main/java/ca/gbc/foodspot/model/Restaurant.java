package ca.gbc.foodspot.model;

public class Restaurant {
    private long id;
    private String name;
    private String address;
    private String phone;
    private float rating;
    private String priceLevel;
    private String distanceText;
    private String tags;
    private String description;
    private boolean favorite;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getPriceLevel() { return priceLevel; }
    public void setPriceLevel(String priceLevel) { this.priceLevel = priceLevel; }

    public String getDistanceText() { return distanceText; }
    public void setDistanceText(String distanceText) { this.distanceText = distanceText; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}
