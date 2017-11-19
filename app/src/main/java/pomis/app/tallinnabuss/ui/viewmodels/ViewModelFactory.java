package pomis.app.tallinnabuss.ui.viewmodels;

import android.content.Context;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lombok.val;
import pomis.app.tallinnabuss.R;
import pomis.app.tallinnabuss.domain.TravelLeg;
import pomis.app.tallinnabuss.domain.TravelLegStorage;

/**
 * Created by romanismagilov on 17.11.17.
 */

public class ViewModelFactory {

    static public ArrayList<ViewModel> getRouteViews(Context context,
                                                     ArrayList<TravelLeg> travelLegs,
                                                     Date startTime) {
        val routeViews = new ArrayList<ViewModel>();
        val sdf = new SimpleDateFormat("HH:mm");

        for (TravelLeg r : travelLegs) {
            val routeView = new InstructionViewModel();
            routeView.routeName = r.routeName;
            routeView.travelTime = r.travelTime;

            switch (r.type) {
                case TRAM:
                    startTime = new Date(startTime.getTime() + r.travelTime.getTime());
                    routeView.typeImage = context.getResources().getDrawable(R.drawable.ic_tram);
                    routeView.instruction = r.finishes.pointName;
                    routeView.color = TravelLegStorage.colorRoute(context, r);
                    routeViews.add(routeView);
                    break;

                case INTERCHANGE:
                    val time = new TimeViewModel();
                    time.timeToShow = sdf.format(startTime);
                    routeViews.add(time);

                    routeView.instruction = "waiting for tram " + r.finishes.lineNumber;
                    routeViews.add(routeView);
                    startTime = new Date(startTime.getTime() + r.travelTime.getTime());


                    val time2 = new TimeViewModel();
                    time2.timeToShow = sdf.format(startTime);
                    routeViews.add(time2);
                    break;

                case WALK:
                    val time3 = new TimeViewModel();
                    time3.timeToShow = sdf.format(startTime);
                    routeViews.add(time3);

                    routeView.typeImage = context.getResources()
                            .getDrawable(R.drawable.ic_pedestrian);
                    routeView.instruction = "walking to " + r.finishes.pointName;
                    if (!r.finishes.pointName.equals("Destination point")) {
                        routeView.instruction += " and waiting for tram";
                    }
                    routeViews.add(routeView);
                    startTime = new Date(startTime.getTime() + r.travelTime.getTime());

                    val time4 = new TimeViewModel();
                    time4.timeToShow = sdf.format(startTime);
                    routeViews.add(time4);
                    break;
            }
        }
        return routeViews;
    }
}
