package pomis.app.tallinnabuss.ui.viewmodels;

import com.mindorks.placeholderview.Animation;
import com.mindorks.placeholderview.annotations.Animate;
import com.mindorks.placeholderview.annotations.Layout;

import pomis.app.tallinnabuss.R;
import pomis.app.tallinnabuss.domain.Route;
import pomis.app.tallinnabuss.domain.TravelLeg;

/**
 * Created by romanismagilov on 17.11.17.
 */

@Layout(R.layout.item_route)
@Animate(Animation.SCALE_UP_ASC)
public class RouteViewModel extends Route {


    public RouteViewModel(TravelLeg starts, TravelLeg finishes, String route_short_name) {
        super(starts, finishes, route_short_name);
    }


}
