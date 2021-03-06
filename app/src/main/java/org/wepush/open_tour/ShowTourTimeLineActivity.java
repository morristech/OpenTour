
package org.wepush.open_tour;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;

import org.wepush.open_tour.fragments_dialogs.InsufficientSettingsDialogFragment;
import org.wepush.open_tour.fragments_dialogs.ListViewTimeLineFragment;
import org.wepush.open_tour.services.TourAlgorithmTask;
import org.wepush.open_tour.utils.Constants;
import org.wepush.open_tour.structures.FloatingActionButton;
import org.wepush.open_tour.structures.Site;
import org.wepush.open_tour.utils.Repository;
import org.wepush.open_tour.utils.SphericalMercator;
import org.wepush.open_tour.utils.TimeLineAdapter;


import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.TimeZone;

public class ShowTourTimeLineActivity extends AppCompatActivity {
    public ProgressBar progressBar;
    public LinearLayout ll1;
    private ListView lw;
    private static LayoutInflater inflater;
    private ListViewTimeLineFragment listViewTimeLineFragment;
    public ArrayList<Site> siteToStamp;
    private ArrayList<String> idSitesToShow;
    private ArrayList<String> showTimeSitesToShow;

//17/07 section for osm drod
    private final static int ZOOM=17;
    private org.osmdroid.views.MapView map;
    private IMapController mapController;
    private MapEventsOverlay overlayNoEventos;
    private MapEventsReceiver mapNoEventsReceiver;


    private ArrayList<org.osmdroid.bonuspack.overlays.Marker> geoPointMarkers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.showtourtimeline_activity);



        progressBar=(ProgressBar)findViewById(R.id.progressBarTourTimeLine);
        progressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN);






        new TourAlgorithmTask(this, "random").execute();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarTimeLine);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);



        lw = (ListView) findViewById(R.id.lwShowTimeLine);
        inflater= getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.listview_header_with_map, lw, false);
        header.setClickable(false);

        lw.addHeaderView(header);


        ImageView backArrow=(ImageView) findViewById(R.id.imageArrowNavigationShowTourTimeLine);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HomeActivity.destroyTourPreferences(getBaseContext());
                siteToStamp.clear();

                startActivity(new Intent(getBaseContext(), SettingTourActivity.class));
                finish();
            }
        });


        FloatingActionButton fabButton = new FloatingActionButton.Builder(this)
                .withDrawable(getResources().getDrawable(R.mipmap.ic_floating_play))
                .withButtonColor(getResources().getColor(R.color.orange500))
                .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
                .withMargins(0, 0, 16, 16)
                .create();

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i=new Intent(getBaseContext(),LiveMapActivity.class);
                i.setAction(Constants.INTENT_FROM_SHOWTOURTL);
                i.putStringArrayListExtra("id",idSitesToShow);
                i.putStringArrayListExtra("showingTime",showTimeSitesToShow);
                Log.d("miotag","intent"+i.getStringArrayListExtra("id"));
                startActivity(i);
                finish();

                }
            });


    }//fine onCreate

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HomeActivity.destroyTourPreferences(getBaseContext());
        siteToStamp.clear();
        startActivity(new Intent(getBaseContext(), SettingTourActivity.class));
        finish();
    }





    public void showResultFromAlgorithm() {
        LinearLayout llView=(LinearLayout) this.findViewById(R.id.linearLayoutList);
        llView.setBackgroundColor(this.getResources().getColor(R.color.white));


//riempimento RecyclerView


        TextView firstElement=(TextView) findViewById(R.id.txtTitleFirstElement);
        firstElement.setText(getResources().getString(R.string.startingTour));

        TextView addressElement=(TextView) findViewById(R.id.txtAddressFirstElement);
        addressElement.setText(Repository.retrieve(this, Constants.WHERE_SAVE, String.class));

        TextView timeElement=(TextView) findViewById(R.id.txtStartingTimeTimeLine);
        timeElement.setText(Repository.retrieve(this,Constants.STARTING_TIME_READABLE_FORMAT,String.class));

        //altri elementi
        listViewTimeLineFragment = new ListViewTimeLineFragment();

        for (int i = 0; i < siteToStamp.size(); i++) {
        }
        lw.setAdapter(new TimeLineAdapter(this, siteToStamp));

        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = position - 1;

                if (position > -1) {

                    Intent intentToShowSite = new Intent(getBaseContext(), ShowDetailsActivity.class);
                    intentToShowSite.setAction(Constants.INTENT_FROM_SHOWTOURTL);
                    intentToShowSite.putExtra("siteId", siteToStamp.get(position).id);
                    startActivity(intentToShowSite);



                }
            }
        });


        //preparazione dell'array di id e showingTime da passare a LiveMapActivity per la visualizzazione/interazione

        idSitesToShow = new ArrayList<>();
        for (Site site : siteToStamp) {
            idSitesToShow.add(site.id);
        }

        showTimeSitesToShow = new ArrayList<>();
        for (Site site : siteToStamp) {
            showTimeSitesToShow.add(site.showingTime);
        }

        XYTileSource mCustomTileSource=new XYTileSource("MapQuest",
                ResourceProxy.string.mapquest_osm, 13, 17, 300, ".jpg", new String[]{
                "http://otile1.mqcdn.com/tiles/1.0.0/map/",
                "http://otile2.mqcdn.com/tiles/1.0.0/map/",
                "http://otile3.mqcdn.com/tiles/1.0.0/map/",
                "http://otile4.mqcdn.com/tiles/1.0.0/map/"});



        map = (org.osmdroid.views.MapView) findViewById(R.id.mapSummaryTL);


        map.setTileSource(mCustomTileSource);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(false);
        map.setUseDataConnection(false);
        mapController = map.getController();
        mapController.setZoom(ZOOM);
        mapController.setCenter(findTourCenter(siteToStamp));

        mapNoEventsReceiver=new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                Toast.makeText(getBaseContext(), R.string.goLiveFloating,Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint geoPoint) {
                Toast.makeText(getBaseContext(), R.string.goLiveFloating,Toast.LENGTH_SHORT).show();
               return false;
            }
        };

        overlayNoEventos = new MapEventsOverlay(this, mapNoEventsReceiver);
        map.getOverlays().add(overlayNoEventos);


