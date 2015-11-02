# SlideBack

使用Kotlin实现的一个简单的拖拽关闭Activity的库，实现原理主要参考ikew0ng巨巨的[SwipBackLayout](https://github.com/ikew0ng/SwipeBackLayout)。

# Usage

1.在你想要具备滑动删除功能的`Activity`的`onCreate`方法中增加
```java
Slider.INSTANCE$.attachToScreen(this, edge, SlideShadow.FULL);
```

2.同时你的Activity对应的`Theme`需要具有下面的属性
```
<style name="AppTranslucentTheme" parent="Theme.AppCompat.Light.DarkActionBar">
    <item name="android:windowIsTranslucent">true</item>
    <item name="android:windowBackground">@android:color/transparent</item>
</style>
```

# Members

[Kesco](https://github.com/kesco)
[Troy](https://github.com/troytang)