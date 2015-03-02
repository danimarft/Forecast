package es.marfetan.daniel.forecast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by daniel on 24/02/2015.
 */
/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {


    private final String TAG = ForecastFragment.class.getSimpleName();
    ArrayAdapter<String> adaptador;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        String weekForecast[] = {"Hoy-Soleado-19:30", "Mañana-Soleado-19:30", "Lunesy-Soleado-19:30", "Martes-Soleado-19:30", "Miercoles-Soleado-19:30", "Jueves-Soleado-19:30", "Viernes-Soleado-19:30"};
       adaptador = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview,new ArrayList<String>());
        ListView listaForecast = (ListView) rootView.findViewById(R.id.listViewForecast);
        listaForecast.setAdapter(adaptador);

        listaForecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = adaptador.getItem(position);
               // Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(),DetailActivity.class);//intent explicito para llamr a una actividad
                intent.putExtra(Intent.EXTRA_TEXT,forecast);
                startActivity(intent);

            }
        });

        new FetchWeatherTask().execute("Madrid");

        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = appPrefs.getString(getResources().getString(R.string.pref_location_key),getResources().getString(R.string.pref_location_default));
        weatherTask.execute(location);

    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }


    class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... ciudad) {//pasarle por parámetro las coordenadas del sitio a mostrar


            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String forecast []= null;
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            URL url = null;
            try {

                final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String CONSULTA = "q";
                final String MODO = "mode";
                String formato = "json";
                final String UNIDADES = "units";
                String unidad = "metric";
                final String CNT = "cnt";
                String dias = "7";
                Uri.Builder builUri = Uri.parse(URL_BASE).buildUpon()
                        .appendQueryParameter(CONSULTA, ciudad[0])
                        .appendQueryParameter(MODO, formato)
                        .appendQueryParameter(UNIDADES, unidad)
                        .appendQueryParameter(CNT, dias);
                // url = new  URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=Madrid&mode=json&units=metric&cnt=7");


                url = new URL(builUri.toString());//llamo a la construccion de la url con uri builder
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.

                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "Error ", e);
            } catch (ProtocolException e) {
                Log.e(TAG, "Error ", e);
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }

            }
            try {
                forecast = getWeatherDataFromJson(forecastJsonStr,7);//guardo los datos obtenidos para mostrarlos en el listview
            } catch (JSONException e) {
                Log.e(TAG, "Error getting data", e);
            }

            return forecast;
        }

        @Override
        protected void onPostExecute (String[] result){
            if (result!=null){
                adaptador.clear();//limpiar el adaptador
                for(String datos:result)
                    adaptador.add(datos);
            }
        }


    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }
        private long celsiusTofahrenheit(long celsius) {
            return (long)(9.0 / 5.0) * celsius + 32;
        }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);
        String highLowStr = "";
        SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String default_unit = getResources().getString(R.string.pref_default_unit);
        String preference_unit = appPrefs.getString(getResources().getString(R.string.pref_unit_key),default_unit);
        if(!preference_unit.equals(default_unit))
            highLowStr = celsiusTofahrenheit(roundedHigh) + "/" + celsiusTofahrenheit(roundedLow);
        else
            highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy: constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";
        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        String[] resultStrs = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;
            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);
            // The date/time is returned as a long. We need to convert that
            // into something human-readable, since most people won't read "1400356800" as

            // "this saturday".
            long dateTime = dayForecast.getLong(OWM_DATETIME);
            day = getReadableDateString(dateTime);
            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            // Temperatures are in a child object called "temp". Try not to name variables
            // "temp" when working with temperature. It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);
            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;


            Log.i(TAG, resultStrs[i]);//Comprueba mediante el log que procesa correctamente el fichero JSON
        }
        return resultStrs;


    }

}


}

