算法来自鲁班，去除了rx依赖，添加了批量压缩，自定义输出目录。
请尽量不要在主线程使用！

使用方式，拷贝App文件夹下的MahoLuban类

MahoLuban.
get(this).
setCacheName("maho").
load(fileList).
setOnMahoCompressLister(new MahoLuban.OnMahoCompressListener(){...}).
start();

多谢大大指正，感激不尽。
