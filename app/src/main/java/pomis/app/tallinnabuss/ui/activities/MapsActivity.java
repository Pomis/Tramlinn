package pomis.app.tallinnabuss.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import pomis.app.tallinnabuss.R;
import pomis.app.tallinnabuss.data.CSVDB;
import pomis.app.tallinnabuss.domain.Route;
import pomis.app.tallinnabuss.domain.TravelLeg;
import pomis.app.tallinnabuss.domain.TripPlan;
import pomis.app.tallinnabuss.domain.Way;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    enum State {
        START_IDLE, DESTINATION_IDLE, ROUTING
    }

    @BindView(R.id.rl_points)
    RelativeLayout rlPoints;
    @BindView(R.id.tv_hint)
    TextView tvHint;
    @BindView(R.id.tv_from)
    TextView tvFrom;
    @BindView(R.id.tv_to)
    TextView tvTo;

    private GoogleMap mMap;
    private ArrayList<TravelLeg> travelLegs;

    private State activityState = State.START_IDLE;
    private TripPlan tripPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        tripPlan = new TripPlan();
        try {
            travelLegs = CSVDB.readTramStops(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng tallinn = new LatLng(59.436962, 24.753574);
        initTouches();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tallinn, 12f));

        drawLines();
    }

    void drawLines() {
        ArrayList<Route> routes = CSVDB.loadAllRoutes(this);


        for (Route r : routes) {
            List<LatLng> latLngs = new ArrayList<>();
            latLngs.add(CSVDB.stopWithId(r.starts.stop_id, r.route_short_name).toLatLng());
            latLngs.add(CSVDB.stopWithId(r.finishes.stop_id, r.route_short_name).toLatLng());
            mMap.addPolyline(new PolylineOptions()
                    .addAll(latLngs)
                    .width(3).color(Color.GRAY)
                    .visible(true).clickable(true));
        }

    }



    void initTouches() {
        mMap.setOnMapClickListener(latLng -> {
            switch (activityState) {

                case START_IDLE:
                    tvHint.setText("Select destination point");
                    tvFrom.setText("Coords: " + latLng.toString());
                    addPedestrianMarker(latLng);
                    activityState = State.DESTINATION_IDLE;
                    tripPlan.departurePoint = latLng;
                    break;

                case DESTINATION_IDLE:
                    tvHint.setText("Points selected");
                    tvTo.setText("Coords: " + latLng.toString());
                    addPedestrianMarker(latLng);
                    activityState = State.ROUTING;
                    tripPlan.destinationPoint = latLng;
                    tripPlan.calculateClosestStations();
                    tvFrom.setText(Math.floor(tripPlan.straightDistanceDeparture*2200)+" min walk to " + tripPlan.departureStation.stop_name);
                    tvTo.setText(Math.floor(tripPlan.straightDistanceDestination*2200)+" min walk to " + tripPlan.destinationStation.stop_name);
                    addBusMarker(tripPlan.departureStation);
                    addBusMarker(tripPlan.destinationStation);

                    addWalkPolygons();
                    startCalculating();
                    break;
            }

        });
    }

    private void addWalkPolygons() {
//        List<LatLng> walkPointsDeparture = new ArrayList<>();
//        walkPointsDeparture.add(tripPlan.departurePoint);
//        walkPointsDeparture.add(new LatLng(
//                        tripPlan.departureStation.stop_lat,
//                        tripPlan.departureStation.stop_lon
//        ));
//
//        mMap.addPolyline(new PolylineOptions()
//                .addAll(walkPointsDeparture)
//                .width(10).color(Color.BLUE)
//                .visible(true).clickable(true));

    }

    private void startCalculating() {
        tripPlan.departureTime = getCurrentTime(new Date());
        Way w = tripPlan.calculateBestWay();
        if (w!=null) {
            Toasty.info(this, "You can reach it in "+w.getTimeToReach().getMinutes()).show();
            w.draw(this, mMap);
            tvFrom.setText(tvFrom.getText().toString()+"\n"+w.getTimeToReach().getMinutes()+
                    " min tram (incl. waiting)");
        }

    }

    /*
     * Default Java `new Date()` return timestamp containing both date + time,
     * we need only time.
     */
    private Date getCurrentTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(date.getTime());
        Toasty.info(this, "Starting time: "+time).show();
        try {
            return sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    void addPedestrianMarker(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(
                latLng
        ).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pedestrian)));
    }

    void addBusMarker(TravelLeg leg) {
        mMap.addMarker(new MarkerOptions().position(
                new LatLng(
                        leg.stop_lat,
                        leg.stop_lon
                )
        ).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus)));
    }
}