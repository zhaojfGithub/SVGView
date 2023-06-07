package com.zjf.svgview;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import androidx.annotation.ColorInt;

/**
 * @author zjf
 * @date 2023/5/29
 */
public class PathBean {
    private Integer id;
    private String tag;

    private Path path;
    private RectF rectF;
    private Region region;
    @ColorInt
    private Integer color;

    private Boolean isSelect;

    public PathBean(Integer id, String tag, Path path, @ColorInt Integer color, Boolean isSelect) {
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        Region region = new Region();
        Rect rect = new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
        region.setPath(path, new Region(rect));

        this.id = id;
        this.tag = tag;
        this.path = path;
        this.rectF = rectF;
        this.region = region;
        this.color = color;
        this.isSelect = isSelect;
    }

    @Override
    public String toString() {
        return "PathBean{" +
                "id=" + id +
                ", tag='" + tag + '\'' +
                ", path=" + path +
                ", rectF=" + rectF +
                ", region=" + region +
                ", color=" + color +
                ", isSelect=" + isSelect +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Boolean getSelect() {
        return isSelect;
    }

    public void setSelect(Boolean select) {
        isSelect = select;
    }

    public RectF getRectF() {
        return rectF;
    }

    public void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(@ColorInt Integer color) {
        this.color = color;
    }
}
