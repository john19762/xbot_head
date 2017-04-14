package cn.ac.iscas.xlab.droidfacedog.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.ac.iscas.xlab.droidfacedog.XBotFace;
import cn.ac.iscas.xlab.droidfacedog.youtu.RecogResult;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by lisongting on 2017/4/12.
 */

public class ImgToServerUtil {
    public static final int RECOG_SUCCESS = 0;
    public static final int RECOG_REJECTED = 1;
    public static final int RECOG_TIMEOUT = 2;
    public static final int RECOG_INVALID_URL = 3;
    private static final int RECOG_SERVER_ERROR = 4;
    public static final String TAG = "xxlab";
    public static final String SERVER_IP_ADDRESS = "server_ip_address";
    public static final String DEFAULT_IP = "192.168.0.111";
    public static final double RECOG_THRESHOLD = 0.60;
    private static RecogResult mRecogResult;
    private static String serverAddress;
    private static RecogResult recogResult;
    private static Pattern IP_ADDRESS = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");

    public static void sendToServer(final Bitmap bitmap, final Context context) {

        //USE : RxJava
        //build an observable object,this object is observed by defined observer
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(final ObservableEmitter<Integer> e) throws Exception {
                if (serverAddress.equals("")) {
                    e.onNext(RECOG_INVALID_URL);
                    e.onComplete();
                }

                if (serverAddress == null) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    serverAddress = prefs.getString(SERVER_IP_ADDRESS, DEFAULT_IP);
                }


                Matcher matcher = IP_ADDRESS.matcher(serverAddress);
                if (!matcher.matches()) {
                    Log.d(TAG, "IP validation failed: " + serverAddress);
                    e.onNext(RECOG_INVALID_URL);
                    e.onComplete();
                }


                //encode the face into Stirng ,and then put it into a jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("Image", encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 100));
                String jsonString = jsonObject.toString();
                Log.d(TAG, jsonString);

                //create an OKHttpClient object
                OkHttpClient okHttpClient = new OkHttpClient();

                Request.Builder builder = new Request.Builder();

                //create requestBody,which carry infomation that would be sent to server
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonString);

                Request request = builder.url("http://" + serverAddress + ":8000/recognition")
                        .post(requestBody)
                        .addHeader("Content-Length",Integer.toString(jsonString.length()))
                        .addHeader("Content-Type","application/json")
                        .addHeader("Accept-Encoding", "identity")
                        .addHeader("Accept", "text/plain")
                        .build();

                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException ex) {
                        Log.e(TAG, "Network request error");
                        e.onNext(RECOG_SERVER_ERROR);
                        e.onComplete();
                    }

                    //when server returns to OKHttpClient successfully, this method would be called
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        int code = response.code();
                        if (code < 400) {
                            InputStream in = response.body().byteStream();
                            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

                            if (!recogResult.parseFrom(reader)){
                                e.onNext(RECOG_SERVER_ERROR);
                                e.onComplete();
                            }else{
                                mRecogResult = recogResult;
                                e.onNext(RECOG_SUCCESS);
                                e.onComplete();
                            }
                        }
                    }
                });
            }
        });
        //make the event(network process of sending bitmap) happens in new Thread
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    //this method is called after @ObservableEmitter<Integer> e calling its onNext() method
                    @Override
                    public void onNext(Integer value) {
                        if (!(context instanceof XBotFace))
                            return;
                        XBotFace activity = (XBotFace) context;
                        String msg;
                        switch (value) {
                            case RECOG_INVALID_URL:
                                Log.e(TAG, "Invalid URL!");
                                break;
                            case RECOG_TIMEOUT:
                                Log.e(TAG, "Connection time out");
                                break;
                            case RECOG_SERVER_ERROR:
                                Log.e(TAG, "Server error");
                                msg = "YOUTU: ret = " +RECOG_SERVER_ERROR
                                        + "RecogResult: null";
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                break;
                            case RECOG_SUCCESS:
                                Log.d(TAG, "Success!");
                                msg = "YOUTU: ret = " + RECOG_SUCCESS + ", confidence = " +
                                        Double.toString(mRecogResult.getConfidence()) + ", id = '" +
                                        mRecogResult.getId() + "'";
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                                if ( mRecogResult.getConfidence() >= RECOG_THRESHOLD) {
                                    activity.updateFaceState(XBotFace.IDENTIFIEDSTATE);
                                    MediaPlayer ttsUserId = activity.lookupNames(mRecogResult.getId());
                                    activity.prepareGreetingTTS(ttsUserId);
                                } else {
                                    activity.prepareGreetingTTS();
                                }

                                Log.w(TAG, "activity.startPlayTTS();");
                                activity.startPlayTTS();
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "request error");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "Sending thread  completed");
                    }
                });
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    // http://stackoverflow.com/questions/9768611/encode-and-decode-bitmap-object-in-base64-string-in-android
    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
