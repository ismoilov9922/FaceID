package uz.faceid.faceidcompany.customviews;

import static androidx.core.math.MathUtils.clamp;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import uz.faceid.faceidcompany.R;


public class BoundingBox extends RelativeLayout {
    private final TextView boxNameTextView;
    private final View boundingBox;

    public BoundingBox(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.bounding_box, this, true);

        boxNameTextView = (TextView) getChildAt(0);
        boxNameTextView.setText("");

        boundingBox = (View) getChildAt(1);
    }

    /**
     * Set visibility of the bounding box.
     *
     * @param visible true if visible, false if not.
     */
    public void setVisibility(boolean visible) {
        boxNameTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
        boundingBox.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setPosition(int l, int t, int r, int b) {
        // Set position of box
        LayoutParams boxParams = new LayoutParams(r - l, b - t);
        boxParams.leftMargin = l;
        boxParams.topMargin = t;
        boundingBox.setLayoutParams(boxParams);

        // Set position of text
        // Use dp unit to provide consistency for differently sized screens
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int dpHeight = (int) (displayMetrics.heightPixels / displayMetrics.density);
        int textHeight = clamp((int) ((b - t) / 3 * displayMetrics.density),
                dpHeight / 25,
                dpHeight / 10);

        LayoutParams textParams = new LayoutParams(LayoutParams.WRAP_CONTENT, textHeight);
        textParams.leftMargin = l;
        textParams.topMargin = t - textHeight;
        textParams.height = textHeight;
        boxNameTextView.setLayoutParams(textParams);
    }

    /**
     * Set caption of the bounding box.
     *
     * @param text caption to assign.
     */
    public void setBoxCaption(String text) {
        if (text.length() > 2) {
            boxNameTextView.setTextColor(Color.GREEN);
            boxNameTextView.setText(text);
            boundingBox.setBackgroundResource(R.drawable.bounding_box_sucess);
        } else {
            boxNameTextView.setTextColor(Color.RED);
            boxNameTextView.setText("Not found");
            boundingBox.setBackgroundResource(R.drawable.bounding_box);
        }
    }
}
