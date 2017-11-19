package pomis.app.tallinnabuss.domain;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

import lombok.val;
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

    ArrayList<TravelLegStorage> recursiveTravelPoints;

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

    public TravelLegStorage calculateBestWay() {
        val directWalkTravelLeg = TravelLegBuilder.from(departurePoint).to(destinationPoint).walking();
        val walkToStationTravelLeg = TravelLegBuilder.from(departurePoint).to(departureStation).walking();

        recursiveTravelPoints = new ArrayList<>();
        val departureStops = CSVDB.stopsWhere(departureStation.pointName);
        for (val stop : departureStops) {

            val waitTime = stop.waitForTransport(departureTime);
            val accum = new TravelLegStorage(waitTime);

            val connections = stop.getRoutes(false);
            for (TravelLeg r : connections) {
                r.iterate(this, departureTime, accum.mutate(r, waitTime), destinationStation);
            }

        }
        TravelLegStorage bestTravelLegStorage;
        if (recursiveTravelPoints.size() != 0) {
            bestTravelLegStorage = recursiveTravelPoints.get(0);
            for (TravelLegStorage w : recursiveTravelPoints) {
                if (w.getTimeToReach().getTime() < bestTravelLegStorage.getTimeToReach().getTime())
                    bestTravelLegStorage = w;
            }
            bestTravelLegStorage.addToEnd(walkToStationTravelLeg, walkToStationTravelLeg.travelTime);

            TravelLeg walkToDestination = TravelLegBuilder.from(bestTravelLegStorage.getLastTravelLeg()).to(destinationPoint)
                    .walking();
            bestTravelLegStorage.add(walkToDestination, walkToDestination.travelTime);

            if (directWalkTravelLeg.travelTime.getTime() < (walkToStationTravelLeg.travelTime.getTime() +
                    walkToDestination.travelTime.getTime()) ) {
                val walkTravelLegStorage = new TravelLegStorage();
                walkTravelLegStorage.add(directWalkTravelLeg, new Date());
                return walkTravelLegStorage;
            }
        } else {
            bestTravelLegStorage = new TravelLegStorage();
            bestTravelLegStorage.add(directWalkTravelLeg, new Date());
        }



        return bestTravelLegStorage;
    }
}

