package es.marfetan.daniel.forecast;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;


public class DetailActivity extends ActionBarActivity {

    final static String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailActivityFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailActivityFragment extends Fragment {


        final static String FORECAST_SHARE_HASHTAG = "#ShunshineApp";
        private String forecast;


        public DetailActivityFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detailfragment, menu);
            MenuItem menuItem = menu.findItem(R.id.action_share);




           ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

           if(mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
            else {
                Log.d(TAG, "Share action provider is null?");
            }


           // super.onCreateOptionsMenu(menu,inflater);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);


            Intent intent = getActivity().getIntent();//escucho la llamda de la otra actividad

            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                forecast = intent.getStringExtra(Intent.EXTRA_TEXT);//guardo la informaion
                //Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
               ((TextView) rootView.findViewById(R.id.detail_text)).setText(forecast);//muestro la info segun los mockups
            }
            return rootView;


        }
        private Intent createShareForecastIntent(){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags((Intent.FLAG_ACTIVITY_NEW_DOCUMENT));
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,forecast+FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }
    }
}
