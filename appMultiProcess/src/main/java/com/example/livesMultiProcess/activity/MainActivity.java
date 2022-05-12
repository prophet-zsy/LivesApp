package com.example.livesMultiProcess.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.example.livesMultiProcess.R;
import com.example.livesMultiProcess.fragment.BookCategoryFragment;
import com.example.livesMultiProcess.fragment.BookRecommendFragment;
import com.example.livesMultiProcess.fragment.MusicContainerFragment;
import com.example.livesMultiProcess.fragment.SettingFragment;
import com.example.livesMultiProcess.fragment.SportFragment;
import com.example.livesMultiProcess.fragment.TranslateFragment;
import com.example.livesMultiProcess.widget.BottomNavigationBar;
import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.tool_bar)
    Toolbar toolbar;

    @BindView(R.id.bottom_nav)
    BottomNavigationBar bottom_nav;

    @BindView(R.id.container)
    FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initDrawerNav();
        initBottomNav();
    }

    private void initDrawerNav() {
        setSupportActionBar(toolbar);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(  // 该toggle实现了drawerlayout对应的listenter
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.nav_sport:
                        Log.i(TAG, "onNavigationItemSelected you choose SportFragment");
                        fragment = SportFragment.newInstance();
                        bottom_nav.setVisibility(View.GONE);
                        toolbar.setTitle(R.string.sport);
                        break;
                    case R.id.nav_music:
                        Log.i(TAG, "onNavigationItemSelected you choose MusicContainerFragment");
                        fragment = MusicContainerFragment.newInstance(MainActivity.this);
                        bottom_nav.setVisibility(View.GONE);
                        toolbar.setTitle(R.string.music);
                        break;
                    case R.id.nav_translate:
                        Log.i(TAG, "onNavigationItemSelected you choose TranslateFragment");
                        fragment = TranslateFragment.newInstance();
                        bottom_nav.setVisibility(View.GONE);
                        toolbar.setTitle(R.string.translate);
                        break;
                    case R.id.nav_book:
                        Log.i(TAG, "onNavigationItemSelected you choose BookCategoryFragment");
                        fragment = BookCategoryFragment.newInstance();
                        bottom_nav.setVisibility(View.VISIBLE);
                        toolbar.setTitle(R.string.book);
                        break;
                    case R.id.nav_setting:
                        Log.i(TAG, "onNavigationItemSelected you choose SettingFragment");
                        fragment = SettingFragment.newInstance();
                        bottom_nav.setVisibility(View.GONE);
                        toolbar.setTitle(R.string.setting);
                        break;
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commitAllowingStateLoss();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void initBottomNav() {
        bottom_nav.addTab("图书分类", R.mipmap.ic_book_category);
        bottom_nav.addTab("图书推荐", R.mipmap.ic_book_recommend);
        bottom_nav.addTab("图书推荐", R.mipmap.ic_book_recommend);
        bottom_nav.addTab("图书推荐", R.mipmap.ic_book_recommend);

        bottom_nav.setSelectedListener(new BottomNavigationBar.SelectedTabListener() {
            @Override
            public void onSelectedTab(int pos) {
                Fragment fragment = null;
                switch (pos) {
                    case 0:
                        fragment = BookCategoryFragment.newInstance();
                        break;
                    case 1:
                    case 2:
                    case 3:
                        fragment = BookRecommendFragment.newInstance();
                        break;
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commitAllowingStateLoss();
            }
        });
    }

}
