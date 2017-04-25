package adamoconnor.informe.Settings;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A {@link PreferenceActivity} which implements and proxies the necessary calls
 * to be used with AppCompat.
 */
public abstract class AppCompatPreferenceActivity extends PreferenceActivity {

    // declare the appcompat delegate.
    private AppCompatDelegate mDelegate;

    /**
     * on create called on the start of the activity being called
     * @param savedInstanceState
     * the saved instance of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    /**
     * wafter the on create method has been called.
     * @param savedInstanceState
     * pass the state on which the activity is on.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    /**
     * get the action bar which resides at the top of the activity.
     * @return
     * return the actionbar.
     */
    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    /**
     * find all the items which reside in the menu.
     * @return
     * the menu which is located in the action bar.
     */
    @Override
    @NonNull
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    /**
     * set the content layout.
     * @param layoutResID
     * get the layout of the activity.
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID) { getDelegate().setContentView(layoutResID);}

    /**
     * set the content view.
     * @param view
     * get the content view of the activity.
     */
    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    /**
     * set the content view as well as the view group parameters.
     * @param view
     * the view of the activity.
     * @param params
     * the parameters of the activity.
     */
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    /**
     * adding of the content view.
     * @param view
     * adding of view
     * @param params
     * adding of parameters.
     */
    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    /**
     * after the activity has called onResume.
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    /**
     * when the title has changed.
     * @param title
     * title of activity.
     * @param color
     * colour of title.
     */
    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    /**
     * when the configuration has been changed.
     * @param newConfig
     * new configuration of user.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    /**
     * when the activity has stopped.
     */
    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    /**
     * when the activity has been destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    /**
     * clear the options menu
     */
    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }
}
