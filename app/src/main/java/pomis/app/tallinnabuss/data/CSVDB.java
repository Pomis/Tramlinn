package pomis.app.tallinnabuss.data;

import android.content.Context;
import android.support.annotation.Nullable;


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import pomis.app.tallinnabuss.domain.Route;
import pomis.app.tallinnabuss.domain.TravelLeg;


public class CSVDB {
    static final double TALLINN_LAT = 59.436962;
    static final double TALLINN_LON = 24.753574;

    static public ArrayList<TravelLeg> stopsFiltered;
    static public ArrayList<Route> routes;

    @Deprecated
    static public ArrayList<TravelLeg> readStops(Context context) throws IOException {

        Reader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("stops.json"))
        );
        Gson gson = new Gson();
        TravelLeg[] travelLegs = gson.fromJson(reader, TravelLeg[].class);

        stopsFiltered = new ArrayList<>();
        for (TravelLeg travelLeg : travelLegs) {
            if (Math.abs(travelLeg.stop_lat - TALLINN_LAT) < 0.1 &&
                    Math.abs(travelLeg.stop_lon - TALLINN_LON) < 0.1) {
                stopsFiltered.add(travelLeg);
            }
        }
        return stopsFiltered;
    }


    static public ArrayList<TravelLeg> readTramStops(Context context) {
        stopsFiltered = new ArrayList<>();
        try {
            InputStreamReader isr = new InputStreamReader(context.getAssets().open("tram_stops.csv"));
            BufferedReader reader = new BufferedReader(isr);
            String csvLine;
            reader.readLine();
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                TravelLeg stop = new TravelLeg();
                stop.stop_id = Integer.parseInt(row[1]);
                stop.stop_name = row[3];
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
        ArrayList<Date> dates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        for (String s : split) {
            try {
                dates.add(sdf.parse(s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dates;
    }

    static public ArrayList<Route> loadAllRoutes(Context context) {
        routes = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("edges.csv"))
            );
            String csvLine;
            reader.readLine();
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                routes.add(new Route(
                        stopWithId(Integer.parseInt(row[1]), row[4]),
                        stopWithId(Integer.parseInt(row[2]), row[4]),
                        row[4]
                ));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        }
        return routes;
    }

    @Nullable
    static public TravelLeg stopWithId(int id, String lineNumber) {
        for (TravelLeg s : stopsFiltered) {
            if (s.stop_id == id && s.lineNumber.equals(lineNumber)) return s;
        }
        return null;
    }

    static public ArrayList<TravelLeg> stopsWhere(String name) {
        ArrayList<TravelLeg> stops = new ArrayList<>();
        for (TravelLeg s : stopsFiltered) {
            if (s.stop_name.equals(name))
                stops.add(s);
        }
        return stops;
    }

    static public ArrayList<TravelLeg> stopsWhere(int id) {
        ArrayList<TravelLeg> stops = new ArrayList<>();
        for (TravelLeg s : stopsFiltered) {
            if (s.stop_id == id)
                stops.add(s);
        }
        return stops;
    }


    static public ArrayList<TravelLeg> findConnections(TravelLeg start) {
        ArrayList<TravelLeg> connections = new ArrayList<>();
        for (Route r : routes) {
            if (r.starts.stop_id == start.stop_id)
                connections.add(r.finishes);
        }
        return connections;
    }

    static public ArrayList<Route> findRoutesFrom(int id) {
        ArrayList<Route> followingRoutes = new ArrayList<>();
        for (Route r : routes) {
            if (r.starts.stop_id == id)
                followingRoutes.add(r);

        }
        return followingRoutes;
    }

    static public ArrayList<Route> findRoutesFrom(TravelLeg start) {
        ArrayList<Route> followingRoutes = new ArrayList<>();
        for (Route r : routes) {
            if (r.starts.stop_name.equals(start.stop_name) &&
                    r.finishes.lineNumber.equals(start.lineNumber))
                followingRoutes.add(r);

        }
        return followingRoutes;
    }


}
