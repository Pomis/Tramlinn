package pomis.app.tallinnabuss.data;

import android.content.Context;
import android.support.annotation.Nullable;


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lombok.val;
import pomis.app.tallinnabuss.domain.TravelLeg;
import pomis.app.tallinnabuss.domain.TravelLegBuilder;
import pomis.app.tallinnabuss.domain.TransportStop;
import pomis.app.tallinnabuss.domain.TravelPoint;


public class CSVDB {
    static final double TALLINN_LAT = 59.436962;
    static final double TALLINN_LON = 24.753574;

    static public ArrayList<TransportStop> stopsFiltered;
    static public ArrayList<TravelLeg> travelLegs;

    @Deprecated
    static public ArrayList<TransportStop> readStops(Context context) throws IOException {

        val reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("stops.json"))
        );
        val gson = new Gson();
        val travelPoints = gson.fromJson(reader, TransportStop[].class);

        stopsFiltered = new ArrayList<>();
        for (val travelPoint : travelPoints) {
            if (Math.abs(travelPoint.stop_lat - TALLINN_LAT) < 0.1 &&
                    Math.abs(travelPoint.stop_lon - TALLINN_LON) < 0.1) {
                stopsFiltered.add(travelPoint);
            }
        }
        return stopsFiltered;
    }


    static public ArrayList<TransportStop> readTramStops(Context context) {
        stopsFiltered = new ArrayList<>();
        try {
            val isr = new InputStreamReader(context.getAssets().open("tram_stops.csv"));
            val reader = new BufferedReader(isr);
            String csvLine;
            reader.readLine();
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                TransportStop stop = new TransportStop();
                stop.stop_id = Integer.parseInt(row[1]);
                stop.pointName = row[3];
                stop.schedule = stringsToDates(row[6].split(" "));
                stop.stop_lat = Double.parseDouble(row[4]);
                stop.stop_lon = Double.parseDouble(row[5]);
                stop.lineNumber = row[2];
                stopsFiltered.add(stop);
            }
            isr.close();
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        }
        return stopsFiltered;
    }

    private static ArrayList<Date> stringsToDates(String[] split) {
        val dates = new ArrayList<Date>();
        val sdf = new SimpleDateFormat("HH:mm:ss");

        for (String s : split) {
            try {
                dates.add(sdf.parse(s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dates;
    }

    static public ArrayList<TravelLeg> loadAllRoutes(Context context) {
        travelLegs = new ArrayList<>();
        try {
            val reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("edges.csv"))
            );
            String csvLine;
            reader.readLine();
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                travelLegs.add(TravelLegBuilder.from(stopWithId(Integer.parseInt(row[1]), row[4]))
                        .to(stopWithId(Integer.parseInt(row[2]), row[4]))
                        .onTram(row[4]));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        }
        return travelLegs;
    }

    @Nullable
    static public TransportStop stopWithId(int id, String lineNumber) {
        for (val s : stopsFiltered) {
            if (s.stop_id == id && s.lineNumber.equals(lineNumber)) return s;
        }
        return null;
    }

    static public ArrayList<TransportStop> stopsWhere(String name) {
        val stops = new ArrayList<TransportStop>();
        for (val s : stopsFiltered) {
            if (s.pointName.equals(name))
                stops.add(s);
        }
        return stops;
    }

    static public ArrayList<TransportStop> stopsWhere(int id) {
        val stops = new ArrayList<TransportStop>();
        for (val s : stopsFiltered) {
            if (s.stop_id == id)
                stops.add(s);
        }
        return stops;
    }


    static public ArrayList<TravelPoint> findConnections(TravelPoint start) {
        val connections = new ArrayList<TravelPoint>();
        for (TravelLeg r : travelLegs) {
            if (r.starts.stop_id == start.stop_id)
                connections.add(r.finishes);
        }
        return connections;
    }

    static public ArrayList<TravelLeg> findRoutesFrom(int id) {
        val followingTravelLegs = new ArrayList<TravelLeg>();
        for (TravelLeg r : travelLegs) {
            if (r.starts.stop_id == id)
                followingTravelLegs.add(r);

        }
        return followingTravelLegs;
    }

    static public ArrayList<TravelLeg> findRoutesFrom(TravelPoint start) {
        val followingTravelLegs = new ArrayList<TravelLeg>();
        for (TravelLeg r : travelLegs) {
            if (r.starts.pointName.equals(start.pointName) &&
                    r.finishes.lineNumber.equals(start.lineNumber))
                followingTravelLegs.add(r);

        }
        return followingTravelLegs;
    }


}
