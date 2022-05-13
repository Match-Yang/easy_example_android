package im.zego.easyexample.android;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import im.zego.easyexample.android.cloudmessage.CloudMessage;
import java.io.IOException;
import java.util.Objects;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpClient {

    private static final String TAG = "HttpClient";
    private OkHttpClient client;

    private HttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(Level.BODY);
        client = new OkHttpClient.Builder()
            .addInterceptor(logging)
            .build();
    }

    private static final class Holder {

        private static final HttpClient INSTANCE = new HttpClient();
    }

    public static HttpClient getInstance() {
        return HttpClient.Holder.INSTANCE;
    }

    public static final MediaType JSON
        = MediaType.get("application/json; charset=utf-8");
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    String baseUrl = "https://easy-example-call.herokuapp.com";

    public void registerFCMToken(String userID, String token, HttpResult result) {
        try {
            Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
            builder.appendEncodedPath("store_fcm_token");
            String url = builder.build().toString();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userID", userID);
            jsonObject.put("token", token);
            post(url, jsonObject.toString(), new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (result != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                result.onResult(-1, e.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (Objects.equals(response.body().string(), "ok")) {
                        if (result != null) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    result.onResult(0, "");
                                }
                            });
                        }
                    } else {
                        if (result != null) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    result.onResult(-1, "");
                                }
                            });
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void callUserByCloudMessage(CloudMessage cloudMessage, HttpResult result) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendEncodedPath("call_invite");
        String url = builder.build().toString();
        post(url, cloudMessage.toJsonString(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (result != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            result.onResult(-1, e.getMessage());
                        }
                    });

                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string());
                    int errorCode = jsonObject.getInt("ret");
                    String message = jsonObject.getString("message");
                    if (result != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                result.onResult(errorCode, message);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (result != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                result.onResult(-1, "");
                            }
                        });
                    }
                }
            }
        });
    }

    public void getRTCToken(String userID, HttpResult result) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendPath("access_token");
        builder.appendQueryParameter("uid", userID);
        String url = builder.build().toString();
        get(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (result != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            result.onResult(-1, e.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string());
                    String token = jsonObject.getString("token");
                    if (!TextUtils.isEmpty(token)) {
                        if (result != null) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    result.onResult(0, token);
                                }
                            });
                        }
                    } else {
                        if (result != null) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    result.onResult(-1, "");
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (result != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                result.onResult(-1, "");
                            }
                        });
                    }
                }
            }
        });
    }

    private void post(String url, String json, Callback callback) {
        Log.d(TAG, "post() called with: url = [" + url + "], json = [" + json + "], callback = [" + callback + "]");
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        client.newCall(request).enqueue(callback);
    }

    private void get(String url, Callback callback) {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        client.newCall(request).enqueue(callback);
    }

    public interface HttpResult {

        void onResult(int errorCode, String result);
    }
}
