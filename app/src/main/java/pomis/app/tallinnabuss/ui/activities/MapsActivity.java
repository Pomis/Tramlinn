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
import pomis.app.tallinnabuss.domain.TransportStop;
import pomis.app.tallinnabuss.domain.TravelLeg;
import pomis.app.tallinnabuss.domain.TravelLegStorage;
import pomis.app.tallinnabuss.domain.TravelPoint;
import pomis.app.tallinnabuss.domain.TripPlan;
import pomis.app.tallinnabuss.ui.viewmodels.TimeViewModel;
import pomis.app.tallinnabuss.ui.viewmodels.ViewModelFactory;

import static pomis.app.tallinnabuss.data.Const.DEFAULT_TIME;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    enum State {
        START_IDLE, DESTINATION_IDLE, ROUTING
    }

    @BindView(R.id.phvInstructions)
    PlaceHolderView phvInstructions;
    @BindView(R.id.rl_points)
    RelativeLayout rlPoints;
    @BindView(R.id.tv_hint)
    TextView tvHint;


    private GoogleMap mMap;
    private ArrayList<TransportStop> travelPoints;
    private Date selectedTime;
    TimeViewModel startTimePicker;
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
        startTimePicker = new TimeViewModel(this::setStartTime, "Select time (default "+DEFAULT_TIME+")");

        phvInstructions.addView(startTimePicker);
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
                    addTramMarker(tripPlan.departureStation);
                    addTramMarker(tripPlan.destinationStation);

                    startCalculating();
                    break;
            }

        });
    }

    void setStartTime() {
        val dialog = com.wdullaer.materialdatetimepicker.time.TimePickerDialog
                .newInstance((view, hourOfDay, minute, second) -> {
                    selectedTime = getTime(new Date(1970, 0, 0, hourOfDay, minute));
                    startTimePicker.timeToShow = hourOfDay + ":" + minute;
                }, true);
        dialog.show(getFragmentManager(), "dialogie");
    }

    private void startCalculating() {
        tripPlan.departureTime = getTime(selectedTime);
        TravelLegStorage w = tripPlan.calculateBestWay();
        if (w != null) {
            Toasty.info(this, "You can reach it in " + w.getTimeToReach().getMinutes()).show();
            w.draw(this, mMap);

            phvInstructions.removeAllViews();
            val routeViews = ViewModelFactory.getRouteViews(this, w.getRoutes(), tripPlan.departureTime);
            for (val rvm : routeViews) {
                phvInstructions.addView(rvm);
            }
            phvInstructions.refresh();
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
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

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

    void addTramMarker(TravelPoint leg) {
        mMap.addMarker(new MarkerOptions().position(
                new LatLng(
                        leg.stop_lat,
                        leg.stop_lon
                )
        ).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_tram)));
    }
}
