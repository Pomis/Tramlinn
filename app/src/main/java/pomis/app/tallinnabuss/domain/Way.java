package pomis.app.tallinnabuss.domain;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pomis.app.tallinnabuss.R;
import pomis.app.tallinnabuss.data.CSVDB;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by romanismagilov on 15.11.17.
 */

public class Way {
    private ArrayList<Route> routes;
    private Date timeToReach;

    public Way(ArrayList<Route> way, Date timeToReach) {
        this.routes = way;
        this.timeToReach = timeToReach;
    }

    public Way() {
        this.timeToReach = new Date(0l);
        routes = new ArrayList<>();
    }

    public Way(Date timeToReach) {
        this.timeToReach = timeToReach;
        routes = new ArrayList<>();
    }


    public void add(Route route, Date additionalTime) {
        routes.add(route);
        timeToReach = new Date(timeToReach.getTime() + additionalTime.getTime());
    }

    public Way mutate(Route route, Date additionalTime) {
        ArrayList<Route> routesNew = ((ArrayList<Route>) routes.clone());
        routesNew.add(route);
        Way way = new Way(
                routesNew,
                new Date(this.timeToReach.getTime() + additionalTime.getTime())
        );
        return way;
    }

    public boolean contains(TravelLeg leg) {
        boolean contains = false;
        for (Route r : routes) {
            if (r.finishes == leg || r.starts == leg)
                contains = true;
        }
        return contains;
    }

    public int getDepth() {
        return routes.size();
    }

    public boolean contains(Route route) {
        boolean contains = false;
        for (Route r : routes) {
            contains |= r.starts.stop_name.equals(route.finishes.stop_name);
        }
        return contains;
    }

    public Date getTimeToReach() {
        return timeToReach;
    }

    public void draw(Context context, GoogleMap mMap) {
        //routes.remove(routes.size() - 1);
        Log.d("kek", "draw: size of way = " + routes.size());
        for (Route r : routes) {
            List<LatLng> latLngs = new ArrayList<>();
            latLngs.add(r.starts.toLatLng());
            latLngs.add(r.finishes.toLatLng());
            mMap.addPolyline(new PolylineOptions()
                    .addAll(latLngs)
                    .width(10).color(colorFromId(context, r.route_short_name))
                    .visible(true).clickable(true));
        }
    }

    int colorFromId(Context context, String id) {
        switch (id) {
            case "\"1\"":
                return context.getResources().getColor(R.color.color1);

            case "\"2\"":
                return context.getResources().getColor(R.color.color2);

            case "\"3\"":
                return context.getResources().getColor(R.color.color3);

            case "\"4\"":
                return context.getResources().getColor(R.color.color4);

            case "walk":
                return context.getResources().getColor(R.color.colorAccent);

        }
        return 0;
    }
}
