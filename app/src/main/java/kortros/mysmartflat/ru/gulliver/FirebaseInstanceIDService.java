package kortros.mysmartflat.ru.gulliver;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static android.content.ContentValues.TAG;

/**
 * Created by aj20010319 on 9/6/2016.
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String newToken = FirebaseInstanceId.getInstance().getToken();
        //sendToken(newToken);
        Log.i(TAG, "FCM Registration Token: " + newToken);
    }
/*
    private void sendToken(String newToken) {
        OkHttpClient client =  new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token",newToken)
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.0.101/firebase/addtoken.php")
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}