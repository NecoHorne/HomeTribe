package com.necohorne.hometribe.Activities.AppActivities;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.necohorne.hometribe.Activities.Fragments.AboutFragment;
import com.necohorne.hometribe.Activities.Fragments.TandCFragment;
import com.necohorne.hometribe.Activities.Fragments.PrivacyFragment;
import com.necohorne.hometribe.R;
import com.necohorne.hometribe.Utilities.SectionsPageAdapter;

public class AboutActivity extends AppCompatActivity {

    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_about );
        mViewPager = (ViewPager) findViewById( R.id.container);
        setUpViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById( R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setUpViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment( new AboutFragment(), "About" );
        adapter.addFragment( new TandCFragment(), "Terms" );
        adapter.addFragment( new PrivacyFragment(), "Privacy Policy" );
        viewPager.setAdapter(adapter);
    }
}
