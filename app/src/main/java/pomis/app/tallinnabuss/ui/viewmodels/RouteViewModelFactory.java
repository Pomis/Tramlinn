package pomis.app.tallinnabuss.ui.viewmodels;

import java.util.ArrayList;

import lombok.val;
import pomis.app.tallinnabuss.domain.Route;

/**
 * Created by romanismagilov on 17.11.17.
 */

public class RouteViewModelFactory {

    static public ArrayList<RouteViewModel> getRouteViews(ArrayList<Route> routes) {
        ArrayList<RouteViewModel> routeViews = new ArrayList<>();
        for (Route r : routes) {
            val routeView = new RouteViewModel();
            routeView.routeName = r.routeName;
            routeView.diff = r.diff;
            switch (r.routeName) {

                case "walk":
                    routeView.instruction = "walk";
                    break;

                case "interchanging":
                    routeView.instruction = "interchange";
                    break;

                default:
                    routeView.instruction = "tram";
                    break;
            }
            routeViews.add(routeView);
        }
        return routeViews;
    }
}
