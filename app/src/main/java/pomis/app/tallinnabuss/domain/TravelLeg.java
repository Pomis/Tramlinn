package pomis.app.tallinnabuss.domain;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

import pomis.app.tallinnabuss.data.CSVDB;

/**
 * Created by romanismagilov on 12.11.17.
 */

public class TravelLeg {

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
        Log.d("kek", "waitForBus: its too late");
        return new Date(0l);
    }

    ArrayList<Route> getRoutes(boolean interchanging) {
        if (routes == null) {
            routes = CSVDB.findRoutesFrom(this);
            ArrayList<TravelLeg> neighbors = CSVDB.stopsWhere(stop_name);
            for (TravelLeg s : neighbors) {
                boolean isNeighbor = s.stop_name.equals(stop_name) && !s.lineNumber.equals(lineNumber);

                if (stop_name.equals("\"Hobujaama\"") && interchanging && isNeighbor) {
                    routes.add(new Route(this, s, "walk"));
                    Log.d("kek", "getRoutes: added interchange " + stop_name + " #" + lineNumber
                            + " -> " + s.stop_name + " #" + s.lineNumber);
                } else if (!interchanging && isNeighbor) {
                    routes.add(new Route(this, s, "walk"));
                    Log.d("kek", "getRoutes: added interchange " + stop_name + " #" + lineNumber
                            + " -> " + s.stop_name + " #" + s.lineNumber);


                }

            }
        }
        return routes;
    }
}

