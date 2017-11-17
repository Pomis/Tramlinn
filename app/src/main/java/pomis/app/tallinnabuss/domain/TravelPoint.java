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

public abstract class TravelPoint {

    public int stop_id;

    public String stop_name;

    public double stop_lat;

    public double stop_lon;

    public ArrayList<Date> schedule;

    public String lineNumber;

    private ArrayList<TravelLeg> travelLegs;

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

    ArrayList<TravelLeg> getRoutes(boolean interchanging) {
        if (travelLegs == null) {
            travelLegs = CSVDB.findRoutesFrom(this);
            ArrayList<TravelPoint> neighbors = CSVDB.stopsWhere(stop_name);
            for (TravelPoint s : neighbors) {
                boolean isNeighbor = s.stop_name.equals(stop_name) && !s.lineNumber.equals(lineNumber);
                if (stop_name.equals(CENTRAL_STATION) && interchanging && isNeighbor
                        || !interchanging && isNeighbor) {
                    travelLegs.add(RouteBuilder.from(this).to(s).walking());

                }
            }
        }
        return travelLegs;
    }
}

