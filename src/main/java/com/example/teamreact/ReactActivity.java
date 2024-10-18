package com.example.teamreact;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReactActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_react);
        webView = findViewById(R.id.webView);
        // native ui 의 동작 정의하기
        ImageButton homeBtn = findViewById(R.id.btn_home);
        homeBtn.setOnClickListener(view -> {
            finish(); // 액티비티 종료!
        });

        // native ui 의 동작 정의하기
        ImageButton reload = findViewById(R.id.btn_reload);
        reload.setOnClickListener(view -> {
            // 현재 페이지 새로고침
            webView.reload();
        });

        // native ui 의 동작 정의하기
        ImageButton upscroll = findViewById(R.id.btn_upscroll);
        upscroll.setOnClickListener(view -> {
            // 맨 위로 스크롤
            webView.scrollTo(0, 0);
        });


        //WebView 의 참조값 얻어오기
        WebView webView=findViewById(R.id.webView);
        //WebView 설정 객체 얻어오기
        WebSettings ws=webView.getSettings();
        ws.setJavaScriptEnabled(true); // javascript 해석 가능하도록
        ws.setDomStorageEnabled(true); // localStorage, sessionStorage 등을 사용가능 하도록

        ws.setUseWideViewPort(true); // viewport 사용
        ws.setLoadWithOverviewMode(true); // 컨텐츠가 웹뷰에 맞게 조정
        ws.setBuiltInZoomControls(true); // 줌 컨트롤 사용
        ws.setDisplayZoomControls(false); // 줌 컨트롤러 숨기기

        // 외부로 리소스를 요청할 수 있도록 Mixed Content 모드 설정 (HTTPS에서 HTTP 요청 가능)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //WebView 클라이언트 객체를 생성해서 넣어주기
        webView.setWebViewClient(new WebViewClient(){
            //재정의 하고 싶은 메소드가 있으면 여기서 해준다.

        });
        //아래의 MyWebViewClient 클래스로 생성한 객체를 전달한다.
        webView.setWebChromeClient(new MyWebViewClient());
        // WebView 에 외부 페이지 로딩 시키기
        webView.loadUrl("http://192.168.0.125:3000");

    }
    public class MyWebViewClient extends WebChromeClient {


        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadFile, FileChooserParams fileChooserParams) {


            if(mFilePathCallback !=null) {
                mFilePathCallback.onReceiveValue(null);
                mFilePathCallback = null;
            }

            mFilePathCallback = uploadFile;
            Intent i =new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");

            startActivityForResult(Intent.createChooser(i, "File Chooser"), INPUT_FILE_REQUEST_CODE);

            return true;

        }


        private void imageChooser() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(ReactActivity.this.getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e(getClass().getName(), "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:"+photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType(TYPE_IMAGE);

            Intent[] intentArray;
            if(takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
        }
    }

    //변수
    private static final String TYPE_IMAGE = "image/*";
    private static final int INPUT_FILE_REQUEST_CODE = 1;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INPUT_FILE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (mFilePathCallback == null) {
                        super.onActivityResult(requestCode, resultCode, data);
                        return;
                    }
                    Uri[] results = new Uri[]{data.getData()};

                    mFilePathCallback.onReceiveValue(results);
                    mFilePathCallback = null;
                } else {
                    if (mUploadMessage == null) {
                        super.onActivityResult(requestCode, resultCode, data);
                        return;
                    }
                    Uri result = data.getData();

                    Log.d(getClass().getName(), "openFileChooser : " + result);
                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                }
            } else {
                if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
                if (mUploadMessage != null) mUploadMessage.onReceiveValue(null);
                mFilePathCallback = null;
                mUploadMessage = null;
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}