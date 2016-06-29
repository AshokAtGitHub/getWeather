package com.ashok.getweather;

import android.util.Log;

import com.ashok.getweather.api.RestApiClient;
import com.ashok.getweather.pojos.Channel;
import com.ashok.getweather.pojos.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
//---------------------------------------------------------
/**
 * Created by Ashok on 6/21/2016.
 */
public class WeatherDataUtils {
    enum EnumKeys_WeatherData{Response, CityStateName, Temperature , Status};
    static EnumKeys_WeatherData mEnumKeysWeatherData;
    private static final String TAG = "ak";

    //-----------------------------------------------------------
     static void getWeatherDataFromYahooWebService(
                            final String cityName,
                            final ArrayList<ForecastItems>mArrayListFcastItems){
         //Log.i(TAG, "Util:getWeatherDataFromYahooWebService():Enter");
         final Map<EnumKeys_WeatherData, String> mapReturnValues = new HashMap<>();//return weather data in this map

         String queryStringForYahooWebSvc = yahooQueryLangFormat(cityName);
         Call<Model> callRetrofit = RestApiClient.getDataUsingRetrofitAndRestApiInterface().
                                        queryIn_RestApi_Interface(queryStringForYahooWebSvc);
        //********
        callRetrofit.enqueue(new Callback<Model>() {
            //---------------------------------------------------------------------------------
            //call-back after weather-data come from Yahoo-weather-web-service
            @Override public void onResponse(Call<Model> callNotUsed, Response<Model> response){
                try {
                    //get reponse-data(JSON format) from Yahoo-weather-Web-svc
                    Channel channel = response.body().getQuery().getResults().getChannel();

                    String cityLocal = channel.getLocation().getCity();
                    String stateLocal = channel.getLocation().getRegion();
                    String countryLocal = channel.getLocation().getCountry();
                    String temperatureLocal = channel.getItem().getCondition().getTemp();
                    String statusLocal = channel.getItem().getCondition().getText();
                    String forecastStatus_0 = channel.getItem().getForecast().get(0).getText();
                    statusLocal = forecastStatus_0;
                    //String humidity = channel.getAtmosphere().getHumidity();
                    //String sunriseTime =
                    //channel.getAstronomy().getSunrise();
                    //String sunsetTime = channel.getAstronomy().getSunset();

                    //CLEAR old Weather-Forcast data from ArrayList
                    mArrayListFcastItems.clear();
                    /*--------------------------------------------------------------------
                     * Get 5-days Weather-Forecast-data from JSON-Array recvd. from Yahoo-Web-svc
                     *------------------------------------------------------------------------*/
                    for (int i = 0; i < 5; i++){
                        String forecastDay = channel.getItem().getForecast().get(i).getDay();
                        forecastDay = WeatherDataUtils.centerString(forecastDay, 5);
                        if (i == 0)
                            forecastDay = "Today";
                        //Log.i(TAG, "Response():Day1="+forecastDay + " Len="+forecastDay_1.length());
                        String forecastHi = channel.getItem().getForecast().get(i).getHigh();
                        String forecastLo = channel.getItem().getForecast().get(i).getLow();
                        String forecastStatus = channel.getItem().getForecast().get(i).getText();
                        //forecastStatus = makeIt_13_charLong(forecastStatus);
                        forecastStatus = WeatherDataUtils.centerString(forecastStatus, 17);
                        String forecastDate = channel.getItem().getForecast().get(i).getDate();
                        forecastDate = WeatherDataUtils.getMonth_n_day(forecastDate);
                        //Log.i(TAG, "Status_Len="+forecastStatus.length());
                        //Log.i(TAG, "Date="+forecastDate);
                        ForecastItems forecastItems = new ForecastItems(forecastDay,
                                forecastHi+ "\u00B0",
                                forecastLo+ "\u00B0",
                                forecastStatus,
                                forecastDate);

                        //add this forecastItems to ArrayList
                        mArrayListFcastItems.add(forecastItems);
                    }//for
                    //mAdapterForEachRow_Fcast.notifyDataSetChanged();
                    //----------------------------------------------------------
                    //Valid Response from Yahoo Web Service

                    //Display CURRENT weather at top on display-screen
                    String cityAndState = null;
                    if (null != cityLocal) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Weather - " + cityLocal);
                        if (null != stateLocal)
                            sb.append(", "+stateLocal);
                        else if (null != countryLocal)
                            sb.append(", " + countryLocal);
                            cityAndState = sb.toString();
                        //mTxt_mainTitle_w_cityName.setText(cityAndState);
                    }
                    //Log.i(TAG,"Util: cityState = " + cityAndState + " Temp="+temperatureLocal);
                    MainActivity.displayWeather(cityAndState, temperatureLocal, statusLocal);
                } catch (Exception e) {
                    Log.i(TAG, "*** Exception - onResponce()");
                    MainActivity.display_No_WeatherDataAvailable();
                    //mTxt_mainTitle_w_cityName.setText("Sorry. No weather-data available for:");
                    //mTxt_temperatureLocal.setText(cityName);
                    //mapReturnValues.put(mEnumKeysWeatherData.Response, "exception");
                    e.printStackTrace();
                }
            }//onResponse()
            //------------------------------------------------------------
            @Override public void onFailure(Call<Model> call, Throwable t) {
                //mTxt_mainTitle_w_cityName.setText("Error:onFailure(): Can not get weather data - response from Wbsvc failed.");
                Log.i(TAG, "**ERROR:onFailure():Response from Yahoo-Weather-Web-Service FAILED");

                //mapReturnValues.put(mEnumKeysWeatherData, "failed");
            }
        });
         //return mapReturnValues;
    }//getWeatherDataFromYahooWebSvc
    //--------------------------------------------------------------------
    static String yahooQueryLangFormat(String cityNameLocation) {
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
    //Get only month and day; Example: get "MAY 12" from full-date "MAY 12, 2016"
    static String getMonth_n_day(String forecastDateString){
        StringBuilder month_n_day_SB = new StringBuilder(6);
        if (forecastDateString.length() < 6)
            return(forecastDateString) ;
        month_n_day_SB.append(forecastDateString.substring(3,6));
        month_n_day_SB.append(' ');
        month_n_day_SB.append(forecastDateString.substring(0,2));
        return month_n_day_SB.toString();
    }
    //-----------------------------------------------------------
    static String centerString(String strIn, int totalLen){
        String strOut = String.format("%"+totalLen+"s%s%"+totalLen+"s", "",strIn,"");
        float mid = strOut.length() / 2;
        float start = mid - (totalLen/2);
        float end = start + totalLen;
        return strOut.substring((int)start, (int)end);
    }
    //-------------------------------------------------------------
}
