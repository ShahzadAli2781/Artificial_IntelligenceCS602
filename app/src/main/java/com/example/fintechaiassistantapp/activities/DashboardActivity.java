package com.example.fintechaiassistantapp.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.fragments.HomeFragment;
import com.example.fintechaiassistantapp.fragments.HistoryFragment;
import com.example.fintechaiassistantapp.fragments.TrendsFragment;
import com.example.fintechaiassistantapp.fragments.ProfileFragment;
import com.example.fintechaiassistantapp.network.SyncManager;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_dashboard);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        ExtendedFloatingActionButton fab = findViewById(R.id.fab_add);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "FinIntelligence";
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                title = "Dashboard";
                fab.show();
            } else if (id == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
                title = "Transaction History";
                fab.show();
            } else if (id == R.id.nav_trends) {
                selectedFragment = new TrendsFragment();
                title = "Spending Trends";
                fab.show();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = "User Profile";
                fab.hide();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title);
            }
            return true;
        });

        // Set default fragment - Always refresh on fresh start to ensure data isolation
        if (savedInstanceState == null) {
            clearFragmentStack();
            loadFragment(new HomeFragment(), "Dashboard");
            fab.show();
        }

        fab.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AddExpenseActivity.class)));

        // Schedule sync on dashboard load
        SyncManager.scheduleSync(this);
    }

    private void loadFragment(Fragment fragment, String title) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void clearFragmentStack() {
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void selectTab(int itemId) {
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setSelectedItemId(itemId);
    }
}