//taking advantage of this for, it'll also complete statistic fields
        //price
        double totalPrice=0.0;
        TextView txtSummaryMoney=(TextView)findViewById(R.id.txtMoneySummary);

        //tour duration
        TextView txtTourLasting=(TextView) findViewById(R.id.txtTimeSummary);
        txtTourLasting.setText(Repository.retrieve(this,Constants.TIME_TO_SPEND,String.class));

        //n. visiting site
        TextView txtNumberSites=(TextView) findViewById(R.id.txtNumberSiteSummary);
        txtNumberSites.setText(String.valueOf(siteToStamp.size()));

        //total km
        double totalKM=0.0;
        TextView txtDistance=(TextView) findViewById(R.id.txtKmSummary);

        geoPointMarkers=new ArrayList<>();

        for (Site site: siteToStamp){

            GeoPoint gp=new GeoPoint(site.latitude,site.longitude);
            org.osmdroid.bonuspack.overlays.Marker mark = new org.osmdroid.bonuspack.overlays.Marker(map);
            mark.setPosition(gp);
            mark.setIcon(getResources().getDrawable(R.drawable.pin_red));
            mark.setTitle(site.name);
            map.getOverlays().add(mark);
            map.invalidate();
            geoPointMarkers.add(mark);

            //statistics:

            //price

            totalPrice=totalPrice+showPrice(site);

        }
            txtSummaryMoney.setText(String.valueOf(totalPrice)+" €");

        //km
            txtDistance.setText(String.valueOf(showDistance())+" km");

        //time spent
           txtTourLasting.setText(convertSecondToHHMMString(Double.valueOf(Repository.retrieve(this,Constants.TIME_TO_SPEND,String.class))));




    }//fine showResultTimeLine



private GeoPoint findTourCenter(ArrayList<Site> a){
        double latitude = 0.0;
        double longitude = 0.0;
        for (Site s : a){
            latitude=latitude+s.latitude;
            longitude=longitude+s.longitude;
        }
            GeoPoint gp=new GeoPoint(latitude/(Double.valueOf(a.size())),longitude/(Double.valueOf(a.size())));
        return gp;
    }


    public  void showDummyActivity(){

        FragmentManager fm = getSupportFragmentManager();
        InsufficientSettingsDialogFragment hf = new InsufficientSettingsDialogFragment();
        hf.show(fm, "badsettings_fragment");

    }

    private double showPrice(Site site){

        JSONArray jsonTickets=new JSONArray();
        String ticketsJson=site.tickets;



        try {
            jsonTickets = new JSONArray(ticketsJson);

            if(jsonTickets!=null && jsonTickets.length()>0) {
                    JSONObject jsonTick=jsonTickets.getJSONObject(0); //got the i-position of ticket array as Object

                  return   Double.valueOf(jsonTick.getString("price"));

            }


        } catch(JSONException e){
            e.printStackTrace();

        }
        return 0.0;
    }

    private String showDistance(){
        double d=0.0;
                for (int i=0; i<siteToStamp.size()-1; i++){
                    d=d+ SphericalMercator.getDistanceFromLatLonInKm(siteToStamp.get(i), siteToStamp.get(i + 1));
                }

                    final DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    df.setRoundingMode(RoundingMode.HALF_UP);
                   return df.format(d);


    }

    private String convertSecondToHHMMString(double secondingTime)
    {
        int secondTime=(int)Math.round(secondingTime);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        df.setTimeZone(tz);
        String time = df.format(new Date(secondTime*1000L));

        return time;

    }


    @Override
    public void onPause(){
        super.onPause();
        if (map != null) {
           for (org.osmdroid.bonuspack.overlays.Marker mark: geoPointMarkers){
               map.getOverlays().remove(mark);
               map.getOverlays().clear();
               map.getTileProvider().createTileCache();
               map.getTileProvider().detach();
           }
        }

    }


}//fine Classe
