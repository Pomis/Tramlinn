package pomis.app.tallinnabuss.domain;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static pomis.app.tallinnabuss.data.Const.DEPTH_LIMIT;

/**
 * Created by romanismagilov on 14.11.17.
 */

public class Route {

    public TravelLeg starts;

    public TravelLeg finishes;

    public String route_short_name;

    public Route(TravelLeg starts, TravelLeg finishes, String route_short_name) {
        this.starts = starts;
        this.finishes = finishes;
        this.route_short_name = route_short_name;
    }

    /*
         * Get minutes between two stations to move by tram. This depends on start time
         * because schedule differs
         */
    public void iterate(TripPlan plan, Date startTime, Way accumulator, TravelLeg destination) {
        if (starts.stop_name.equals(destination.stop_name)) {
            plan.recusiveWays.add(accumulator);
            if (plan.currentMinTime.getTime() < accumulator.getTimeToReach().getTime())
                plan.currentMinTime = accumulator.getTimeToReach();
            Log.d("kek", "iterate: end of recursion, dest reached:" + accumulator.getTimeToReach().getMinutes());
        } else if (plan.recusiveWays.size() > DEPTH_LIMIT) {
            Log.d("kek", "iterate: recursion terminated due to depth");
        } else if (plan.currentMinTime.getTime() < accumulator.getTimeToReach().getTime()) {
            Log.d("kek", "iterate: recursion terminated due to ineffectiveness");
        } else {// if (starts.lineNumber.equals(finishes.lineNumber)) {
            boolean go = false;
            for (Date d : finishes.schedule) {
                if (d.getTime() > startTime.getTime()) {
                    go = true;
                    Date diff = new Date(d.getTime() - startTime.getTime());
                    Log.d("kek", "iterate:" + starts.stop_name + " station reached. Elapsed time " + diff.getMinutes());
                    Log.d("kek", "iterate: " + starts.stop_name + "(id " + starts.stop_id + " (" + starts.lineNumber + ")) -> "
                            + finishes.stop_name + "(id " + finishes.stop_id + " (" + finishes.lineNumber + ")); accumTime: " + accumulator.getTimeToReach().getMinutes()
                            + "; destination: " + destination.stop_name + "(" + destination.stop_id + ")");
                    // check if it is destination
                    if (finishes.stop_name.equals(destination.stop_name)
                            || finishes.stop_id == destination.stop_id) {
                        plan.recusiveWays.add(accumulator);
                        Log.d("kek", "iterate: end of recursion, dest reached:" + accumulator.getTimeToReach().getMinutes());
                    } else {
                        ArrayList<Route> interchanges = finishes.getRoutes(true);
                        for (Route r : interchanges) {
                            if (!accumulator.contains(r) && r.finishes != starts)
                                 r.iterate(plan, d, accumulator.mutate(r, diff), destination);
                        }
                    }

                    break;
                }
            }
            if (!go) Log.d("kek", "iterate: was in " + starts.stop_name + "(id " + starts.stop_id
                    + "), but stopped due to no more trams. Depth: " + accumulator.getDepth());
        }
    }


    public double getWalkTime() {

        return sqrt(pow(starts.stop_lat - finishes.stop_lat, 2) +
                pow(starts.stop_lon - finishes.stop_lon, 2));
    }
}