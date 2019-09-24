package com.example.pdfviewpractice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnDrawListener;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

import java.io.File;
import java.io.IOException;

import cn.com.cybertech.base.BaseActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import wpa.sdly.ycjw.http.R;
public class DetailsReportingActivity extends AppCompatActivity {
    private PDFView pdfView;
    private String pdfUrl;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
     private String cacheUrl = "";//应用缓存路径
    private String pdfName = "error";//文件名称   默认一个错误名称
    private boolean mLoadComplete = false;//加载完成

    private Handler handler = null;
    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            checkPerMission();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_reporting);
        pdfView = findViewById(R.id.pdfView);
        if (getIntent().getStringExtra("pdfUrl") != null) {
            pdfUrl = getIntent().getStringExtra("pdfUrl");
        }
        checkPerMission();

     }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbar_menu = menu;
        return true;
    }


    /**
     * 检查读写权限
     */
    private void checkPerMission() {
        int permission = ActivityCompat.checkSelfPermission(DetailsReportingActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {//无权限  申请
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(DetailsReportingActivity.this, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
            if (handler == null) {
                handler = new Handler();
            }
            handler.postDelayed(checkRunnable, 1000);//隔一秒再检查
        } else {
            DownloadPdf();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {//删除文件  避免引起缓存过大
            File dest = new File(cacheUrl, pdfName);
            if (dest.exists()) {
                dest.delete();
            }
        } catch (Exception e) {

        }
    }


    /**
     * 开始下载PDF
     */
    private void DownloadPdf() {
        cacheUrl = getCacheDir().getAbsolutePath();//应用缓存路径
        pdfName = pdfUrl.substring(pdfUrl.lastIndexOf("/") + 1);//文件名称
        final File dest = new File(cacheUrl, pdfName);
        if (dest.exists()) {
            SeePdf(dest);
        } else {
            Request request = new Request.Builder().url(pdfUrl).build();
            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // 下载失败
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Sink sink = null;
                    BufferedSink bufferedSink = null;
                    try {
                        if (!dest.exists()) {
                            boolean newFile = dest.createNewFile();
                        }
                        sink = Okio.sink(dest);
                        bufferedSink = Okio.buffer(sink);
                        bufferedSink.writeAll(response.body().source());
                        bufferedSink.close();
                        if (handler == null) {
                            handler = new Handler(Looper.getMainLooper());
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                SeePdf(dest);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (bufferedSink != null) {
                            bufferedSink.close();
                        }

                    }
                }
            });
        }
    }

    /**
     * 查看PDF
     */
    private void SeePdf(File dest) {
        try {
            pdfView.setVisibility(View.VISIBLE);
            pdfView.fromFile(dest)
                    .defaultPage(1)  //设置默认显示第1页
                    .onPageChange(new OnPageChangeListener() {
                        @Override
                        public void onPageChanged(int page, int pageCount) {

                        }
                    }) //设置翻页监听
                    .onDraw(new OnDrawListener() {
                        @Override
                        public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                            refreshPageView();
                        }
                    })
                    .onLoad(new OnLoadCompleteListener() {
                        @Override
                        public void loadComplete(int nbPages) {
                            mLoadComplete = true;
                        }
                    })
                    .showMinimap(false) //pdf放大的时候，是否在屏幕的右上角生成小地图
                    .swipeVertical(true) //pdf文档翻页是否是垂直翻页，默认是左右滑动翻页
                    .enableSwipe(true) //是否允许翻页，默认是允许翻页
                    .load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏页数
     */
    Runnable hidePage = new Runnable() {
        @Override
        public void run() {

        }
    };

    private void refreshPageView() {

        if (handler == null) {
            handler = new Handler();
        }
        handler.removeCallbacks(hidePage);
        handler.postDelayed(hidePage, 3000);
    }
}



