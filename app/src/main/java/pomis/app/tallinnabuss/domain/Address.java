package pomis.app.tallinnabuss.domain;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by romanismagilov on 16.11.17.
 */

public class Address extends TravelPoint {
    public Address(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.lineNumber = "0";
    }
}
