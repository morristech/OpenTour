package org.wepush.open_tour.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.wepush.open_tour.R;
import org.wepush.open_tour.structures.DB1SqlHelper;
import org.wepush.open_tour.structures.Site;


import java.util.ArrayList;

/**
 * Created by Antonio on 23/04/2015.
 */
public class TimeLineAdapter extends BaseAdapter{

    private LayoutInflater layoutInflater;
    private ArrayList<Site>siteToShowInList=new ArrayList<>();
    private Context context;

    public TimeLineAdapter(Context ctx,ArrayList<Site> showingListOfSite){

        context=ctx;
        siteToShowInList=showingListOfSite;
        layoutInflater = LayoutInflater.from(context);

    }

    @Override
    public Object getItem(int position) {
        return siteToShowInList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return siteToShowInList.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_element_tm, null);
            holder = new ViewHolder();
            holder.vTitle = (TextView) convertView.findViewById(R.id.txtTitle);
            holder.vDescription = (TextView) convertView.findViewById(R.id.txtAddress);
            holder.vTime = (TextView) convertView.findViewById(R.id.txtTime);
            holder.vPlaceHolder= (ImageView) convertView.findViewById(R.id.imgPlaceHolder);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.vTitle.setText(siteToShowInList.get(position).name);
        holder.vDescription.setText(siteToShowInList.get(position).address+","+siteToShowInList.get(position).addressCivic);
        holder.vTime.setText(String.valueOf(siteToShowInList.get(position).showingTime));


        String imagePath= DB1SqlHelper.getInstance(context).getPictureSite(siteToShowInList.get(position).id);
        String imagePathToUse="";

        if(TextUtils.equals(imagePath, "placeholder")){
            imagePathToUse="header_milan";
        } else {
            imagePathToUse = imagePath.substring(79, imagePath.length()-4);
        }

//        Ottengo la giusta immagine del monumento da impostare nella toolbar
        int drawableResource=context.getResources().getIdentifier(imagePathToUse, "drawable", context.getPackageName());
        try {
            holder.vPlaceHolder.setImageDrawable(context.getResources().getDrawable(drawableResource));
        } catch (Resources.NotFoundException e){
            Log.d("miotag","eccezione!!!!! immagine mancante: "+imagePathToUse);
            imagePathToUse="header_milan";
            int drawableResourceNoPicture=context.getResources().getIdentifier(imagePathToUse, "drawable", context.getPackageName());
            holder.vPlaceHolder.setImageDrawable(context.getResources().getDrawable(drawableResourceNoPicture));
        }
        return convertView;
    }

    static class ViewHolder {
        TextView vTitle;
        TextView vDescription;
        TextView vTime;

        ImageView vPlaceHolder;

    }
}




