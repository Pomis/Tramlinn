package pomis.app.tallinnabuss.ui.viewmodels;

import android.widget.TextView;

import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import pomis.app.tallinnabuss.R;
import pomis.app.tallinnabuss.domain.TravelLeg;

/**
 * Created by romanismagilov on 17.11.17.
 */

@Layout(R.layout.item_time)
//@Animate(Animation.SCALE_UP_ASC)
public class TimeViewModel extends TravelLeg implements ViewModel {
    private Runnable callback;

    TimeViewModel() {
    }

    public TimeViewModel(Runnable callback, String timeToShow) {
        this.callback = callback;
        this.timeToShow = timeToShow;
    }

    @View(R.id.tv_time)
    private TextView tvTime;

    public String timeToShow;

    @Resolve
    public void onResolve() {
        tvTime.setText(timeToShow);
    }

    @Click(R.id.tv_time)
    public void onClick() {
        if (callback != null) {
            callback.run();
        }
    }

}
