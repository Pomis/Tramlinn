package pomis.app.tallinnabuss.ui.viewmodels;

import android.widget.ImageView;
import android.widget.TextView;

import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import lombok.val;
import pomis.app.tallinnabuss.R;
import pomis.app.tallinnabuss.domain.TravelLeg;

/**
 * Created by romanismagilov on 17.11.17.
 */

@Layout(R.layout.item_route)
//@Animate(Animation.SCALE_UP_ASC)
public class TravelLegViewModel extends TravelLeg {

    TravelLegViewModel() {
    }

    @View(R.id.tv_time)
    private TextView tvTime;
    @View(R.id.tv_instruction)
    private TextView tvInstruction;
    @View(R.id.iv_type)
    private ImageView ivType;

    String instruction;

    @Resolve
    private void onResolve() {
        val sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        tvTime.setText(sdf.format(travelTime));
        tvInstruction.setText(instruction);
    }


}
