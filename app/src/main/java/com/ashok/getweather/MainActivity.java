package com.ashok.getweather;
/**
 * This app
 * (1) get weather-info from Yahoo-Weather web service - https://developer.yahoo.com/weather/
   (2) get city name User-input thru App-bar after clicking search-icon
   (3) handle bad user input by providing proper message on display.
       "Sorry no weather info for user entered "bad input" !!

   Implementation points:
   To fetch above JSON data, this app uses Retrofit-2 Library and thus this App
   do NOT have to use (i) Http calls (ii) Async Task
   Refs for Retrofit exampels to get JSON data from remote websvc websites
    (1) http://codeentries.com/libraries/how-to-use-retrofit-2-in-android-the-example.html
  Ref for converting JSON data to Pojo file Pavel Dudka used AndroidWarrier.com web site info
  (ii)http://www.androidwarriors.com/2015/12/retrofit-20-android-example-web.html
 */
import android.app.SearchManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ashok.getweather.api.RestApiClient;
import com.ashok.getweather.api.RestApi_Interface;
import com.ashok.getweather.pojos.Channel;
import com.ashok.getweather.pojos.Model;
import com.ashok.getweather.pojos.NameAndId_pojo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    String BaseURL_forYahooWebSvcAPI = "https://query.yahooapis.com";
    TextView mTxt_mainTitle_w_cityName, mTxt_temperatureLocal, mTxt_statusLocal, txt_humidity,
            txt_sunrise, txt_sunset;
    //Forcast variables
    TextView mTxt_titleFiveDayForcast_w_cityName;

    private ArrayList<ForecastItems>mArrayListFcastItems;//Array to store forcast items for 5 days
    private AdapterListView_Fcast mAdapter; //for binding each row of forcasst-data in Array to View
    Context mContextThisApp;
    String mCityName; //to find weather info

    SearchView mAppBarSearchView;
    TextView mUserInput;

    private static final String TAG = "ak";
    //-------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContextThisApp = getApplicationContext();

        //Get refs. for current weather condition to display on top
        mTxt_mainTitle_w_cityName = (TextView)findViewById(R.id.txt_titleLine_w_cityName);
        mTxt_temperatureLocal = (TextView)findViewById(R.id.txt_tempLocal);
        mTxt_statusLocal = (TextView)findViewById(R.id.txt_statusLocal);
        //txt_humidity = (TextView)findViewById(R.id.txt_humidity);
        //txt_sunrise = (TextView)findViewById(R.id.txt_sunrise);
        //txt_sunset = (TextView)findViewById(R.id.txt_sunset);

        //Forecast 5 days title
        mTxt_titleFiveDayForcast_w_cityName=(TextView)
                                findViewById(R.id.txt_titleFivedaysForcast_w_cityName);
        //Array to store forcast for 5 days
        mArrayListFcastItems = new ArrayList<ForecastItems>();
        //set ListView Adapter for each row for our array for 5 days of Froecast data
        mAdapter = new AdapterListView_Fcast(mContextThisApp, mArrayListFcastItems);

        //get ref to listView for 5-days forecastdata; It is in main-XML file
        ListView listViewForForcastDataFor5Days = (ListView) findViewById(R.id.listVIew_inMain);
        //set AdapterForArrayList for out List View in main-XML file
        listViewForForcastDataFor5Days.setAdapter(mAdapter);
        mCityName = "San Jose, CA";//"San Jose, CA 95136";//("New York, NY");//("Miami, FL");
        getWeatherDataFromYahooWebService(mCityName);
    }
    //----------------------------------------------------------
    void getWeatherDataFromYahooWebService(String cityName) {
        String queryStringForYahooWebSvc = yahooQueryLangFormat(cityName);
        Call<Model> callRetrofit = RestApiClient.getDataUsingRetrofitAndRestApiInterface().
                queryIn_RestApi_Interface(queryStringForYahooWebSvc);
        //********
        callRetrofit.enqueue(new Callback<Model>() {
            //---------------------------------------------------------------------------------
            @Override public void onResponse(Call<Model> callNotUsed, Response<Model> response){
                try {
                    //get reponse-data from Yahoo-weather-Websvc. It has JSON format
                    Channel channel = response.body().getQuery().getResults().getChannel();

                    String cityLocal = channel.getLocation().getCity();
                    String temperatureLocal = channel.getItem().getCondition().getTemp();
                    String statusLocal = channel.getItem().getCondition().getText();
                    String forecastStatus_0 = channel.getItem().getForecast().get(0).getText();
                    statusLocal = forecastStatus_0;
                    //String humidity = channel.getAtmosphere().getHumidity();
                    //String sunriseTime = channel.getAstronomy().getSunrise();
                    //String sunsetTime = channel.getAstronomy().getSunset();
                    /****************************************************************
                     * Get Weather-Forecast from JSON-Array from Yahoo-Web-svc for ALL 5 days
                     ***************************************************************/
                    // first, CLEAR old Weather-Forcast data from ArrayList
                    mArrayListFcastItems.clear();
                    for (int i = 0; i < 5; i++){
                        String forecastDay = channel.getItem().getForecast().get(i).getDay();
                        forecastDay = centerString(forecastDay, 5);
                        if (i == 0)
                            forecastDay = "Today";
                        //Log.i(TAG, "Response():Day1="+forecastDay + " Len="+forecastDay_1.length());
                        String forecastHi = channel.getItem().getForecast().get(i).getHigh();
                        String forecastLo = channel.getItem().getForecast().get(i).getLow();
                        String forecastStatus = channel.getItem().getForecast().get(i).getText();
                        //forecastStatus_1 = makeIt_13_charLong(forecastStatus_2);
                        forecastStatus = centerString(forecastStatus, 13);
                        String forecastDate = channel.getItem().getForecast().get(i).getDate();
                        forecastDate = getMonth_n_day(forecastDate);
                        //Log.i(TAG, "Status_Len="+forecastStatus.length());
                        //Log.i(TAG, "Date="+forecastDate);
                        ForecastItems forecastItems = new ForecastItems(forecastDay,
                                forecastHi+ "\u00B0",
                                forecastLo+ "\u00B0",
                                forecastStatus,
                                forecastDate);

                        //add this forecastItems to ArralyList
                        mArrayListFcastItems.add(forecastItems);
                    }//for
                    mAdapter.notifyDataSetChanged();
                    //----------------------------------------------------------
                    //Display CURRENT weather in top
                    if (null != cityLocal)
                        mTxt_mainTitle_w_cityName.setText("Weather for " + cityLocal);
                    if (null != temperatureLocal)
                        mTxt_temperatureLocal.setText(temperatureLocal + "\u00B0");//for degree F "\u2109");
                    else
                        mTxt_temperatureLocal.setText("*Temp*");//for degree F "\u2109");
                    if (null != statusLocal)
                        mTxt_statusLocal.setText(forecastStatus_0);
                    else
                        mTxt_statusLocal.setText("*Status*");

                    //txt_humidity.setText("humidity  : " + humidity);

                    //txt_sunrise.setText("Sunrise time: " + sunriseTime);
                    //txt_sunset.setText("Sunset time: " + sunsetTime);

                    //Forecast-5-days-title-line
                    mTxt_titleFiveDayForcast_w_cityName.setText("Five days forecast for "+cityLocal);

                    //txt_subtitleHiLoStatusForecast.setText(stringToDisplay);

                } catch (Exception e) {
                    Log.i(TAG, "*** Exception - onResponce()");
                    mTxt_mainTitle_w_cityName.setText("Sorry. No weather-data available for:");
                    mTxt_temperatureLocal.setText(mCityName);
                    clearAllRemainingDisplay();
                    e.printStackTrace();
                }
            }//onResponse()
            //---------------------------------------------------------
            @Override public void onFailure(Call<Model> call, Throwable t) {
                mTxt_mainTitle_w_cityName.setText("Error:onFailure(): Can not get weather data - response from Wbsvc failed.");
                Log.i(TAG, "**ERROR:onFailure():Response from Yahoo-Weather-Web-Service FAILED");
            }
        });
    }//getWeatherDataFromYahooWebSvc
    //--------------------------------------------------------------
    void clearAllRemainingDisplay(){
        mTxt_statusLocal.setText("");
        mTxt_titleFiveDayForcast_w_cityName.setText("");
        // CLEAR old Weather-Forcast data from ArrayList
        mArrayListFcastItems.clear();
    }
    //-------------------------------------------------------------
    //Get only month and day (MAY 12) from full-date JAN 15, 2016
    String getMonth_n_day(String forecastDateString){
        StringBuilder month_n_day_SB = new StringBuilder(6);
        if (forecastDateString.length() < 6)
            return(forecastDateString) ;

        month_n_day_SB.append(forecastDateString.substring(3,6));
        month_n_day_SB.append(' ');
        month_n_day_SB.append(forecastDateString.substring(0,2));
        return month_n_day_SB.toString();
    }
    //-----------------------------------------------
    private static String centerString(String strIn, int totalLen){
        String strOut = String.format("%"+totalLen+"s%s%"+totalLen+"s", "",strIn,"");
        float mid = strOut.length() / 2;
        float start = mid - (totalLen/2);
        float end = start + totalLen;
        return strOut.substring((int)start, (int)end);
    }
    //------------------------------------------------
    String makeIt_13_charLong(String forecastStatus){
        if (forecastStatus.length() < 13){
            StringBuilder sb = new StringBuilder();
            sb.append(forecastStatus);
            int addNumberOfBlanks = 13 - sb.length();
            for (int i = 0; i <= addNumberOfBlanks; i++)
                sb.append(" ");
            return sb.toString();
        }
        if (forecastStatus.length() > 13){
            StringBuilder sb = new StringBuilder();
            sb.append(forecastStatus);
            int truncateEndChars = sb.length() - 13;
            sb.delete(13,sb.length());
            return sb.toString();
        }
        return(forecastStatus);
    }
    //-------------------------------------------------------------------
    public String yahooQueryLangFormat(String cityNameLocation) {
        /**************************************************
         This Query format taken from https://developer.yahoo.com/weather/
         select * from weather.forecast where woeid in (select woeid from geo.places(1) where text="nome, ak")
         **************************************************/
        return "select * from weather.forecast " +
                "where woeid in " +
                "(select woeid from geo.places(1) " +
                "where text= \" " + cityNameLocation + " \" )";
    }
    //-------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.appBarSearchIcon);
        if (searchItem != null) {
            mAppBarSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            EditText userInputSearchBox = (EditText) mAppBarSearchView.findViewById
                    (android.support.v7.appcompat.R.id.search_src_text);
            userInputSearchBox.setHint("Enter city-name here");
            userInputSearchBox.setHintTextColor(getResources().getColor(R.color.grey));
            userInputSearchBox.setTextColor(getResources().getColor(R.color.black));

            String userInput = userInputSearchBox.getText().toString();//ak - Jun9,2016

            View searchPlateView = mAppBarSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
            searchPlateView.setBackgroundColor(getResources().getColor(R.color.yellowLight));

            //Display user-entered search text
            mAppBarSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String userInputCityName) {
                    //mUserInput.setText("User entered: "+userInputCityName);
                    //mUserInput.setTextColor(getResources().getColor(R.color.blue));
                    mCityName = userInputCityName;
                    getWeatherDataFromYahooWebService(mCityName);
                    return false;
                }
                //--------------------------------
                @Override
                public boolean onQueryTextChange(String newText) {
                    // use this method for auto complete search process
                    return false;
                }
            });
            SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
            mAppBarSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        return super.onCreateOptionsMenu(menu);
        //return true;
    }
    //-------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //---------------------------------------------------------
    @Override
    public void onBackPressed() {
        if (!mAppBarSearchView.isIconified()) {
            mAppBarSearchView.setIconified(true);
            findViewById(R.id.appBarSearchIcon).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
    //-----------------------------------------------------------
}
