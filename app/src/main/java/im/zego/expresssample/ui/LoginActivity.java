package im.zego.expresssample.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.permissionx.guolindev.PermissionX;
import im.zego.expresssample.databinding.ActivityLoginBinding;
import im.zego.expresssample.express.AppCenter;
import im.zego.zegoexpress.ExpressManager;
import im.zego.zegoexpress.ZegoMediaOptions;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.Random;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initZEGOExpressSDK();
        binding.joinLiveAsHost.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onJoinRoomClicked(true);
            }
        });
        binding.joinLiveAsAudience.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onJoinRoomClicked(false);
            }
        });
    }

    private void initZEGOExpressSDK() {
        ExpressManager.getInstance().createEngine(getApplication(), AppCenter.appID, AppCenter.appSign);
        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .request((allGranted, grantedList, deniedList) -> {
            });
    }

    private void onJoinRoomClicked(boolean asHost) {
        if (!checkAppID()) {
            Toast.makeText(getApplication(),
                "please set your appID to AppCenter.java", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateInput()) {
            Toast.makeText(getApplication(),
                "input cannot be null", Toast.LENGTH_SHORT).show();
            return;
        }
        PermissionX.init(LoginActivity.this)
            .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .request((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    joinRoom(binding.joinRoomId.getText().toString(), asHost, new IZegoRoomLoginCallback() {
                        @Override
                        public void onRoomLoginResult(int errorCode, JSONObject jsonObject) {
                            if (errorCode == 0) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("asHost", asHost);
                                startActivity(intent);
                            }
                        }
                    });
                }
            });
    }

    private void joinRoom(String roomID, boolean joinAsHost, IZegoRoomLoginCallback callback) {
        binding.loginLoading.setVisibility(View.VISIBLE);
        Random random = new Random(System.currentTimeMillis());
        String userID = System.currentTimeMillis() + "";
        String username = Build.MANUFACTURER + random.nextInt(2048);
        ZegoUser user = new ZegoUser(userID, username);
        int mediaOptions = ZegoMediaOptions.autoPlayAudio | ZegoMediaOptions.autoPlayVideo;
        if (joinAsHost) {
            mediaOptions = mediaOptions |
                ZegoMediaOptions.publishLocalAudio | ZegoMediaOptions.publishLocalVideo;
        }
        ExpressManager.getInstance().joinRoom(roomID, user, mediaOptions, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject jsonObject) {
                binding.loginLoading.setVisibility(View.GONE);
                if (callback != null) {
                    callback.onRoomLoginResult(errorCode, jsonObject);
                }
            }
        });
    }

    private boolean checkAppID() {
        return AppCenter.appID != 0L && !TextUtils.isEmpty(AppCenter.appSign);
    }

    private boolean validateInput() {
        return binding.joinRoomId.getText().length() > 0;
    }
}