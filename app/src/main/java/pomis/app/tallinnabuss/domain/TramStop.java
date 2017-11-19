package pomis.app.tallinnabuss.domain;

import android.util.Log;

import java.util.Date;

/**
 * Created by romanismagilov on 17.11.17.
 */

public class TramStop extends TravelPoint {
    Date waitForBus(Date startTime) {
        for (Date s : schedule) {
            if (s.getTime() > startTime.getTime()) {
                Date diff = new Date(s.getTime() - startTime.getTime());
                Log.d("kek", "Waiting for bus on station " + pointName + ", line " + lineNumber + " for " + diff.getMinutes() + "minutes");
                return diff;
            }
        }
        return new Date(0l);
    }
}
