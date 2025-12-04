package com.example.samiratravelmobile;

import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.samiratravelmobile.fragments.ReloadableFragment;
import com.example.samiratravelmobile.fragments.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private boolean hasResumedOnce = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScrollView mainScrollView = findViewById(R.id.mainScrollView);
        FloatingActionButton fabScrollTop = findViewById(R.id.fabScrollTop);
        FloatingActionButton fabScrollBottom = findViewById(R.id.fabScrollBottom);

        fabScrollTop.setOnClickListener(v -> mainScrollView.smoothScrollTo(0, 0));
        fabScrollBottom.setOnClickListener(v -> mainScrollView.post(() -> {
            View content = mainScrollView.getChildAt(0);
            if (content != null) {
                int bottom = content.getMeasuredHeight();
                mainScrollView.smoothScrollTo(0, bottom);
            }
        }));

        mainScrollView.setOnScrollChangeListener((View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) -> {
            if (scrollY <= 32) {
                fabScrollTop.hide();
            } else {
                fabScrollTop.show();
            }

            View content = mainScrollView.getChildAt(0);
            if (content == null) {
                fabScrollBottom.hide();
                return;
            }

            int maxScroll = content.getMeasuredHeight() - mainScrollView.getHeight();
            if (maxScroll <= 32 || scrollY >= maxScroll - 16) {
                fabScrollBottom.hide();
            } else {
                fabScrollBottom.show();
            }
        });

        mainScrollView.post(() -> {
            fabScrollTop.hide();
            View content = mainScrollView.getChildAt(0);
            if (content == null || content.getMeasuredHeight() <= mainScrollView.getHeight()) {
                fabScrollBottom.hide();
            }
        });

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container, new HeroFragment());
        ft.add(R.id.container, new AboutFragment());
        ft.add(R.id.container, new KeunggulanFragment());
        ft.add(R.id.container, new PackagesFragment());
        ft.add(R.id.container, new GalleryFragment());
        ft.add(R.id.container, new LeaderFragment());
        ft.add(R.id.container, new FooterFragment());
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasResumedOnce) {
            hasResumedOnce = true;
            return;
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof ReloadableFragment) {
                ((ReloadableFragment) fragment).reloadData();
            }
        }
    }
}
