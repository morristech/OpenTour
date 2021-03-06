package org.wepush.open_tour.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wepush.open_tour.structures.DB1SqlHelper;
import org.wepush.open_tour.structures.Site;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by antoniocoppola on 29/05/15.
 */
public class ReadFromJson extends IntentService {

    private String stringFromJson;
    public ReadFromJson(){
        super("ReadFromJson");
    }

    @Override
    protected void onHandleIntent(Intent intent){


        try {




            if (TextUtils.equals(Locale.getDefault().getDisplayLanguage(), "English")){

                stringFromJson = jsonToStringFromAssetFolder("milandb_en", getApplication());
            } else {

                stringFromJson = jsonToStringFromAssetFolder("milandb_ita", getApplication());
            }

            parseResult(new JSONObject(stringFromJson));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            System.out.println(e.toString());

        }



    }


    private void parseResult(JSONObject obj){

        DB1SqlHelper.getInstance(this).deleteDb(this);

        try {
            ArrayList<Site> siteToReturn = new ArrayList<Site>();
            JSONArray arrayFromJson = obj.getJSONArray("sites");

            for (int i = 0; i < arrayFromJson.length(); i++) {

                Site site = new Site();

                JSONObject singleSite = arrayFromJson.getJSONObject(i);
                site.id = singleSite.getString("id");
                site.name = singleSite.getString("name");
                site.description = singleSite.getString("description");
                site.tips = singleSite.getString("tips");

                JSONObject locationInfo = singleSite.getJSONObject("location");
                site.address = locationInfo.getString("street_name");
                site.addressCivic = locationInfo.getString("street_number");
                JSONArray coordinates = locationInfo.getJSONArray("coordinates");

                site.latitude = coordinates.getDouble(0);
                site.longitude = coordinates.getDouble(1);

                //PICTURES
                JSONArray jsonArrayPicture=singleSite.getJSONArray("pictures");
                JSONObject pictureJson=new JSONObject();
                try {
                    pictureJson = jsonArrayPicture.getJSONObject(0);
                } catch (JSONException e){
                    pictureJson=null;
                }
                if (pictureJson!=null){
                    site.pictureUrl=pictureJson.getString("picture");
                }else {
                    site.pictureUrl="placeholder";
                }




                //sezione tickets
                JSONArray jsonTickets=singleSite.getJSONArray("tickets");
                //sezione contacts
                JSONArray jsonContacts=singleSite.getJSONArray("contacts");
                //sezione openings
                JSONArray jsonOpenings=singleSite.getJSONArray("openings");
                site.openings=jsonOpenings.toString();
                site.tickets=jsonTickets.toString();
                site.contacts=jsonContacts.toString();
                site.visitTime=singleSite.getInt("visit_time");
                Log.d("miotag"," VISIT TIME acquired: "+site.visitTime);
//sezione TYPE OF SITE
                site.typeOfSite=singleSite.getString("type");

                try {
                       if (TextUtils.equals(singleSite.getString("always_open"),"true")) {
                        site.alwaysOpen = 1;
                        } else {
                            site.alwaysOpen=0;
                            }
                    } catch(JSONException e){
                        e.printStackTrace();
                        }

//sezione nearest sites





            siteToReturn.add(site);


            }



            DB1SqlHelper.getInstance(this).addSites(siteToReturn);

             }  catch (JSONException e){
                 System.out.println(e.toString());
        }
    }

    public static String jsonToStringFromAssetFolder(String fileName,Context context) throws IOException {
        AssetManager manager = context.getAssets();

        InputStream file = manager.open(fileName);

        byte[] data = new byte[file.available()];

        file.read(data);
        file.close();
        return new String(data);
    }
}
