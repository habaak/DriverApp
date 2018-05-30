package com.erise.habaak.driverapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {


    Fragment fragment;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;

    {
        mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.navigation_home:
                        fragment = new HomeFragment();
                        switchFragment(fragment);
                        return true;
                    case R.id.navigation_bus_info:
                        fragment = new BusFragment();
                        switchFragment(fragment);
                        return true;
                    case R.id.navigation_driver_info:
                        fragment = new DriverFragment();
                        switchFragment(fragment);
                        return true;
                }
                return false;
            }

        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  Fragmnet
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        HomeFragment fragment = new HomeFragment();
        fragmentTransaction.add(R.id.fragment_container,fragment);
        fragmentTransaction.commit();

//  Bottom Nav
        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);



}

    public void switchFragment(Fragment fragment){

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack
        transaction.replace(R.id.fragment_container, fragment);
// Commit the transaction
        transaction.commit();
    }


}