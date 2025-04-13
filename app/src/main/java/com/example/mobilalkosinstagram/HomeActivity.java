package com.example.mobilalkosinstagram;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private static final String LOG_TAG = HomeActivity.class.getName();
    private BottomNavigationView bottomNavigation;
    private NavigationRailView navigationRail;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize both navigation views
        bottomNavigation = findViewById(R.id.bottomNavigation);
        navigationRail = findViewById(R.id.navigationRail);

        // Check which navigation to show based on screen size/orientation
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        boolean isLandscape = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;

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

        RecyclerView feedRecyclerView = findViewById(R.id.feedRecyclerView);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedRecyclerView.setAdapter(new FeedAdapter(getPlaceholderPosts()));
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
            showToast("Profile will be implemented later");
        } else if (itemId == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }
    }

    private List<Post> getPlaceholderPosts() {
        List<Post> posts = new ArrayList<>();
        // Add 5 placeholder posts
        for (int i = 1; i <= 5; i++) {
            posts.add(new Post("User " + i, "This is placeholder post #" + i));
        }
        return posts;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Simple placeholder class for posts
    private static class Post {
        String username;
        String description;

        Post(String username, String description) {
            this.username = username;
            this.description = description;
        }
    }

    // Simple adapter for the feed
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
            holder.username.setText(post.username);
            holder.description.setText(post.description);
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView username;
            TextView description;

            ViewHolder(android.view.View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.username);
                description = itemView.findViewById(R.id.description);
            }
        }
    }
}