package pomis.app.tallinnabuss.domain;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

import pomis.app.tallinnabuss.data.CSVDB;

import static java.lang.Math.*;

/**
 * Created by romanismagilov on 13.11.17.
 */

public class TripPlan {

    public LatLng departurePoint;

    public LatLng destinationPoint;

    public Date departureTime;

    public TravelLeg departureStation;

    public TravelLeg destinationStation;

    public double straightDistanceDeparture;

    public double straightDistanceDestination;

    ArrayList<Way> recusiveWays;

    Date currentMinTime;

    public void calculateClosestStations() {
        currentMinTime = new Date();
        straightDistanceDeparture = 1000;
        straightDistanceDestination = 1000;
        for (TravelLeg stop : CSVDB.stopsFiltered) {
            // departure
            double dist =
                    sqrt(pow(stop.stop_lat - departurePoint.latitude, 2) +
                            pow(stop.stop_lon - departurePoint.longitude, 2));
            if (dist < straightDistanceDeparture) {
                straightDistanceDeparture = dist;
                departureStation = stop;
            }
            // destination
            dist = sqrt(pow(stop.stop_lat - destinationPoint.latitude, 2) +
                    pow(stop.stop_lon - destinationPoint.longitude, 2));
            if (dist < straightDistanceDestination) {
                straightDistanceDestination = dist;
                destinationStation = stop;
            }
        }
    }

    public Way calculateBestWay() {
        Route directWalkRoute = new Route(new Address(departurePoint),
                new Address(destinationPoint),
                "walk");
        Route walkToStationRoute = new Route(new Address(departurePoint),
                departureStation, "walk");

        if (directWalkRoute.getWalkTime() < walkToStationRoute.getWalkTime()) {
            Way walkWay = new Way();
            walkWay.add(directWalkRoute, new Date());
            return walkWay;
        }


        recusiveWays = new ArrayList<>();
        ArrayList<TravelLeg> departureStops = CSVDB.stopsWhere(departureStation.stop_name);
        for (TravelLeg stop : departureStops) {

            Date waitTime = stop.waitForBus(departureTime);
            Way accum = new Way(waitTime);

            ArrayList<Route> connections = stop.getRoutes(false);
            for (Route r : connections) {
                r.iterate(this, departureTime, accum.mutate(r, waitTime), destinationStation);
            }

        }
        Way bestWay;
        if (recusiveWays.size() != 0) {
            bestWay = recusiveWays.get(0);
            for (Way w : recusiveWays) {
                if (w.getTimeToReach().getTime() < bestWay.getTimeToReach().getTime())
                    bestWay = w;
            }
            bestWay.add(walkToStationRoute, new Date(0l));
        } else {
            bestWay = new Way();
            bestWay.add(directWalkRoute, new Date());
        }

        Route walkToDestination = new Route(
                bestWay.getLastTravelLeg(),
                new Address(destinationPoint),
                "walk"
        );
        bestWay.add(walkToStationRoute, walkToDestination.getWalkTimeDate());
        return bestWay;
    }
}

