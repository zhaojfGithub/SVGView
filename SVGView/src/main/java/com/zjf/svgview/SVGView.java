package com.zjf.svgview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zjf
 * @date 2023/5/29
 */
public class SVGView extends View {

    private static final int MAX_MOVE_NUMBER = 5;

    private static final float MOVE_INIT_SPEED = 2;

    private static final float ZOOM_INIT_SPEED = 3;

    private OnSVGClickListener onClickListener;

    private SVGHelpInterface svgHelp;

    private Integer svgId;

    private File file;

    private int svgBackground;

    private int lineColor;

    private List<PathBean> PathList;

    private Float scale;

    private RectF originRecF;

    private Paint paint;

    private GestureDetector gestureClick;

    private GestureDetector gestureMove;

    private ScaleGestureDetector gestureZoom;

    private int moveNumber = 0;

    private Float moveX = 0F;

    private Float moveY = 0F;

    private boolean isMove;
    private float moveSpeed;

    private boolean isZoom;
    private float zoomSpeed;

    @ColorInt
    private Integer color;


    public SVGView(Context context) {
        this(context, null);
    }

    public SVGView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SVGView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.SVGView);
            svgId = typedArray.getResourceId(R.styleable.SVGView_SVGId, -1);
            scale = typedArray.getFloat(R.styleable.SVGView_SVGScale, 1);
            svgBackground = typedArray.getColor(R.styleable.SVGView_SVGBackground, Color.WHITE);
            color = typedArray.getColor(R.styleable.SVGView_SVGColor, Color.BLACK);
            lineColor = typedArray.getColor(R.styleable.SVGView_SVGLineColor, Color.BLACK);
            isMove = typedArray.getBoolean(R.styleable.SVGView_SVGIsMove, false);
            isZoom = typedArray.getBoolean(R.styleable.SVGView_SVGIsZoom, false);
            moveSpeed = typedArray.getFloat(R.styleable.SVGView_SVGMoveSpeed, MOVE_INIT_SPEED);
            zoomSpeed = typedArray.getFloat(R.styleable.SVGView_SVGZoomSpeed, ZOOM_INIT_SPEED);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert typedArray != null;
            typedArray.recycle();
        }
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (originRecF == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getSize(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth = (int) originRecF.width() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = (int) originRecF.height() + getPaddingTop() + getPaddingBottom();
        int measuredWidth;
        int measuredHeight = desiredHeight;
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                measuredWidth = widthSize;
                break;
            case MeasureSpec.AT_MOST:
                measuredWidth = Math.min(desiredWidth, widthSize);
                break;
            default:
                measuredWidth = desiredWidth;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            measuredHeight = heightSize;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        PathList = new ArrayList<>();
        gestureClick = new GestureDetector(getContext(), getOnClickGesture());
        gestureMove = new GestureDetector(getContext(), getOnMoveGesture());
        gestureZoom = new ScaleGestureDetector(getContext(), getOnZoomGesture());
        if (svgHelp == null) {
            svgHelp = new SVGHelpImpl();
        }
        if (svgId == null || svgId == -1) {
            return;
        }
        deCodeSVG();
    }

    private void deCodeSVG() {
        new Thread(() -> {
            try {
                List<PathBean> list = svgHelp.deCodeSVG(getContext(), svgId, file, color);
                PathList.clear();
                PathList.addAll(list);
                originRecF = svgHelp.getSVGRecF(PathList);
                post(this::requestLayout);
                post(this::invalidate);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private GestureDetector.OnGestureListener getOnClickGesture() {
        return new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(@NonNull MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent e) {
                boolean result = false;
                for (PathBean pathBean : PathList) {
                    float x = (e.getX() - (getWidth() - originRecF.width() * scale) / 2 - moveX) / scale;
                    float y = (e.getY() - (getHeight() - originRecF.height() * scale) / 2 - moveY) / scale;
                    if (svgHelp.isClick(pathBean, x, y)) {
                        pathBean.setSelect(!pathBean.getSelect());
                        if (onClickListener != null) {
                            onClickListener.onClick(pathBean);
                        }
                        result = true;
                        invalidate();
                    }
                }
                return result;
            }

            @Override
            public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {

            }

            @Override
            public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        };
    }

    private GestureDetector.SimpleOnGestureListener getOnMoveGesture() {
        return new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                if (!isMove) {
                    return false;
                }
                if (moveNumber <= MAX_MOVE_NUMBER) {
                    moveNumber++;
                    return true;
                }
                float deltaX = (e2.getX() - e1.getX()) / moveSpeed;
                float deltaY = (e2.getY() - e1.getY()) / moveSpeed;
                moveX = deltaX;
                moveY = deltaY;
                invalidate();
                return true;
            }
        };
    }

    private ScaleGestureDetector.SimpleOnScaleGestureListener getOnZoomGesture() {
        return new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            Float lastScale;

            @Override
            public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
                lastScale = 1F;
                return super.onScaleBegin(detector);
            }

            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                if (!isZoom) {
                    return false;
                }
                scale += (detector.getScaleFactor() - lastScale) / zoomSpeed;
                invalidate();
                return true;
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
                super.onScaleEnd(detector);
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (PathList.isEmpty() || canvas == null || originRecF == null) {
            return;
        }
        canvas.save();
        canvas.drawColor(svgBackground);
        //默认先移动到中间
        canvas.translate((getWidth() - originRecF.width() * scale) / 2, (getHeight() - originRecF.height() * scale) / 2);
        //处理手势位移
        canvas.translate(moveX, moveY);
        canvas.scale(scale, scale);
        for (int i = 0; i < PathList.size(); i++) {
            svgHelp.onDraw(PathList.get(i), canvas, paint, lineColor);
        }
        canvas.restore();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureClick.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        if (pointerCount == 1) {
            gestureMove.onTouchEvent(event);
        } else {
            gestureZoom.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            moveNumber = 0;
        }
        return true;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        requestLayout();
    }

    public void setSVGId(@RawRes int svgId) {
        this.file = null;
        this.svgId = svgId;
        deCodeSVG();
    }

    public void setSVGFile(File file) {
        if (!file.exists()) {
            return;
        }
        this.svgId = null;
        this.file = file;
        deCodeSVG();
    }

    public boolean isMove() {
        return isMove;
    }

    public void setMove(boolean move) {
        isMove = move;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public boolean isZoom() {
        return isZoom;
    }

    public void setZoom(boolean zoom) {
        isZoom = zoom;
    }

    public float getZoomSpeed() {
        return zoomSpeed;
    }

    public void setZoomSpeed(float zoomSpeed) {
        this.zoomSpeed = zoomSpeed;
    }

    public Integer getSVGId() {
        return svgId;
    }

    public Float getSVGScale() {
        return scale;
    }

    /**
     * 设置SVG提取辅助类
     *
     * @param svgHelp SVG提取
     */
    public void setSVGHelp(SVGHelpInterface svgHelp) {
        this.svgHelp = svgHelp;
    }

    /**
     * 设置点击事件
     *
     * @param onClickListener 点击回调
     */
    public void setOnClickListener(OnSVGClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnSVGClickListener {
        void onClick(PathBean pathBean);
    }

}
