package pomis.app.tallinnabuss.ui.viewmodels;

import java.util.ArrayList;

import lombok.val;
import pomis.app.tallinnabuss.domain.TravelLeg;

/**
 * Created by romanismagilov on 17.11.17.
 */

public class TravelLegViewModelFactory {

    static public ArrayList<TravelLegViewModel> getRouteViews(ArrayList<TravelLeg> travelLegs) {
        ArrayList<TravelLegViewModel> routeViews = new ArrayList<>();
        for (TravelLeg r : travelLegs) {
            val routeView = new TravelLegViewModel();
            routeView.routeName = r.routeName;
            routeView.travelTime = r.travelTime;

            switch (r.type) {
                case TRAM:
                    break;

                case INTERCHANGE:
                    routeView.instruction = "interchange";
                    break;

                case WALK:
                    routeView.instruction = "walking ";
                    break;
            }
            routeViews.add(routeView);
        }
        return routeViews;
    }
}
