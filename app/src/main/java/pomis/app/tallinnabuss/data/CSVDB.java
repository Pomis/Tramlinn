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

import pomis.app.tallinnabuss.domain.TravelLeg;
import pomis.app.tallinnabuss.domain.RouteBuilder;
import pomis.app.tallinnabuss.domain.TramStop;
import pomis.app.tallinnabuss.domain.TravelPoint;


public class CSVDB {
    static final double TALLINN_LAT = 59.436962;
    static final double TALLINN_LON = 24.753574;

    static public ArrayList<TravelPoint> stopsFiltered;
    static public ArrayList<TravelLeg> travelLegs;

    @Deprecated
    static public ArrayList<TravelPoint> readStops(Context context) throws IOException {

        Reader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("stops.json"))
        );
        Gson gson = new Gson();
        TravelPoint[] travelPoints = gson.fromJson(reader, TravelPoint[].class);

        stopsFiltered = new ArrayList<>();
        for (TravelPoint travelPoint : travelPoints) {
            if (Math.abs(travelPoint.stop_lat - TALLINN_LAT) < 0.1 &&
                    Math.abs(travelPoint.stop_lon - TALLINN_LON) < 0.1) {
                stopsFiltered.add(travelPoint);
            }
        }
        return stopsFiltered;
    }


    static public ArrayList<TravelPoint> readTramStops(Context context) {
        stopsFiltered = new ArrayList<>();
        try {
            InputStreamReader isr = new InputStreamReader(context.getAssets().open("tram_stops.csv"));
            BufferedReader reader = new BufferedReader(isr);
            String csvLine;
            reader.readLine();
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                TramStop stop = new TramStop();
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

    static public ArrayList<TravelLeg> loadAllRoutes(Context context) {
        travelLegs = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("edges.csv"))
            );
            String csvLine;
            reader.readLine();
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                travelLegs.add(RouteBuilder.from(stopWithId(Integer.parseInt(row[1]), row[4]))
                        .to(stopWithId(Integer.parseInt(row[2]), row[4]))
                        .onTram(row[4]));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        }
        return travelLegs;
    }

    @Nullable
    static public TravelPoint stopWithId(int id, String lineNumber) {
        for (TravelPoint s : stopsFiltered) {
            if (s.stop_id == id && s.lineNumber.equals(lineNumber)) return s;
        }
        return null;
    }

    static public ArrayList<TravelPoint> stopsWhere(String name) {
        ArrayList<TravelPoint> stops = new ArrayList<>();
        for (TravelPoint s : stopsFiltered) {
            if (s.stop_name.equals(name))
                stops.add(s);
        }
        return stops;
    }

    static public ArrayList<TravelPoint> stopsWhere(int id) {
        ArrayList<TravelPoint> stops = new ArrayList<>();
        for (TravelPoint s : stopsFiltered) {
            if (s.stop_id == id)
                stops.add(s);
        }
        return stops;
    }


    static public ArrayList<TravelPoint> findConnections(TravelPoint start) {
        ArrayList<TravelPoint> connections = new ArrayList<>();
        for (TravelLeg r : travelLegs) {
            if (r.starts.stop_id == start.stop_id)
                connections.add(r.finishes);
        }
        return connections;
    }

    static public ArrayList<TravelLeg> findRoutesFrom(int id) {
        ArrayList<TravelLeg> followingTravelLegs = new ArrayList<>();
        for (TravelLeg r : travelLegs) {
            if (r.starts.stop_id == id)
                followingTravelLegs.add(r);

        }
        return followingTravelLegs;
    }

    static public ArrayList<TravelLeg> findRoutesFrom(TravelPoint start) {
        ArrayList<TravelLeg> followingTravelLegs = new ArrayList<>();
        for (TravelLeg r : travelLegs) {
            if (r.starts.stop_name.equals(start.stop_name) &&
                    r.finishes.lineNumber.equals(start.lineNumber))
                followingTravelLegs.add(r);

        }
        return followingTravelLegs;
    }


}
