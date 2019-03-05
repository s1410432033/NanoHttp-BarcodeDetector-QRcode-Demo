# NanoHttp-BarcodeDetector-QRcode-Demo
手機內建立一個Server，創建一支API後將掃描QRCode完結果傳至API Response。


## NanoHttp 匯入 Android
1.下載nanoHttp jar檔 載點：https://github.com/NanoHttpd/nanohttpd/releases 

2.載入android studio jar 示範:http://rx1226.pixnet.net/blog/post/288329377-%5Bandroid%5D-2-16-%E5%8A%A0%E5%85%A5jar---android-studio

3.gralde加入    

```Android
implementation files('libs/nanohttpd-2.2.0.jar')
```

## NanoHttp創立Server

  首先，先創立一個Server！
  
  在return newFixedLengthResponse裡就是放要回送的Response。
```android
public class MyServer extends NanoHTTPD {
    public MyServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        if (NanoHTTPD.Method.GET.equals(method)){
            return newFixedLengthResponse("");
        }
        return super.serve(session);
    }
}
```

  再來，創立一個Service用來啟動Server，這樣不會造成到畫面上的Delay。
  
  別忘了在Manifest加入Service申請！
  ```android
  public class ServerService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyServer myServer = new MyServer(8080);
        try {
            myServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
  ```
  
  再來只要在MainActivity當中startService就可以架起來了！
  
  # 掃描QRCode

  利用barcodeDetector搭配cameraSource可以做出在一個畫面上開啟相機並讓相機有掃描QRCode功能。
  
  ## 設置
  1.在gradle加入
  ```android
  compile 'com.google.android.gms:play-services-vision:15.0.1'
  ```
  ## Layout畫面
  畫面上設置一個SurfaceView跟一個TextView放掃描結果。
  ```android
  <?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.example.huanghuai.qrcode.MainActivity">

    <SurfaceView
        android:layout_width="match_parent"
        android:layout_height="400dp"
        android:id="@+id/qrCodeFind"/>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:textSize="30sp"
        android:text="Please scan"
        android:gravity="center"
        android:id="@+id/qrCodeText"/>
</RelativeLayout>
  ```
  ## 設置BarcodeDetector
  ```android
  barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
  ```
  ## 設置CameraSource
  在setRequestedPreviewSize屬性中是設置FPS setAutoFocusEnabled是設置是否要有焦點。
  
  ```android
  cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();
  ```
  
  之後在Surface中啟動以設置好的CameraSource就有了一個可以啟用鏡頭的View了！
  
  ## 拿到掃描結果

    啟用setProcessor裡的receiveDetections可以拿到detections.getDetectedItems()，這個陣列裡是放他一次掃描的結果，
    如果一次掃了多個QRcode，它的都會放到這個陣列中，另外因為掃描頻率很快，所以通常掃到了一筆實際上已經回傳了很幾筆一樣的了，
    所以寫了一個addInList方法取出跟上次不同結果的值。

```android
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCode = detections.getDetectedItems();

                if(qrCode.size()!=0){
                    qrCodeText.post(new Runnable() {
                        @Override
                        public void run() {
                            qrCodeText.setText(qrCode.valueAt(0).displayValue);
                            addInList(qrCode.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });
        
        private void addInList(String scannResult){
        if (scannList.isEmpty()){
            scannList.add(scannResult);
        }else{
            String a = scannList.get(scannList.size()-1);
            if (!a.equals(scannResult)) {
                scannList.add(scannResult);
            }
        }
            for(String s :scannList){
                Log.e("s", "onCreate: "+s);
            }
        Log.e("size", "addInList: "+scannList.size() );

    }
  ```
  
  別忘了在Destory中stopService！
  
  最後再把掃描的結果scannList放在上述Server裡的return newFixedLengthResponse("")裡就完成了。
