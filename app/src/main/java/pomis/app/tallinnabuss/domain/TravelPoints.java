package pomis.app.tallinnabuss.domain;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;

import pomis.app.tallinnabuss.R;

/**
 * Created by romanismagilov on 15.11.17.
 */

public class TravelPoints {


    private ArrayList<TravelLeg> travelLegs;
    private Date timeToReach;

    public ArrayList<TravelLeg> getRoutes() {
        return travelLegs;
    }

    public TravelPoints(ArrayList<TravelLeg> way, Date timeToReach) {
        this.travelLegs = way;
        this.timeToReach = timeToReach;
    }

    public TravelPoints() {
        this.timeToReach = new Date(0l);
        travelLegs = new ArrayList<>();
    }

    public TravelPoints(Date timeToReach) {
        this.timeToReach = timeToReach;
        travelLegs = new ArrayList<>();
    }


    public void add(TravelLeg travelLeg, Date additionalTime) {
        travelLegs.add(travelLeg);
        timeToReach = new Date(timeToReach.getTime() + additionalTime.getTime());
    }

    public void addToEnd(TravelLeg travelLeg, Date additionalTime) {
        travelLegs.add(0, travelLeg);
        timeToReach = new Date(timeToReach.getTime() + additionalTime.getTime());
    }


    public TravelPoints mutate(TravelLeg travelLeg, Date additionalTime) {
        ArrayList<TravelLeg> routesNew = ((ArrayList<TravelLeg>) travelLegs.clone());
        routesNew.add(travelLeg);
        TravelPoints travelPoints = new TravelPoints(
                routesNew,
                new Date(this.timeToReach.getTime() + additionalTime.getTime())
        );
        return travelPoints;
    }


    public int getDepth() {
        return travelLegs.size();
    }

    public boolean contains(TravelLeg travelLeg) {
        boolean contains = false;
        for (TravelLeg r : travelLegs) {
            contains |= r.starts.stop_name.equals(travelLeg.finishes.stop_name);
        }
        return contains;
    }

    public Date getTimeToReach() {
        return timeToReach;
    }

    public void draw(Context context, GoogleMap mMap) {
        //routes.remove(routes.size() - 1);
        Log.d("kek", "draw: size of way = " + travelLegs.size());
        for (TravelLeg r : travelLegs) {
            mMap.addPolyline(new PolylineOptions()
                    .add(r.starts.toLatLng())
                    .add(r.finishes.toLatLng())
                    .width(10).color(colorRoute(context, r))
                    .visible(true));
        }
    }

    public TravelPoint getLastTravelLeg() {
        return travelLegs.get(travelLegs.size() - 1).finishes;
    }

    int colorRoute(Context context, TravelLeg travelLeg) {
        int nullColor = 0;
        switch (travelLeg.type) {
            case TRAM:
                switch (travelLeg.routeName) {
                    case "\"1\"":
                        return context.getResources().getColor(R.color.color1);

                    case "\"2\"":
                        return context.getResources().getColor(R.color.color2);

                    case "\"3\"":
                        return context.getResources().getColor(R.color.color3);

                    case "\"4\"":
                        return context.getResources().getColor(R.color.color4);
                }
                break;
            case INTERCHANGE:
            case WALK:
                return context.getResources().getColor(R.color.colorAccent);
        }

        return nullColor;

    }
}
