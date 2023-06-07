# SVGView

一款处理复杂不规则图形的View，支付缩放，点击，变色操作，保留扩展功能，后续有时间会根据反馈进行迭代

### 引入

建议直接下载下来当成model引入，后续上传到maven再改

### XML属性

|  Attributes   | format  | describe     |
|:-------------:|---------|--------------|
|     SVGId     | integer | 使用svg的文件id   |
|   SVGScale    | float   | 设置初始缩放倍数     |
| SVGBackground | color   | 设置控件背景       |
|   SVGColor    | color   | 设置区域默认颜色     |
| SVGLineColor  | color   | 设置分割线默认颜色    |
|   SVGIsMove   | boolean | 是否启用滑动动能     |
| SVGMoveSpeed  | float   | 设置滑动的速度，约小越快 |
|   SVGIsZoom   | boolean | 是否启用缩放功能     |
| SVGZoomSpeed  | float   | 设置缩放的速度，约小越快 |

### 简单使用

1. 给UI要一份SVG图
2. 通过Android studio 转成xml
3. 创建一个raw文件夹，如果没有就创建一个，和res同级目录
4. xml引入，当然也可以通过Java引入，其中SVGId是必须的
5. 也支持File引入，通过Java调用setSVGFile方法

```xml

<com.zjf.svgview.SVGView 
    android:id="@+id/SVGView" 
    app:SVGId="@raw/ic_front" 
    app:SVGIsMove="true" 
    app:SVGIsZoom="true"
    android:layout_width="match_parent" 
    android:layout_height="match_parent" />
```
处理点击区域
```kotlin
val svgView = findViewById<SVGView>(R.id.SVGView)
svgView.zoomSpeed = 1F
svgView.moveSpeed = 3F
svgView.setOnClickListener(SVGView.OnSVGClickListener {
    if (it.select) {
        it.color = Color.RED
    } else {
        it.color = Color.BLUE
    }
})
```

至此设置完毕了

### 点击额外设置

因为SVG转化XML是由一个一个path组成的，假如我想要知道我点击的区域是那一部分，那么就需要加一个TAG,用来标识所点击的区域，例如：

```xml

<path
    android:pathData="M127.2,331.47C126.83,324.96 126.41,318.69 126.14,312.42C125.31,293.16 124.64,273.9 123.68,254.65C123.46,250.26 122.34,245.9 121.6,241.38C137.65,240.17 152.63,234.49 166.86,225.82C167.16,228.42 167.47,230.77 167.71,233.13C168.9,244.89 169.72,256.65 169.16,268.49C168.66,278.91 166.6,289.01 163.83,299.02C161.34,307.98 158.97,316.96 156.59,325.95C156.24,327.28 156.13,328.69 155.92,330.02C146.28,324.93 136.87,325.72 127.2,331.47"
    android:strokeWidth="1" 
    android:fillColor="#DEE1E3" 
    android:fillType="evenOdd" 
    android:tag="这是一个标识" 
    android:strokeColor="#00000000" />
```

其中 android:tag="这是一个标识"是必须的,如果想使用其他标识可以重写SVGHelpImpl的deCodeSVG方法，

### 点击回调处理

回调默认返回的是PathBean，如果需要添加自定义字段，请继承PathBean，然后重写SVGHelpImpl的getPathBean方法

关于更多可以查看源码，代码是最好的老师