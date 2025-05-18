package com.example.mobilalkosinstagram;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private LocationHelper locationHelper;
    private static final String LOG_TAG = HomeActivity.class.getName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private BottomNavigationView bottomNavigation;
    private NavigationRailView navigationRail;
    private FirebaseUser user;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize location services
        locationHelper = new LocationHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        // Initialize both navigation views
        bottomNavigation = findViewById(R.id.bottomNavigation);
        navigationRail = findViewById(R.id.navigationRail);

        // Check which navigation to show based on screen size/orientation
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (isTablet || isLandscape) {
            // Show rail, hide bottom nav
            if (bottomNavigation != null) bottomNavigation.setVisibility(View.GONE);
            if (navigationRail != null) navigationRail.setVisibility(View.VISIBLE);
            setupNavigationRail();
        } else {
            // Show bottom nav, hide rail
            if (navigationRail != null) navigationRail.setVisibility(View.GONE);
            if (bottomNavigation != null) bottomNavigation.setVisibility(View.VISIBLE);
            setupBottomNavigation();
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.i(LOG_TAG, "Unauthenticated user!");
            finish();
            return;
        }

        // Check location permission
        checkLocationPermission();

        RecyclerView feedRecyclerView = findViewById(R.id.feedRecyclerView);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirestorePostRepository repository = new FirestorePostRepository();
        repository.getAllPosts(posts -> {
            feedRecyclerView.setAdapter(new FeedAdapter(posts));
        }, e -> {
            Log.e(LOG_TAG, "Hiba történt a posztok lekérdezésekor", e);
            Toast.makeText(this, "Nem sikerült a posztok betöltése.", Toast.LENGTH_SHORT).show();
        });

        // Add Post gomb
        Button addPostButton = findViewById(R.id.addPostButton);
        addPostButton.setOnClickListener(v -> {
            // Kezdetben üres helynévvel hozzuk létre a posztot
            Post newPost = new Post(user.getDisplayName() != null ? user.getDisplayName() : "User1",
                    "Ez egy új poszt leírása",
                    "https://linkképhez.hu/cat.jpg",
                    "");

            //Lekérjük a helyet és frissítjük a posztot
            locationHelper.getCurrentLocation(location -> {
                Log.d(LOG_TAG, location != null ? "Location received" : "Location not received");
                if (location != null) {
                    String cityName = getCityNameFromLocation(location);
                    newPost.setLocation(cityName);

                    // Hozzáadjuk a posztot a Firestore-hoz
                    FirestorePostRepository repository1 = new FirestorePostRepository();
                    repository.addPost(newPost, HomeActivity.this);

                    // Frissítsük a RecyclerView-t
                    repository.getAllPosts(posts -> {
                        feedRecyclerView.setAdapter(new FeedAdapter(posts));
                    }, e -> {
                        Log.e(LOG_TAG, "Hiba történt a posztok lekérdezésekor", e);
                        Toast.makeText(HomeActivity.this, "Nem sikerült a posztok frissítése.", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        getLifecycle().addObserver(new HomeActivityLifecycleObserver(this));

        // Új gombok kezelése
        Button sortByLocationButton = findViewById(R.id.sortByLocationButton);
        Button filterByLengthButton = findViewById(R.id.filterByLengthButton);
        Button filterShortPostsButton = findViewById(R.id.filterShortPostsButton);

        sortByLocationButton.setOnClickListener(v -> {
            repository.getPostsOrderedByLocation(
                    posts -> {
                        feedRecyclerView.setAdapter(new FeedAdapter(posts));
                    },
                    e -> {
                        Toast.makeText(this, "Hiba a rendezésben", Toast.LENGTH_SHORT).show();
                    }
            );
        });

        filterByLengthButton.setOnClickListener(v -> {
            repository.getPostsByDescriptionLength(100, // 100 karakter minimum
                    posts -> {
                        if (posts.isEmpty()) {
                            Toast.makeText(this, "Nincs ilyen hosszú leírású poszt", Toast.LENGTH_SHORT).show();
                        } else {
                            feedRecyclerView.setAdapter(new FeedAdapter(posts));
                        }
                    },
                    e -> {
                        Toast.makeText(this, "Hiba a szűrésben", Toast.LENGTH_SHORT).show();
                    }
            );
        });

        filterShortPostsButton.setOnClickListener(v -> {
            repository.getPostsWithShortDescriptions(
                    posts -> {
                        if (posts.isEmpty()) {
                            Toast.makeText(this, "Nincs ilyen rövid leírású poszt", Toast.LENGTH_SHORT).show();
                        } else {
                            feedRecyclerView.setAdapter(new FeedAdapter(posts));
                            Toast.makeText(this, "Rövid posztok betöltve", Toast.LENGTH_SHORT).show();
                        }
                    },
                    e -> {
                        Toast.makeText(this, "Hiba a rövid posztok szűrésében", Toast.LENGTH_SHORT).show();
                    }
            );
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setOnNavigationItemSelectedListener(item -> {
                handleNavigationItemSelected(item.getItemId());
                return true;
            });
        }
    }

    private void setupNavigationRail() {
        if (navigationRail != null) {
            navigationRail.setOnItemSelectedListener(item -> {
                handleNavigationItemSelected(item.getItemId());
                return true;
            });
        }
    }

    private void handleNavigationItemSelected(int itemId) {
        if (itemId == R.id.nav_home) {
            // Already on home
        } else if (itemId == R.id.nav_messages) {
            showToast("Messages will be implemented later");
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        } else if (itemId == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Helymeghatározás engedélyezve", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Helymeghatározás nélkül nem jeleníthető meg a pontos hely", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getCurrentLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Helymeghatározáshoz engedély szükséges", Toast.LENGTH_SHORT).show();
            callback.onLocationReceived(null);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        callback.onLocationReceived(location);
                    } else {
                        Toast.makeText(this, "Nem sikerült lekérni a helyadatokat", Toast.LENGTH_SHORT).show();
                        callback.onLocationReceived(null);
                    }
                });
    }

    private String getCityNameFromLocation(Location location) {
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1); // Maximum 1 eredményt kérünk

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Városnév vagy ha az nincs, akkor a településnév
                String city = address.getLocality();
                if (city == null) {
                    city = address.getSubAdminArea();
                }
                return city != null ? city : String.format("%.4f, %.4f", location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Hiba történt a geokódolás során", e);
        }
        // Ha bármi probléma van, visszaadjuk a koordinátákat
        return String.format("%.4f, %.4f", location.getLatitude(), location.getLongitude());
    }

    interface LocationCallback {
        void onLocationReceived(Location location);
    }

    public void refreshFeed() {
        // A feed adatainak frissítése a Firestore-ból, ha az aktivitás újra látható
        RecyclerView feedRecyclerView = findViewById(R.id.feedRecyclerView);
        FirestorePostRepository repository = new FirestorePostRepository();

        repository.getAllPosts(posts -> {
            // Adapter létrehozása
            FeedAdapter adapter = new FeedAdapter(posts);
            feedRecyclerView.setAdapter(adapter);

            // Layout animáció beállítása
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(
                    this, R.anim.layout_slide_up);
            feedRecyclerView.setLayoutAnimation(animation);

            // Animáció indítása
            feedRecyclerView.scheduleLayoutAnimation();

        }, e -> {
            Log.e(LOG_TAG, "Hiba történt a posztok frissítésekor", e);
            Toast.makeText(this, "Nem sikerült a posztok frissítése.", Toast.LENGTH_SHORT).show();
        });
    }

    public void saveUserProgress() {
        // A felhasználó által végzett módosításokat elmenthetjük
        // Például: menthetjük a helyi adatbázisba vagy a Firebase Firestore-ba.
        Log.d("HomeActivity", "User progress saved!");
    }
    private static class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
        private final List<Post> posts;

        FeedAdapter(List<Post> posts) {
            this.posts = posts;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_feed_post, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Post post = posts.get(position);
            holder.username.setText(post.getUsername());
            holder.description.setText(post.getDescription());
            holder.editDescription.setText(post.getDescription());

            // Hely megjelenítése
            if (post.getLocation() != null && !post.getLocation().isEmpty()) {
                holder.locationTextView.setText(post.getLocation());
            } else {
                holder.locationTextView.setText("Hely ismeretlen");
            }

            // Törlés gomb
            holder.deleteButton.setOnClickListener(v -> {
                FirestorePostRepository repository = new FirestorePostRepository();
                repository.deletePost(post.getId());
                posts.remove(position);
                notifyItemRemoved(position);
            });

            // Frissítés gomb
            holder.updateButton.setOnClickListener(v -> {
                // Elrejtjük a régi leírást
                holder.description.setVisibility(View.GONE);
                // Megjelenítjük a szerkesztő layout-ot
                holder.editDescriptionLayout.setVisibility(View.VISIBLE);
            });

            // Save Update gomb
            holder.saveUpdateButton.setOnClickListener(v -> {
                String newDescription = holder.editDescription.getText().toString().trim();

                if (newDescription.isEmpty()) {
                    Toast.makeText(holder.itemView.getContext(), "A leírás nem lehet üres.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirestorePostRepository repository = new FirestorePostRepository();
                post.setDescription(newDescription);
                repository.updatePost(post, () -> {
                    // Lista frissítése sikeres frissítés után
                    repository.getAllPosts(updatedPosts -> {
                        posts.clear();
                        posts.addAll(updatedPosts);
                        notifyDataSetChanged();
                    }, e -> {
                        Log.e(LOG_TAG, "Hiba a lista frissítésekor", e);
                        Toast.makeText(holder.itemView.getContext(), "Nem sikerült a lista frissítése.", Toast.LENGTH_SHORT).show();
                    });
                }, e -> {
                    Log.e(LOG_TAG, "Hiba a poszt frissítésekor", e);
                    Toast.makeText(holder.itemView.getContext(), "Nem sikerült a poszt frissítése.", Toast.LENGTH_SHORT).show();
                });

                // Elrejtjük a szerkesztő layout-ot, újra megjelenítjük a leírást
                holder.editDescriptionLayout.setVisibility(View.GONE);
                holder.description.setVisibility(View.VISIBLE);
            });
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView username;
            TextView description;
            TextView locationTextView;
            EditText editDescription;
            Button deleteButton;
            Button updateButton;
            Button saveUpdateButton;
            LinearLayout editDescriptionLayout;

            ViewHolder(android.view.View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.username);
                description = itemView.findViewById(R.id.description);
                locationTextView = itemView.findViewById(R.id.locationTextView);
                editDescription = itemView.findViewById(R.id.editDescription);
                deleteButton = itemView.findViewById(R.id.deletePostButton);
                updateButton = itemView.findViewById(R.id.updatePostButton);
                saveUpdateButton = itemView.findViewById(R.id.saveUpdateButton);
                editDescriptionLayout = itemView.findViewById(R.id.editDescriptionLayout);
            }
        }
    }
}