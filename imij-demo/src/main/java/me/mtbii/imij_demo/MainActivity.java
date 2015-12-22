package me.mtbii.imij_demo;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import me.mtbii.imij_lib.Imij;

public class MainActivity extends AppCompatActivity {
    public static final String ARG_LAST_MENU_POSITION = "last_menu_position";

    private String[] mImijFeatureTitles;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private int mCurrentPosition;
    private Imij mImij;

    private ImijFragment mLastFragment;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mCurrentPosition = 0;
        mTitle = mDrawerTitle = getTitle();
        mImij = new Imij(this);

        mImijFeatureTitles = getResources().getStringArray(R.array.imij_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.nav_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mImijFeatureTitles));

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        selectItem(0);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(ARG_LAST_MENU_POSITION, mCurrentPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        selectLastItem(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        switch(item.getItemId()){
            case R.id.action_settings:
                openSettings();
                break;

            case R.id.action_refresh:
                if (mLastFragment != null) {
                    mLastFragment.refresh();
                }
                break;

            default: return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void selectLastItem(Bundle savedInstanceState) {
        try {
            int lastPos = savedInstanceState.getInt(ARG_LAST_MENU_POSITION);
            selectItem(lastPos);
        }
        catch(Exception e){
            selectItem(0);
        }
    }

    public void selectItem(int position) {
        String title = mImijFeatureTitles[position];
        mProgressDialog = ProgressDialog.show(this, "Processing", title, true, false);
        mLastFragment = new ImijFragment();
        Bundle args = new Bundle();
        args.putInt(ImijFragment.ARG_IMIJ_NUMBER, position);
        mLastFragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, mLastFragment)
                .commit();

        mDrawerList.setItemChecked(position, true);
        setTitle(title);
        mDrawerLayout.closeDrawer(mDrawerList);
        mCurrentPosition = position;
    }

    public void setTitle(CharSequence title){
        mTitle = title;
        getSupportActionBar().setTitle(title);
    }

    private void openSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public Imij getImijContext(){
        return mImij;
    }

    public ProgressDialog getProgressDialog() {
        return mProgressDialog;
    }

    public class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

}
