package pomis.app.tallinnabuss.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.mindorks.placeholderview.PlaceHolderView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import lombok.val;
import pomis.app.tallinnabuss.R;
import pomis.app.tallinnabuss.data.CSVDB;
import pomis.app.tallinnabuss.domain.TravelLeg;
import pomis.app.tallinnabuss.domain.TravelPoint;
import pomis.app.tallinnabuss.domain.TripPlan;
import pomis.app.tallinnabuss.domain.TravelPoints;
import pomis.app.tallinnabuss.ui.viewmodels.TravelLegViewModel;
import pomis.app.tallinnabuss.ui.viewmodels.TravelLegViewModelFactory;

import static pomis.app.tallinnabuss.data.Const.DEFAULT_TIME;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    @BindView(R.id.phvInstructions)
    PlaceHolderView phvInstructions;

    enum State {
        START_IDLE, DESTINATION_IDLE, ROUTING
    }

    @BindView(R.id.rl_points)
    RelativeLayout rlPoints;
    @BindView(R.id.tv_hint)
    TextView tvHint;


    private GoogleMap mMap;
    private ArrayList<TravelPoint> travelPoints;

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
            travelPoints = CSVDB.readTramStops(this);
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

        drawTramLines();
    }

    void drawTramLines() {
        ArrayList<TravelLeg> travelLegs = CSVDB.loadAllRoutes(this);
        for (TravelLeg r : travelLegs) {
            List<LatLng> latLngs = new ArrayList<>();
            latLngs.add(CSVDB.stopWithId(r.starts.stop_id, r.routeName).toLatLng());
            latLngs.add(CSVDB.stopWithId(r.finishes.stop_id, r.routeName).toLatLng());
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
                    addPedestrianMarker(latLng);
                    activityState = State.DESTINATION_IDLE;
                    tripPlan.departurePoint = latLng;
                    break;

                case DESTINATION_IDLE:
                    tvHint.setText("Points selected");
                    addPedestrianMarker(latLng);
                    activityState = State.ROUTING;
                    tripPlan.destinationPoint = latLng;
                    tripPlan.calculateClosestStations();
//                    tvFrom.setText(Math.floor(tripPlan.straightDistanceDeparture * 2200) + " min walk to " + tripPlan.departureStation.stop_name);
//                    tvTo.setText(Math.floor(tripPlan.straightDistanceDestination * 2200) + " min walk to " + tripPlan.destinationStation.stop_name);
                    addBusMarker(tripPlan.departureStation);
                    addBusMarker(tripPlan.destinationStation);

                    startCalculating();
                    break;
            }

        });
    }

    private void startCalculating() {
        tripPlan.departureTime = getTime(null);
        TravelPoints w = tripPlan.calculateBestWay();
        if (w != null) {
            Toasty.info(this, "You can reach it in " + w.getTimeToReach().getMinutes()).show();
            w.draw(this, mMap);

            val routeViews = TravelLegViewModelFactory.getRouteViews(w.getRoutes());
            for (TravelLegViewModel rvm : routeViews) {
                phvInstructions.addView(rvm);
            }
            phvInstructions.refresh();
//            tvFrom.setText(tvFrom.getText().toString() + "\n" + w.getTimeToReach().getMinutes() +
//                    " min tram (incl. waiting)");
            initInstructions();
        }

    }

    private void initInstructions() {
        rlPoints.animate().yBy(-220).setDuration(1500).start();
    }

    /*
     * Default Java `new Date()` return timestamp containing both date + time,
     * we need only time.
     */
    private Date getTime(@Nullable Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        String time = (date == null) ? DEFAULT_TIME : sdf.format(date.getTime());

        Toasty.info(this, "Starting time: " + time).show();
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

    void addBusMarker(TravelPoint leg) {
        mMap.addMarker(new MarkerOptions().position(
                new LatLng(
                        leg.stop_lat,
                        leg.stop_lon
                )
        ).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus)));
    }
}
