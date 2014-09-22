package com.mtomczak.nausicaa;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * An square graphic panel
 */
class GraphicPanel extends View {
  public GraphicPanel(Context ctx, AttributeSet attrs) {
    super(ctx, attrs);
  }

  @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Square off the dials by width
    setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
  }
}
