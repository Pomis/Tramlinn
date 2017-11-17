package pomis.app.tallinnabuss.domain;

import android.util.Log;
import java.util.ArrayList;
import java.util.Date;
import static pomis.app.tallinnabuss.data.Const.DEPTH_LIMIT;

/**
 * Created by romanismagilov on 14.11.17.
 */

public class TravelLeg {
    public enum Type {
        TRAM, INTERCHANGE, WALK
    }

    public Type type;

    public TravelPoint starts;

    public TravelPoint finishes;

    public String routeName;

    public Date travelTime;

    protected TravelLeg(){}

    /*
     * Get minutes between two stations to move by tram. This depends on start time
     * because schedule differs
     * */
    void iterate(TripPlan plan, Date startTime, TravelPoints accumulator, TravelPoint destination) {
        if (starts.stop_name.equals(destination.stop_name)) {
            plan.recursiveTravelPoints.add(accumulator);
            if (plan.currentMinTime.getTime() < accumulator.getTimeToReach().getTime())
                plan.currentMinTime = accumulator.getTimeToReach();
            Log.d("kek", "iterate: end of recursion, dest reached:" + accumulator.getTimeToReach().getMinutes());
        } else if (plan.recursiveTravelPoints.size() > DEPTH_LIMIT) {
            Log.d("kek", "iterate: recursion terminated due to depth");
        } else if (plan.currentMinTime.getTime() < accumulator.getTimeToReach().getTime()) {
            Log.d("kek", "iterate: recursion terminated due to ineffectiveness");
        } else {// if (starts.lineNumber.equals(finishes.lineNumber)) {
            boolean go = false;
            for (Date d : finishes.schedule) {
                if (d.getTime() > startTime.getTime()) {
                    go = true;
                    travelTime = new Date(d.getTime() - startTime.getTime());
                    Log.d("kek", "iterate:" + starts.stop_name + " station reached. Elapsed time " + travelTime.getMinutes());
                    Log.d("kek", "iterate: " + starts.stop_name + "(id " + starts.stop_id + " (" + starts.lineNumber + ")) -> "
                            + finishes.stop_name + "(id " + finishes.stop_id + " (" + finishes.lineNumber + ")); accumTime: " + accumulator.getTimeToReach().getMinutes()
                            + "; destination: " + destination.stop_name + "(" + destination.stop_id + ")");
                    // check if it is destination
                    if (finishes.stop_name.equals(destination.stop_name)
                            || finishes.stop_id == destination.stop_id) {
                        plan.recursiveTravelPoints.add(accumulator);
                        Log.d("kek", "iterate: end of recursion, dest reached:" + accumulator.getTimeToReach().getMinutes());
                    } else {
                        ArrayList<TravelLeg> interchanges = finishes.getRoutes(true);
                        for (TravelLeg r : interchanges) {
                            if (!accumulator.contains(r) && r.finishes != starts)
                                r.iterate(plan, d, accumulator.mutate(r, travelTime), destination);
                        }
                    }

                    break;
                }
            }
            if (!go) Log.d("kek", "iterate: was in " + starts.stop_name + "(id " + starts.stop_id
                    + "), but stopped due to no more trams. Depth: " + accumulator.getDepth());
        }
    }

}
