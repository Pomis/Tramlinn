package pomis.app.tallinnabuss.domain;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by romanismagilov on 17.11.17.
 */

public class RouteBuilder {
    private TravelLeg travelLeg;

    private RouteBuilder(TravelPoint from) {
        this.travelLeg = new TravelLeg();
        this.travelLeg.starts = from;
    }

    static public RouteBuilder from(TravelPoint from) {
        return new RouteBuilder(from);
    }


    static public RouteBuilder from(LatLng from) {
        return new RouteBuilder(new Address(from));
    }

    public RouteBuilder to(TravelPoint from) {
        travelLeg.finishes = from;
        return this;
    }


    public RouteBuilder to(LatLng from) {
        travelLeg.finishes = new Address(from);
        return this;
    }

    public TravelLeg walking() {
        travelLeg.type = TravelLeg.Type.WALK;
        travelLeg.travelTime = new Date((long) sqrt(pow(travelLeg.starts.stop_lat - travelLeg.finishes.stop_lat, 2) +
                pow(travelLeg.starts.stop_lon - travelLeg.finishes.stop_lon, 2)) * 60 * 1000);
        return travelLeg;
    }

    public TravelLeg interchanging() {
        travelLeg.type = TravelLeg.Type.INTERCHANGE;
        travelLeg.travelTime = new Date((long) sqrt(pow(travelLeg.starts.stop_lat - travelLeg.finishes.stop_lat, 2) +
                pow(travelLeg.starts.stop_lon - travelLeg.finishes.stop_lon, 2)) * 60 * 1000);
        return travelLeg;
    }

    public TravelLeg onTram(String routeName) {
        travelLeg.type = TravelLeg.Type.TRAM;
        travelLeg.routeName = routeName;
        return travelLeg;
    }
}
