package com.weserv.grumpy.assetracker.UI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.weserv.grumpy.assetracker.Core.AssetTypes;
import com.weserv.grumpy.assetracker.Core.Assets;
import com.weserv.grumpy.assetracker.Core.Connection.APIJsonArrayRequestFactory;
import com.weserv.grumpy.assetracker.Core.Connection.HttpVolleyRequestSender;
import com.weserv.grumpy.assetracker.Core.Connection.JSONArrayResponseCallback;
import com.weserv.grumpy.assetracker.Core.Connection.MessageConstants;
import com.weserv.grumpy.assetracker.Core.Connection.RequestResponseCallback;
import com.weserv.grumpy.assetracker.Core.MISTransactions;
import com.weserv.grumpy.assetracker.Core.MgrTransactions;
import com.weserv.grumpy.assetracker.Core.Session;
import com.weserv.grumpy.assetracker.R;
import com.weserv.grumpy.assetracker.Utils.Common;

import org.json.JSONArray;

import java.util.ArrayList;

import java.util.Observable;
import java.util.Observer;

public class MainScreenActivity extends AppCompatActivity
        implements Observer {

    private Toolbar mToolbar;
    private DrawerLayout mNavDrawerLayout;
    private NavigationView mNavDrawerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private ProgressDialog myProgress;

    private AssetTypes assetTypes = new AssetTypes();
    private Assets assets = new Assets();
    private MISTransactions misTransactions = new MISTransactions();
    private MgrTransactions mgrTransactions = new MgrTransactions();

    public final static String EXTRA_MESSAGE = "com.weserv.grumpy.assetracker.UI.MainScreenActivity.MESSAGE";

    private int loadedData;
    private boolean initialLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        assetTypes.addObserver(this);
        assets.addObserver(this);
        misTransactions.addObserver(this);
        mgrTransactions.addObserver(this);

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);

        mNavDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = setupDrawerToggle();

        mNavDrawerView = (NavigationView) findViewById(R.id.drawer);
        setupDrawerContent(mNavDrawerView);

        myProgress = new ProgressDialog(this);
        myProgress.setMessage("Please wait...");
        myProgress.setCancelable(false);

        loadedData = 0;
        //Assume all IT equipments for now. Load only once
        loadedData++;
        assetTypes.retrieveFromWS(this, (MyApp) getApplication(), 2);
        loadData();
        initialLoad = true;

    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(Common.LOGNAME, "Main onStart called");
        //finish();

        if (!initialLoad){
            loadData();
        }

        initialLoad = false;
    }

    public void loadData()
    {
        if ( !myProgress.isShowing())
        {
            myProgress.show();
        }

        loadedData++;
        assets.retrieveFromWS(this,(MyApp) getApplication());

        if (((MyApp) getApplication()).getSession().getAccessRight().hasMISRights() == true ){
            loadedData ++;
            misTransactions.retrieveFromWS(this,(MyApp) getApplication());
        }

        if(((MyApp)getApplication()).getSession().getAccessRight().hasManagerRights() == true) {
            loadedData ++;
            mgrTransactions.retrieveFromWS(this,(MyApp) getApplication());
        }





     }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mNavDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        //Set name and email on NavHeader
        MyApp app = (MyApp) getApplication();
        String name = app.getSession().getUser().getName();
        String email = app.getSession().getUser().getEmailAddress();

        View headerView = mNavDrawerView.inflateHeaderView(R.layout.nav_header);
        TextView textName = (TextView) headerView.findViewById(R.id.text_emp_name);
        textName.setText(name);
        TextView textEmail = (TextView) headerView.findViewById(R.id.text_email);
        textEmail.setText(email);

        //add items depending on access rights
        Menu drawerMenu = mNavDrawerView.getMenu();

        if ( app.getSession().getAccessRight().hasMISRights()) {
            drawerMenu.setGroupVisible(R.id.grp_mis,true);
        }
        else{
            drawerMenu.setGroupVisible(R.id.grp_mis,false);
        }

        if ( app.getSession().getAccessRight().hasAdminRights()){
            drawerMenu.setGroupVisible(R.id.grp_admin,true);
        }
        else{
            drawerMenu.setGroupVisible(R.id.grp_admin,false);
        }

        if ( app.getSession().getAccessRight().hasManagerRights()){
            drawerMenu.setGroupVisible(R.id.grp_manager,true);
        }
        else{
            drawerMenu.setGroupVisible(R.id.grp_manager,false);
        }




        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on
        // position
        Fragment fragment = null;

        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.nav_item_my_asset:
                fragmentClass = MyAssetFragment.class;
                break;
            case R.id.nav_item_transactions:
                fragmentClass = AcceptanceFragment.class;
                break;
            case R.id.nav_item_asset_transfer:
                Intent transferAssetActivity = new Intent(getApplicationContext(), TransferAssetActivity.class);
                startActivity(transferAssetActivity);
                return;
            case R.id.nav_items_change_password:
                Intent changePasswordActivity = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(changePasswordActivity);
                return;
            case R.id.nav_items_logout:
                logout();
                return;
            case R.id.nav_items_mis_acceptances:
                fragmentClass = MISAcceptanceFragment.class;
                break;
            case R.id.nav_items_mis_assets:
                fragmentClass = MISAssignmentsFragment.class;
                break;

            case R.id.nav_items_mgr_approvals:
                fragmentClass = MGRApprovalsFragment.class;
                break;

            case R.id.nav_items_admin_register_asset:
                Intent assetRegistrationActivity = new Intent(getApplicationContext(), AssetRegistrationActivity.class);
                startActivity(assetRegistrationActivity);
                return;
            case R.id.nav_items_admin_assign:
                Intent assignAssetActivity= new Intent(getApplicationContext(),AssetAssignmentActivity.class);
                startActivity(assignAssetActivity);
                return;

            case R.id.nav_items_settings:
                Intent hostAddress = new Intent(getApplicationContext(), Preference.class);
                startActivity(hostAddress);
                return;



            default:
                fragmentClass = UnderConstructionFragment.class;
                menuItem.setCheckable(true);
                break;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(Common.LOGNAME,e.toString());
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mNavDrawerLayout.closeDrawers();
    }

    private void setDefaultFragment() {
        Fragment fragment = null;
        Class defaultFragment = MyAssetFragment.class;
        try {
            fragment = (Fragment) defaultFragment.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
            mNavDrawerView.setCheckedItem(R.id.nav_item_my_asset);

            setTitle(R.string.title_myasset);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //logout();
    }

    public void logout() {
        finish();
        MyApp app = (MyApp) getApplication();
        app.getSession().endSession();
    }

    @Override
    public void update(Observable observable, Object o) {

        if ( observable instanceof AssetTypes) {
            if ( o == null ) {
                Log.d(Common.LOGNAME, "Loading of Asset Types successful");
            }
            else{
                Log.d(Common.LOGNAME,"Error loading Asset Types.");
            }
            loadedData --;
        }

        if ( observable instanceof Assets){
            if ( o == null ){
                Log.d(Common.LOGNAME,"Loading of Assets successful.");
            }
            else{
                Log.d(Common.LOGNAME,"Error loading Assets");
            }

            loadedData--;
        }

        if (observable instanceof  MISTransactions){
            if ( o ==  null){
                Log.d(Common.LOGNAME,"Loading MIS Transactions Successful");
            }
            else{
                Log.d(Common.LOGNAME,"Error loading MIS transactions.");
            }

            loadedData --;
        }

        if ( observable instanceof MgrTransactions){
            if ( o ==  null){
                Log.d(Common.LOGNAME,"Loading Manager Transactions Successful");
            }
            else{
                Log.d(Common.LOGNAME,"Error loading Manager transactions.");
            }

            loadedData --;
        }

        //Complete
        if ( loadedData <= 0)
        {
            if ( myProgress.isShowing())
            {
                myProgress.hide();
            }

            setDefaultFragment();
        }

    }
}
