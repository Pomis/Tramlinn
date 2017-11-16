package pomis.app.tallinnabuss.domain;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by romanismagilov on 16.11.17.
 */

public class Address extends TravelLeg {
    public Address(LatLng latLng) {
        this.stop_lat = latLng.latitude;
        this.stop_lon = latLng.longitude;
        this.lineNumber = "0";
    }
}
