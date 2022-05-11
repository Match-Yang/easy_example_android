package im.zego.expresssample.ui.login;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.permissionx.guolindev.PermissionX;
import im.zego.expresssample.express.AppCenter;
import im.zego.expresssample.databinding.ActivityLoginBinding;
import im.zego.expresssample.express.ExpressManager;
import im.zego.expresssample.express.ExpressManager.ExpressManagerHandler;
import im.zego.expresssample.express.ZegoDeviceUpdateType;
import im.zego.expresssample.express.ZegoMediaOptions;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private static final String TAG = "LoginActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.username.setText(Build.MANUFACTURER);
        binding.roomid.setText("123765");

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkAppID()) {
                    Toast.makeText(getApplication(),
                        "please set your appID to AppCenter.java", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (! validateInput()) {
                    Toast.makeText(getApplication(),
                        "input cannot be null", Toast.LENGTH_SHORT).show();
                    return;
                }
                PermissionX.init(LoginActivity.this)
                    .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            String username = binding.username.getText().toString();
                            String roomID = binding.roomid.getText().toString();
                            joinRoom(roomID, username);
                        }
                    });
            }
        });

        ExpressManager.getInstance().createEngine(getApplication(), AppCenter.appID);
        ExpressManager.getInstance().setExpressHandler(new ExpressManagerHandler() {
            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                if (updateType == ZegoUpdateType.ADD) {
                    for (int i = 0; i < userList.size(); i++) {
                        ZegoUser user = userList.get(i);
                        TextureView remoteTexture = binding.remoteTexture;
                        binding.remoteName.setText(user.userName);
                        setRemoteViewVisible(true);
                        ExpressManager.getInstance().setRemoteVideoView(user.userID, remoteTexture);
                    }
                } else {
                    setRemoteViewVisible(false);
                }
            }

            @Override
            public void onRoomUserDeviceUpdate(ZegoDeviceUpdateType updateType, String userID, String roomID) {
                Log.d(TAG,
                    "onRoomUserDeviceUpdate() called with: updateType = [" + updateType + "], userID = [" + userID
                        + "], roomID = [" + roomID + "]");
                if (updateType == ZegoDeviceUpdateType.cameraOpen) {
                    setRemoteViewVisible(true);
                } else if (updateType == ZegoDeviceUpdateType.cameraClose) {
                    setRemoteViewVisible(false);
                }
            }

            @Override
            public void onRoomTokenWillExpire(String roomID, int remainTimeInSecond) {

            }
        });
        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .request((allGranted, grantedList, deniedList) -> {
            });

        binding.logoutRoom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layoutLogin.setVisibility(View.VISIBLE);
                binding.layoutRoom.setVisibility(View.GONE);
                ExpressManager.getInstance().leaveRoom();
            }
        });

        binding.switchBtn.setSelected(true);
        binding.switchBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                ExpressManager.getInstance().switchFrontCamera(!selected);
            }
        });

        binding.cameraBtn.setSelected(true);
        binding.cameraBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                ExpressManager.getInstance().enableCamera(!selected);
            }
        });
        binding.micBtn.setSelected(true);
        binding.micBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                ExpressManager.getInstance().enableMic(!selected);
            }
        });
    }

    private void joinRoom(String roomID ,String username) {
        binding.loading.setVisibility(View.VISIBLE);
        String userID = System.currentTimeMillis() + "";
        ZegoUser user = new ZegoUser(userID, username);
        String token = ExpressManager.generateToken(userID, AppCenter.appID, AppCenter.serverSecret);
        int mediaOptions = ZegoMediaOptions.autoPlayAudio | ZegoMediaOptions.autoPlayVideo |
            ZegoMediaOptions.publishLocalAudio | ZegoMediaOptions.publishLocalVideo;
        ExpressManager.getInstance().joinRoom(roomID, user, token, mediaOptions, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject jsonObject) {
                binding.loading.setVisibility(View.GONE);
                if (errorCode == 0) {
                    binding.layoutLogin.setVisibility(View.GONE);
                    binding.layoutRoom.setVisibility(View.VISIBLE);
                    ExpressManager.getInstance().setLocalVideoView(binding.localTexture);
                    binding.localName.setText(binding.username.getText().toString());
                } else {
                    Toast.makeText(getApplication(), "join room failed,errorCode :" + errorCode,
                        Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validateInput() {
        return binding.username.getText().length() > 0 && binding.roomid.getText().length() > 0;
    }

    private boolean checkAppID() {
        return AppCenter.appID != 0L && !TextUtils.isEmpty(AppCenter.serverSecret);
    }

    private void setRemoteViewVisible(boolean visible) {
        binding.remoteTexture.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

}