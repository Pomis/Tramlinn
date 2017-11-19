package pomis.app.tallinnabuss.domain;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import lombok.val;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by romanismagilov on 17.11.17.
 */

public class TravelLegBuilder {
    private TravelLeg travelLeg;

    private TravelLegBuilder(TravelPoint from) {
        this.travelLeg = new TravelLeg();
        this.travelLeg.starts = from;
    }

    static public TravelLegBuilder from(TravelPoint from) {
        return new TravelLegBuilder(from);
    }


    static public TravelLegBuilder from(LatLng from) {
        val address = new Address(from);
        address.pointName = "Departure point";
        return new TravelLegBuilder(address);
    }

    public TravelLegBuilder to(TravelPoint from) {
        travelLeg.finishes = from;
        return this;
    }


    public TravelLegBuilder to(LatLng from) {
        travelLeg.finishes = new Address(from);
        travelLeg.finishes.pointName = "Destination point";
        return this;
    }

    public TravelLeg walking() {
        travelLeg.type = TravelLeg.Type.WALK;
        val time = sqrt(pow(travelLeg.starts.stop_lat - travelLeg.finishes.stop_lat, 2) +
                pow(travelLeg.starts.stop_lon - travelLeg.finishes.stop_lon, 2)) * 2200 * 60 * 1000;
        travelLeg.travelTime = new Date((long) time);
        return travelLeg;
    }

    public TravelLeg interchanging() {
        travelLeg.type = TravelLeg.Type.INTERCHANGE;
        val time = sqrt(pow(travelLeg.starts.stop_lat - travelLeg.finishes.stop_lat, 2) +
                pow(travelLeg.starts.stop_lon - travelLeg.finishes.stop_lon, 2)) * 2200 * 60 * 1000;
        travelLeg.travelTime = new Date((long) time);
        return travelLeg;
    }

    public TravelLeg onTram(String routeName) {
        travelLeg.type = TravelLeg.Type.TRAM;
        travelLeg.routeName = routeName;
        return travelLeg;
    }
}
