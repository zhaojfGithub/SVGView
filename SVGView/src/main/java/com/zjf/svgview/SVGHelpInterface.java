package com.zjf.svgview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author zjf
 * @date 2023/5/29
 */
public interface SVGHelpInterface {
    List<PathBean> deCodeSVG(Context context, Integer SVGId, File file, @ColorInt Integer color) throws IOException;

    RectF getSVGRecF(List<PathBean> list);

    PathBean getPathBean(Integer id, String tag, Path path, Integer color);

    void onDraw(PathBean pathBean, Canvas canvas, Paint paint, @ColorInt Integer LineColor);

    Boolean isClick(PathBean pathBean, Float x, Float y);
}
