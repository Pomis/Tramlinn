package pomis.app.tallinnabuss.domain;

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

    public TravelPoint departureStation;

    public TravelPoint destinationStation;

    private double straightDistanceDeparture;

    private double straightDistanceDestination;

    ArrayList<TravelPoints> recursiveTravelPoints;

    Date currentMinTime;

    public void calculateClosestStations() {
        currentMinTime = new Date();
        straightDistanceDeparture = 1000;
        straightDistanceDestination = 1000;
        for (TravelPoint stop : CSVDB.stopsFiltered) {
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

    public TravelPoints calculateBestWay() {
        TravelLeg directWalkTravelLeg = RouteBuilder.from(departurePoint).to(destinationPoint).walking();
        TravelLeg walkToStationTravelLeg = RouteBuilder.from(departurePoint).to(departureStation).walking();

        recursiveTravelPoints = new ArrayList<>();
        ArrayList<TravelPoint> departureStops = CSVDB.stopsWhere(departureStation.stop_name);
        for (TravelPoint stop : departureStops) {

            Date waitTime = stop.waitForBus(departureTime);
            TravelPoints accum = new TravelPoints(waitTime);


            ArrayList<TravelLeg> connections = stop.getRoutes(false);
            for (TravelLeg r : connections) {
                r.iterate(this, departureTime, accum.mutate(r, waitTime), destinationStation);
            }

        }
        TravelPoints bestTravelPoints;
        if (recursiveTravelPoints.size() != 0) {
            bestTravelPoints = recursiveTravelPoints.get(0);
            for (TravelPoints w : recursiveTravelPoints) {
                if (w.getTimeToReach().getTime() < bestTravelPoints.getTimeToReach().getTime())
                    bestTravelPoints = w;
            }
            bestTravelPoints.addToEnd(walkToStationTravelLeg, new Date(0l));

            TravelLeg walkToDestination = RouteBuilder.from(bestTravelPoints.getLastTravelLeg()).to(destinationPoint)
                    .walking();
            bestTravelPoints.add(walkToDestination, walkToDestination.travelTime);

            if (directWalkTravelLeg.travelTime.getTime() < (walkToStationTravelLeg.travelTime.getTime() +
                    walkToDestination.travelTime.getTime()) ) {
                TravelPoints walkTravelPoints = new TravelPoints();
                walkTravelPoints.add(directWalkTravelLeg, new Date());
                return walkTravelPoints;
            }
        } else {
            bestTravelPoints = new TravelPoints();
            bestTravelPoints.add(directWalkTravelLeg, new Date());
        }



        return bestTravelPoints;
    }
}

