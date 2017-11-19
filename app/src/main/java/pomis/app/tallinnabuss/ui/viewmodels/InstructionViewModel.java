package pomis.app.tallinnabuss.ui.viewmodels;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntegerRes;
import android.widget.ImageView;
import android.widget.TextView;

import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import pomis.app.tallinnabuss.R;
import pomis.app.tallinnabuss.domain.TravelLeg;
import pomis.app.tallinnabuss.domain.TravelLegStorage;

/**
 * Created by romanismagilov on 17.11.17.
 */

@Layout(R.layout.item_instruction)
//@Animate(Animation.SCALE_UP_ASC)
public class InstructionViewModel extends TravelLeg implements ViewModel{

    InstructionViewModel() {
    }

    @View(R.id.tv_instruction)
    private TextView tvInstruction;
    @View(R.id.iv_type)
    private ImageView ivType;

    String instruction;
    Drawable typeImage;
    int color;

    @Resolve
    public void onResolve() {
        tvInstruction.setText(instruction);
        ivType.setImageDrawable(typeImage);
        ivType.setColorFilter(color);
    }


}
