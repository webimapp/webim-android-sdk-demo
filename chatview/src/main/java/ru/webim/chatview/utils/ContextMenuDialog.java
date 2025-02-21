package ru.webim.chatview.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import androidx.cardview.widget.CardView;

import ru.webim.chatview.R;

public class ContextMenuDialog extends AnchorMenuDialog {
    private Matrix matrix = new Matrix();
    private Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float dimAmount;

    public ContextMenuDialog(Context context) {
        super(context, R.style.Base_Theme_AppCompat_Dialog);
    }

    @Override
    protected void onMenuCalculated(int[] anchorViewPosition) {
        drawVisibleArea(anchorViewPosition);
    }

    public void setDimAmount(float dimAmount) {
        this.dimAmount = dimAmount;
    }

    public float getDimAmount() {
        return dimAmount;
    }

    private void drawVisibleArea(int[] positionArray) {
        Bitmap bitmap = Bitmap.createBitmap(visibleWindowWidth, visibleWindowHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.argb((int) (255 * dimAmount), 0, 0, 0));
        Bitmap visibleViewBitmap = bitmapFromView(anchorView);

        int bottomPositionVisibleView = positionArray[1] + anchorView.getHeight() - indentTop;
        int topPositionVisibleView = positionArray[1] - indentTop;
        int leftPositionVisibleView = positionArray[0] - indentLeft;
        int rightPositionVisibleView = positionArray[0] + anchorView.getWidth() - indentLeft;

        if (anchorView instanceof CardView) {
            CardView cardView = (CardView) anchorView;
            BitmapShader bitmapShader = new BitmapShader(visibleViewBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            matrix.reset();
            matrix.setTranslate(positionArray[0] - indentLeft, positionArray[1] - indentTop);
            bitmapShader.setLocalMatrix(matrix);
            bitmapPaint.setShader(bitmapShader);
            canvas.drawRoundRect(
                new RectF(leftPositionVisibleView, topPositionVisibleView, rightPositionVisibleView, bottomPositionVisibleView),
                cardView.getRadius(),
                cardView.getRadius(),
                bitmapPaint);
        } else {
            canvas.drawBitmap(
                visibleViewBitmap,
                new Rect(0, 0, anchorView.getWidth(), anchorView.getHeight()),
                new Rect(leftPositionVisibleView, topPositionVisibleView, rightPositionVisibleView, bottomPositionVisibleView),
                null);
        }

        containerView.setBackground(new BitmapDrawable(getContext().getResources(), bitmap));
    }

    private Bitmap bitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(canvas);
        return bitmap;
    }
}
