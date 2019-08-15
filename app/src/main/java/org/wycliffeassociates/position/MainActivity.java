package org.wycliffeassociates.position;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class MainActivity extends Activity {

    private CarsFragment carsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        carsFragment = (CarsFragment) fm.findFragmentById(R.id.fragment_container);

        if(carsFragment == null) {
            carsFragment = new CarsFragment();
            ft.add(R.id.fragment_container, carsFragment).commit();
        }
    }
}
