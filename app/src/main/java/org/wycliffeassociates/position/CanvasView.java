package org.wycliffeassociates.position;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class CanvasView extends View {

    public interface DrawListener {
        void onDraw(int x, int y);
        void onDrawFinished(int x, int y);
    }

    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    Context context;
    private Paint mPaint;
    private float mX, mY;
    private int dirX = 0, dirY = 0;
    private static final float TOLERANCE = 5;

    private DrawListener callback;

    public boolean isBlocked = false;

    HashMap<Integer, Character> characters = new HashMap<>();

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        this.callback = null;

        // we set a new Path
        mPath = new Path();

        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4f);
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(isBlocked) return;

        // draw the mPath with the mPaint on the canvas when onDraw
        //canvas.drawPath(mPath, mPaint);

        for(Map.Entry<Integer, Character> entry : characters.entrySet()) {
            Integer id = entry.getKey();
            Character character = entry.getValue();

            Paint paint = new Paint();

            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(false);

            paint.setColor(Color.BLACK);
            canvas.drawRect(
                    (character.x)*2 - 10,
                    (character.y)*2 - 10,
                    (character.x + 60)*2 + 10,
                    (character.y + 60)*2 + 10,
                    paint);

            paint.setColor(character.color);
            canvas.drawRect(
                    (character.x)*2,
                    (character.y)*2,
                    (character.x + 60)*2,
                    (character.y + 60)*2,
                    paint);

            paint.setColor(Color.BLACK);
            paint.setTextSize(20);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(character.name, (character.x)*2 + 10, (character.y)*2 + 60, paint);
        }
    }

    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
        mX = x;
        mY = y;
        dirX = 0;
        dirY = 0;
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            if(x > mX) dirX = 1;
            if(x < mX) dirX = -1;

            if(y > mY) dirY = 1;
            if(y < mY) dirY = -1;

            if(callback != null) {
                callback.onDraw(dirX, dirY);
            }
        }
    }

    public void clearCanvas() {
        mPath.reset();
        invalidate();
    }

    public void setCharacters(HashMap<Integer, Character> characters) {
        this.characters = characters;
        isBlocked = false;
    }

    public void setCallback(DrawListener callback) {
        this.callback = callback;
    }

    // when ACTION_UP stop touch
    private void upTouch() {
        if(callback != null) {
            callback.onDrawFinished(dirX, dirY);
        }
    }

    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                break;
        }
        return true;
    }
}
