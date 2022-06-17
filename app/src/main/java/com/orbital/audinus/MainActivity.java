package com.orbital.audinus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout Tablayout;
    private FragmentContainerView fragmentContainerView;
    private SlidingUpPanelLayout slidingLayout;
    private FragmentContainerView playerSlider;
    static ImageView imageView;
    boolean slide = false;





//BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (!checkPermission()) {
            requestPermission();
            if (Build.VERSION.SDK_INT < 30) {
                onCreate(savedInstanceState);
            }
        }
            setContentView(R.layout.activity_main);
            Tablayout = findViewById(R.id.views);
            viewPager = findViewById(R.id.viewpager);
            fragmentContainerView = findViewById(R.id.currently_playing_bar);


            slidingLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
            playerSlider = findViewById(R.id.playerSlide);
            slidingLayout.setPanelSlideListener(onSlideListener());
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            imageView = findViewById(R.id.album_art2);
            imageView.setVisibility(View.GONE);

            imageView.setOnClickListener(v -> {

                    Intent intent = new Intent(this, MusicPlayerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (MyMediaPlayer.isPlayingSameSong()) { //prevents crash but causes progressbar to freak out sometimes
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    }
                    this.startActivity(intent);

            });


            Tablayout.setupWithViewPager(viewPager);

            FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            fragmentAdapter.addFragment(new SongsFragment(), "songs");
            fragmentAdapter.addFragment(new PlaylistsFragment(), "playlist");
            fragmentAdapter.addFragment(new fragment3(), "favorites");
            viewPager.setAdapter(fragmentAdapter);
    }


    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
            Toast.makeText(this, "Storage permissions are needed to manage your music", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    private SlidingUpPanelLayout.PanelSlideListener onSlideListener() {
        return new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
            }

            @Override
            public void onPanelCollapsed(View view) {
                slide=false;
            }

            @Override
            public void onPanelExpanded(View view) {
                slide=true;
            }

            @Override
            public void onPanelAnchored(View view) {
            }

            @Override
            public void onPanelHidden(View view) {
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (!slide){
            super.onBackPressed();
        } else{
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

    }
}