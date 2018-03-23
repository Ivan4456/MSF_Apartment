package kortros.mysmartflat.ru.gulliver;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import me.leolin.shortcutbadger.ShortcutBadger;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

public class Main extends Activity {

    private WebView web;
    private Context ctx;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;


    private boolean connectivity() {
        ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void ToastNotify(final String notificationMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Main.this, notificationMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private class SendLocationJavaInterface {
        @android.webkit.JavascriptInterface
        public String getLocation() {         // запись координат
            return "Coordinates";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        ctx = getApplicationContext();

        Intent intent = new Intent(this, FirebaseMessagingService.class);
        startService(intent);

        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 0,5000);

        web = (WebView) findViewById(R.id.web);
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        web.setWebViewClient(new InternalWebViewClient());


/*        web.setWebViewClient(new WebViewClient());

        web.setWebChromeClient(new WebChromeClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.indexOf("tel:") > -1) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else {
                    return true;
                }
            }
        });
*/
        web.setWebChromeClient(new WebChromeClient() { //WebChromeClient
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                ToastNotify(message);
                //return super.onJsAlert(view, url, message, result);
                return true;
            }
        });

        web.addJavascriptInterface(new SendLocationJavaInterface(), "coords");
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setGeolocationEnabled(true);
        web.getSettings().setAppCacheEnabled(true);
        web.getSettings().setDatabaseEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowUniversalAccessFromFileURLs(true);
        //web.setLayerType(View.LAYER_TYPE_SOFTWARE,null);

        String newToken = FirebaseInstanceId.getInstance().getToken();
        //sendToken(newToken);
        Log.i(TAG, "FCM Registration Token: " + newToken);
        Random r = new Random();
        int rand = r.nextInt(1000) + 1;

        web.loadUrl("https://test.mysmartflat.ru/?udid="+newToken+"&device_type=android&rand="+String.valueOf(rand)+"&appversion=1.1");

        //web.loadUrl("http://192.168.0.111:8080/map");


        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    );
                }
            }
        });


        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);
        }else {
            CookieManager.getInstance().setAcceptCookie(true);
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private class InternalWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.indexOf("tel:") > -1) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            if (url.contains(".pdf")) {

                //web.loadUrl("http://docs.google.com/gview?embedded=true&url=" + url);
                web.loadUrl("https://docs.google.com/viewer?url=" + url);



                /*
                Uri path = Uri.parse(url);
                Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                pdfIntent.setDataAndType(path, "application/pdf");
                pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                try
                {
                    startActivity(pdfIntent);
                }
                catch(ActivityNotFoundException e)
                {
                    Toast.makeText(Main.this, "PDF файл не найден", Toast.LENGTH_SHORT).show();
                }
                catch(Exception otherException)
                {
                    Toast.makeText(Main.this, "Ошибка", Toast.LENGTH_SHORT).show();
                }
                */

                return true;
            }
            return true;
        }
    }

    //SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

    @Override
    public void onBackPressed() {
        if(web.canGoBack()){
            web.goBack();
        } else {
            super.onBackPressed();
            if (mTimer != null) {                              // останавливаем таймер определения соединения с сервером
                mTimer.cancel();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        //final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        //final View decorView = getWindow().getDecorView();
        //decorView.setSystemUiVisibility(uiOptions);
    }
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (connectivity() == false) {

                        Toast t = Toast.makeText(ctx,"Сервер не доступен! Повторите вход через пару минут.", Toast.LENGTH_LONG);
                        TextView message = (TextView) t.getView().findViewById(android.R.id.message);
                        if( message != null) {
                            message.setGravity(Gravity.CENTER);
                            t.setGravity(0,0,0);
                            t.show();
                        }

                        mTimer.cancel();
                        finish();
                    }
                }
            });
        }	}
}

