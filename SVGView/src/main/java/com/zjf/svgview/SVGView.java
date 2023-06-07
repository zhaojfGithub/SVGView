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
import android.util.Log;
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

    /**
     * 默认阈值，一般不做修改，太少了会不好控制滑动和缩放
     */
    private static final int MAX_MOVE_NUMBER = 5;

    /**
     * 默认移动速度
     */
    private static final float MOVE_INIT_SPEED = 2;

    /**
     * 默认缩放速度
     */
    private static final float ZOOM_INIT_SPEED = 3;

    private OnSVGClickListener onClickListener;

    private SVGHelpInterface svgHelp;

    private Integer svgId;

    private File file;

    /**
     * 整体背景
     */
    private int svgBackground;

    /**
     * 分割线颜色
     */
    private int lineColor;

    private List<PathBean> PathList;

    /**
     * 当前缩放倍数
     */
    private Float scale;

    /**
     * 图像的默认大小
     */
    private RectF originRecF;

    private Paint paint;

    /**
     * 处理区域点击
     */
    private GestureDetector gestureClick;

    /**
     * 处理手势移动
     */
    private GestureDetector gestureMove;

    /**
     * 处理手势缩放
     */
    private ScaleGestureDetector gestureZoom;

    /**
     * 区分缩放与滑动的阈值
     */
    private int moveNumber = 0;

    /**
     * 当前位移的X轴距离
     */
    private Float moveX = 0F;

    /**
     * 当前位移的Y轴距离
     */
    private Float moveY = 0F;

    /**
     * 之前滑动的X轴距离(因为可能经历了多次滑动，所以需要记录一下)
     */
    private Float lastMoveX = 0F;

    /**
     * 之前滑动的Y轴距离
     */
    private Float lastMoveY = 0F;

    /**
     * 是否可以触发滑动
     */
    private boolean isMove;
    /**
     * 移动的速度
     */
    private float moveSpeed;

    /**
     * 是否可以触发缩放
     */
    private boolean isZoom;

    /**
     * 缩放的速度
     */
    private float zoomSpeed;

    /**
     * 主区域的颜色
     */
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
                    float x = (e.getX() - (getWidth() - originRecF.width() * scale) / 2 - lastMoveX) / scale;
                    float y = (e.getY() - (getHeight() - originRecF.height() * scale) / 2 - lastMoveY) / scale;
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
                float scaleGap = (detector.getScaleFactor() - lastScale) / zoomSpeed;
                //因为如果scale为负数，会造成图像倒转，到0会看不到，故设置为0.1
                if (scale + scaleGap > 0.1) {
                    scale += scaleGap;
                }
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
        canvas.translate(lastMoveX, lastMoveY);
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
            if (moveNumber > MAX_MOVE_NUMBER) {
                //只有经历了滑动才记录
                lastMoveX += moveX;
                lastMoveY += moveY;
                moveX = 0F;
                moveY = 0F;
            }
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
