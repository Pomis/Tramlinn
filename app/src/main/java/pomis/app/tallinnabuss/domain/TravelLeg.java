package pomis.app.tallinnabuss.domain;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

import pomis.app.tallinnabuss.data.CSVDB;

import static pomis.app.tallinnabuss.data.Const.CENTRAL_STATION;

/**
 * Created by romanismagilov on 12.11.17.
 */

public abstract class TravelLeg {

    public int stop_id;

    public String stop_name;

    public double stop_lat;

    public double stop_lon;

    public ArrayList<Date> schedule;

    public String lineNumber;

    private ArrayList<Route> routes;

    public LatLng toLatLng() {
        return new LatLng(
                stop_lat,
                stop_lon
        );
    }

    Date waitForBus(Date startTime) {
        for (Date s : schedule) {
            if (s.getTime() > startTime.getTime()) {
                Date diff = new Date(s.getTime() - startTime.getTime());
                Log.d("kek", "Waiting for bus on station " + stop_name + ", line " + lineNumber + " for " + diff.getMinutes() + "minutes");
                return diff;
            }
        }
        return new Date(0l);
    }

    ArrayList<Route> getRoutes(boolean interchanging) {
        if (routes == null) {
            routes = CSVDB.findRoutesFrom(this);
            ArrayList<TravelLeg> neighbors = CSVDB.stopsWhere(stop_name);
            for (TravelLeg s : neighbors) {
                boolean isNeighbor = s.stop_name.equals(stop_name) && !s.lineNumber.equals(lineNumber);
                if (stop_name.equals(CENTRAL_STATION) && interchanging && isNeighbor
                        || !interchanging && isNeighbor) {
                    routes.add(RouteBuilder.from(this).to(s).walking());

                }
            }
        }
        return routes;
    }
}

