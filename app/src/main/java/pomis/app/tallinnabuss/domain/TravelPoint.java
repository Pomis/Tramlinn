package pomis.app.tallinnabuss.domain;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

import lombok.val;
import pomis.app.tallinnabuss.data.CSVDB;

import static pomis.app.tallinnabuss.data.Const.CENTRAL_STATION;

/**
 * Created by romanismagilov on 12.11.17.
 */

public abstract class TravelPoint {

    public int stop_id;

    public String pointName;

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



    ArrayList<TravelLeg> getRoutes(boolean interchanging) {
        if (travelLegs == null) {
            travelLegs = CSVDB.findRoutesFrom(this);
            val neighbors = CSVDB.stopsWhere(pointName);
            for (TravelPoint s : neighbors) {
                boolean isNeighbor = s.pointName.equals(pointName) && !s.lineNumber.equals(lineNumber);
                if (pointName.equals(CENTRAL_STATION) && interchanging && isNeighbor
                        || !interchanging && isNeighbor) {
                    travelLegs.add(TravelLegBuilder.from(this).to(s).interchanging());

                }
            }
        }
        return travelLegs;
    }
}

