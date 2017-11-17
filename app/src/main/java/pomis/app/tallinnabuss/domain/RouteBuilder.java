package pomis.app.tallinnabuss.domain;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by romanismagilov on 17.11.17.
 */

public class RouteBuilder {
    private Route route;

    private RouteBuilder(TravelLeg from) {
        this.route = new Route();
        this.route.starts = from;
    }

    static public RouteBuilder from(TravelLeg from) {
        return new RouteBuilder(from);
    }


    static public RouteBuilder from(LatLng from) {
        return new RouteBuilder(new Address(from));
    }

    public RouteBuilder to(TravelLeg from) {
        route.finishes = from;
        return this;
    }


    public RouteBuilder to(LatLng from) {
        route.finishes = new Address(from);
        return this;
    }

    public Route walking() {
        route.routeName = "walk";
        route.diff = new Date((long) sqrt(pow(route.starts.stop_lat - route.finishes.stop_lat, 2) +
                pow(route.starts.stop_lon - route.finishes.stop_lon, 2)) * 60 * 1000);
        return route;
    }

    public Route interchanging() {
        route.routeName = "interchanging";
        route.diff = new Date((long) sqrt(pow(route.starts.stop_lat - route.finishes.stop_lat, 2) +
                pow(route.starts.stop_lon - route.finishes.stop_lon, 2)) * 60 * 1000);
        return route;
    }

    public Route onTram(String routeName) {
        route.routeName = routeName;
        return route;
    }
}
