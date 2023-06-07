package com.zjf.svgview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.core.graphics.PathParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author zjf
 * @date 2023/5/29
 */
public class SVGHelpImpl implements SVGHelpInterface {

    @Override
    public List<PathBean> deCodeSVG(Context context, Integer SVGId, File file, @ColorInt Integer color) throws IOException {
        InputStream inputStream = null;
        List<PathBean> pathList = new ArrayList<>();
        try {
            if (SVGId != null) {
                inputStream = context.getResources().openRawResource(SVGId);
            } else if (file != null && file.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    inputStream = Files.newInputStream(file.toPath());
                }else {
                    inputStream = new FileInputStream(file);
                }
            } else {
                return null;
            }
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document parse = documentBuilder.parse(inputStream);
            NodeList paths = parse.getDocumentElement().getElementsByTagName("path");
            for (int i = 0; i < paths.getLength(); i++) {
                Element item = (Element) paths.item(i);
                if (item == null) {
                    continue;
                }
                String pathStr = item.getAttribute("android:pathData");
                String pathTag = item.getAttribute("android:tag");
                Path pathData = PathParser.createPathFromPathData(pathStr);
                PathBean pathBean = getPathBean(i, pathTag, pathData, color);
                pathList.add(pathBean);
            }
            return pathList;
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            assert inputStream != null;
            inputStream.close();
        }
        return null;
    }

    @Override
    public RectF getSVGRecF(List<PathBean> list) {
        if (list == null){
             return null;
        }
        float left = -1F, top = -1F, right = -1F, bottom = -1F;
        for (int i = 0; i < list.size(); i++) {
            RectF bounds = new RectF();
            list.get(i).getPath().computeBounds(bounds, true);
            left = left == -1 ? bounds.left : Math.min(left, bounds.left);
            top = top == -1 ? bounds.top : Math.min(top, bounds.top);
            right = right == -1 ? bounds.right : Math.max(right, bounds.right);
            bottom = bottom == -1 ? bounds.bottom : Math.max(bottom, bounds.bottom);
        }
        return new RectF(left, top, right, bottom);
    }

    @Override
    public PathBean getPathBean(Integer id, String tag, Path path, Integer color) {
        return new PathBean(id, tag, path, color, false);
    }

    @Override
    public void onDraw(PathBean pathBean, Canvas canvas, Paint paint, Integer LineColor) {
        paint.setColor(pathBean.getColor());
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(pathBean.getPath(), paint);
        //交割线
        paint.setStrokeWidth(1);
        paint.setColor(LineColor);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(pathBean.getPath(), paint);
    }

    @Override
    public Boolean isClick(PathBean pathBean, Float x, Float y) {
        return pathBean.getRegion().contains(x.intValue(), y.intValue());
    }
}
