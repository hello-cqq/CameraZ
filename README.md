# CameraZ
# 介绍
CamearZ是自主开发且开源的一个Android相机预览SDK，目前已推广应用在公司内部的一些模块，目的是希望通过最简单的接口帮助开发者完成Android相机的功能调用，不用费心考虑camera和surface的生命周期。支持以下功能：

1. 快捷预览：只用几行代码就可以实现相机预览，且同时支持camera1和camera2接口，实测全屏下预览效果在OPPO Find N及HUAWEI P10上优于HUAWEI的[统一扫码服务](https://developer.huawei.com/consumer/cn/doc/development/HMSCore-Guides/service-introduction-0000001050041994)；
2. 快捷扫码：对[zxing](https://github.com/zxing/zxing)库做了简单修改，配合cameraz可快速实现一个扫码功能；
3. 变形预览：支持矩形、圆形等不同形状预览，且支持预览窗口拖拽和双指缩放变焦；
4. 前后摄预览：支持同时打开前后主摄像头进行双窗口预览；
5. 后置多摄预览：在手机厂商HAL支持camera2的前提下，可打开后置多颗摄像头实现预览拍照。

更多详细介绍：[【语雀】【CameraZ】个人开源的安卓相机预览SDK](https://www.yuque.com/ahahahq/tech/ukfmcwph91qh9dml?singleDoc)
<a name="Xanid"></a>
# 架构
![相机架构 (4).png](https://cdn.nlark.com/yuque/0/2021/png/333743/1632122403271-340f876f-36f4-4112-a793-661b386f86f5.png#averageHue=%23554747&clientId=u616b7b08-d89a-4&from=ui&id=Zx7Iw&name=%E7%9B%B8%E6%9C%BA%E6%9E%B6%E6%9E%84%20%284%29.png&originHeight=950&originWidth=1648&originalType=binary&ratio=1&rotation=0&showTitle=false&size=52104&status=done&style=none&taskId=uc5bfe1b4-0c7c-47eb-8811-535726d3318&title=)<br />![相机架构 (3).png](https://cdn.nlark.com/yuque/0/2021/png/333743/1632121970669-9a3a46af-28dd-4a9e-b3ae-843d3ef9e08d.png#averageHue=%23ababab&clientId=u616b7b08-d89a-4&from=ui&id=C79ne&name=%E7%9B%B8%E6%9C%BA%E6%9E%B6%E6%9E%84%20%283%29.png&originHeight=760&originWidth=1707&originalType=binary&ratio=1&rotation=0&showTitle=false&size=90493&status=done&style=none&taskId=uea9c97f6-f7c8-4d4d-9afd-538eef9ea8c&title=)

1. APP层提供少量且简单的入口：打开相机，设置参数，预览/拍照/录制，暂停，关闭等；
2. Manager负责生成（Producer）、监听（Listener）和管理Client；
3. Client是给业务端使用的相机客户端，业务方不仅可以使用自己定义的预览surface，还可以一键使用CameraZ已经封装好的预览实现，真正做到零代码接入；
4. Client为内部实现的预览提供了自定义的装饰器或者叫滤镜，通过proxy1或者proxy2（分别对应camera1和camera2的代理实现），调用原生API。
<a name="rPLoN"></a>
# 接入
<a name="OxF51"></a>
## 引入依赖
```groovy
//需要在根build.gradle引入仓库：mavenCentral
repositories {
    mavenCentral()
}
...
//引入预览库
implementation "io.github.hello-cqq:cameraz:1.0.0"
//引入条码库
implementation "io.github.hello-cqq:barcode:1.0.0"
```
<a name="NIQIG"></a>
## 扫码使用举例
```kotlin
private lateinit var cameraZ: CameraZ
private lateinit var decoder: Decoder
private var backCameraId = "0"

private val previewCallback: PreviewCallback = object : PreviewCallback {
    override fun onPreviewFrame(data: ByteArray?, size: Size, format: Int) {
        decodeCode(data, size, format)
    }
}

override fun onCreate() {
    super.onCreate()
    //获取相机管理实例
    cameraZ = CameraZ.getInstance(this.applicationContext)
    decoder = Decoder()
    //获取后置主摄id，通常为0
    backCameraId = CameraConfigUtil.getBackCameraList()[0].toString()
}

override fun onResume() {
    super.onResume()
    //使用camera1接口启动相机，建议在onResume执行，因为通常我们还需要申请相机等权限
    cameraZ.open(
        backCameraId,
        object : CameraStateCallback {
            override fun onOpened(client: CameraClient) {
                Log.d(TAG, "onOpened")
                cameraView = findViewById(R.id.camera_view)
                //可选，绑定相机生命周期到Activity
                client.bind(this@SuperScanActivity)
                //打开预览
                client.startPreview(cameraView!!, TAG, previewCallback)
            }
        },
        1
    )
}

//解码
private fun decodeCode(data: ByteArray?, size: Size, format: Int) {
    val results = data?.let {
        if (format == ImageFormat.NV21) {
            decoder.decode(it, format, size, null, null)
        } else {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            decoder.decode(bitmap)
        }
    }
}

//拍照
private fun clickShotBtn() {
    cameraClient?.let {
        it.takePicture(TAG, pictureCallback)
    }
}

//关闭
override fun onDestroy() {
    super.onDestroy()
    //关闭相机
    cameraZ.closeAll()
    //注意，仅在彻底不使用时再释放(线程)
    cameraZ.release()
}
```
<a name="p4Qd7"></a>
# 预览
<a name="i0Tt1"></a>
## 常见的预览问题
<a name="CasHE"></a>
### 1. 预览拉伸？
相机分辨率与Surface比例不一致导致
<a name="oxNFu"></a>
### 2. 预览卡顿？
相机分辨率太大或UI线程操作相机
<a name="x2LrS"></a>
## CameraZ 预览算法

1. 任意期望的预览分辨率为![](https://cdn.nlark.com/yuque/__latex/7f1122aba66682a54d4eed3934855162.svg#card=math&code=b%28%7Bx_%7Bb%7D%7D%20%2A%20%7By_%7Bb%7D%7D%29&id=iJtYx)，相机所支持的分辨率数组为![](https://cdn.nlark.com/yuque/__latex/a6e60769526e4938ce4ebf07c6cd432a.svg#card=math&code=%5Cleft%20%5C%7Ba_%7Bn%7D%5Cright%20%5C%7D&id=KjjvM);
2. 将![](https://cdn.nlark.com/yuque/__latex/a6e60769526e4938ce4ebf07c6cd432a.svg#card=math&code=%5Cleft%20%5C%7Ba_%7Bn%7D%5Cright%20%5C%7D&id=pwTTR)按分辨率尺寸![](https://cdn.nlark.com/yuque/__latex/43520ceb0eed0e8efc157edad713a27a.svg#card=math&code=%5Csqrt%7Bx_%7Ba_%7Bi%7D%7D%5E2%2By_%7Ba_%7Bi%7D%7D%5E2%7D%20&id=UxsW2)从大到小排序，得到![](https://cdn.nlark.com/yuque/__latex/21e64e07a963a05db5abf38f6a0aa214.svg#card=math&code=%5Cleft%20%5C%7Bs_%7Bn%7D%5Cright%20%5C%7D&id=PdJpv)；
3. 遍历![](https://cdn.nlark.com/yuque/__latex/21e64e07a963a05db5abf38f6a0aa214.svg#card=math&code=%5Cleft%20%5C%7Bs_%7Bn%7D%5Cright%20%5C%7D&id=HpItq),并取出所有同时满足以下两个条件的项：
   1. ![](https://cdn.nlark.com/yuque/__latex/afa4f98a7dfe0e80f8f942651c767a34.svg#card=math&code=%7Bx_%7Bb%7D%7D%20%2A%201.5%3E%3D%20%7Bx_%7Bs_%7Bi%7D%7D%7D%20%3E%3D%20%7Bx_%7Bb%7D%7D&id=FOAd4)
   2. ![](https://cdn.nlark.com/yuque/__latex/46469a2c722ff63a65cabb4c3c1dc81b.svg#card=math&code=%7By_%7Bb%7D%7D%20%2A%201.5%3E%3D%20%7By_%7Bs_%7Bi%7D%7D%7D%20%3E%3D%20%7By_%7Bb%7D%7D&id=bLz4x)

得到新的相机支持的分辨率数组![](https://cdn.nlark.com/yuque/__latex/784878ef950e802b09a59596259a6d44.svg#card=math&code=%5Cleft%20%5C%7Bp_%7Bm%7D%5Cright%20%5C%7D&id=UgFPC)；

4. 将![](https://cdn.nlark.com/yuque/__latex/784878ef950e802b09a59596259a6d44.svg#card=math&code=%5Cleft%20%5C%7Bp_%7Bm%7D%5Cright%20%5C%7D&id=MkCGS)按相机分辨率比例与期望分辨率比例插值![](https://cdn.nlark.com/yuque/__latex/f931f73a1b515e827d1c4b13d3092473.svg#card=math&code=%5Cleft%20%7C%5Cfrac%7By_%7Bp_%7Bi%7D%7D%7D%7Bx_%7Bp_%7Bi%7D%7D%7D-%5Cfrac%7By_%7Bb%7D%7D%7Bx_%7Bb%7D%7D%5Cright%20%7C&id=a00B9)从小到大排序，得到![](https://cdn.nlark.com/yuque/__latex/55397875ea18deeb30be3d2c67d8a438.svg#card=math&code=%5Cleft%20%5C%7Bq_%7Bm%7D%5Cright%20%5C%7D&id=JVOQN)；
5. 取![](https://cdn.nlark.com/yuque/__latex/55397875ea18deeb30be3d2c67d8a438.svg#card=math&code=%5Cleft%20%5C%7Bq_%7Bm%7D%5Cright%20%5C%7D&id=weklt)的第一项，得到最佳的相机预览分辨率，即![](https://cdn.nlark.com/yuque/__latex/6cd7b925e4e4d083cae161e8ec51a494.svg#card=math&code=%7Bq_%7B0%7D%7D&id=KrXNN)，如下图形表示：

![CameraS预览算法.png](https://cdn.nlark.com/yuque/0/2022/png/333743/1655190620477-b9e358c8-5d44-4e08-a129-f154e36d7017.png#averageHue=%230c0904&clientId=ub261cca8-7d77-4&from=paste&height=1417&id=u5c784768&name=CameraS%E9%A2%84%E8%A7%88%E7%AE%97%E6%B3%95.png&originHeight=1417&originWidth=1608&originalType=binary&ratio=1&rotation=0&showTitle=false&size=156418&status=done&style=none&taskId=u79099af8-ea2a-44cd-b396-c2c32880c9a&title=&width=1608)

6. 通常![](https://cdn.nlark.com/yuque/__latex/7f1122aba66682a54d4eed3934855162.svg#card=math&code=b%28%7Bx_%7Bb%7D%7D%20%2A%20%7By_%7Bb%7D%7D%29&id=uWKbS)的值与屏幕大小一致，需要将**b**按照![](https://cdn.nlark.com/yuque/__latex/6cd7b925e4e4d083cae161e8ec51a494.svg#card=math&code=%7Bq_%7B0%7D%7D&id=UYp7h)的比例进行向上调整surface的宽高，最终预览正常。
<a name="gr2OH"></a>
## 方案对比
这里用华为开发者平台的开放能力——[统一扫码服务](https://developer.huawei.com/consumer/cn/doc/development/HMSCore-Guides/service-introduction-0000001050041994)做对比，从官网下载的demo，设备使用了OPPO FindN和HUAWEI P10。在折叠屏Find N和华为p10机器上，CameraZ的预览是正常的，而HUAWEI的demo都存在不同程度的拉伸问题，这是因为我们快捷扫码自定义了一套可以适配所有预览窗口大小的预览算法。<br />下图左边是CameraZ，右边是HUAWEI统一扫码服务。<br />![image.png](https://cdn.nlark.com/yuque/0/2023/png/333743/1676807593325-09c5e87c-04ee-4196-8b7d-0c24ce593113.png?x-oss-process=image%2Fresize%2Cw_937%2Climit_0)
