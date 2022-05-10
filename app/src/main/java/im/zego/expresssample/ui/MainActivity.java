package im.zego.expresssample.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;
import im.zego.expresssample.databinding.ActivityMainBinding;
import im.zego.zegoexpress.ExpressManager;
import im.zego.zegoexpress.ExpressManager.ExpressManagerHandler;
import im.zego.zegoexpress.ZegoDeviceUpdateType;
import im.zego.zegoexpress.ZegoParticipant;
import im.zego.zegoexpress.callback.IZegoRoomSetRoomExtraInfoCallback;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static final String CO_HOST_ID = "coHostID";
    public static final String HOST_ID = "hostID";
    private ActivityMainBinding binding;
    private static final String TAG = "LoginActivity";
    /**
     * join as host or not
     */
    private boolean joinAsHost;
    private String hostID = "";
    private String coHostID = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        joinAsHost = getIntent().getBooleanExtra("asHost", false);
        ZegoParticipant localParticipant = ExpressManager.getInstance().getLocalParticipant();
        if (joinAsHost) {
            ExpressManager.getInstance().setRoomExtraInfo(HOST_ID, localParticipant.userID);
            hostID = localParticipant.userID;
            // if join as host,preview host's video view in fullview
            setRenderToView(hostID, true, true);
            binding.cohostBtn.setVisibility(View.GONE);
        }

        ExpressManager.getInstance().setExpressHandler(new ExpressManagerHandler() {
            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                if (updateType == ZegoUpdateType.ADD) {
                    for (int i = 0; i < userList.size(); i++) {
                        ZegoUser user = userList.get(i);
                        if (user.userID.equals(hostID)) {
//                            if (!joinAsHost) {
//                                // if join as audience,play host's video view in fullview
//                                setRenderToView(hostID, true, false);
//                            }
                        }
                    }
                } else {
                    ZegoParticipant localParticipant = ExpressManager.getInstance().getLocalParticipant();
                    for (int i = 0; i < userList.size(); i++) {
                        ZegoUser user = userList.get(i);
                        if (joinAsHost || localParticipant.userID.equals(coHostID)) {
                            if (user.userID.equals(coHostID)) {
                                ExpressManager.getInstance().setRoomExtraInfo(CO_HOST_ID, "");
                                coHostID = "";
                                setRenderToView(coHostID, false, false);
                            }
                        }
                    }
                }
            }

            @Override
            public void onRoomUserDeviceUpdate(ZegoDeviceUpdateType updateType, String userID, String roomID) {
                Log.d(TAG,
                    "onRoomUserDeviceUpdate() called with: updateType = [" + updateType + "], userID = [" + userID
                        + "], roomID = [" + roomID + "]");
                if (updateType == ZegoDeviceUpdateType.cameraOpen) {
                } else if (updateType == ZegoDeviceUpdateType.cameraClose) {
                }
            }

            @Override
            public void onRoomTokenWillExpire(String roomID, int remainTimeInSecond) {

            }

            @Override
            public void onRoomExtraInfoUpdate(String roomID, ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
                for (ZegoRoomExtraInfo info : roomExtraInfoList) {
                    Log.d(TAG, "onRoomExtraInfoUpdate() called with: info.key = [" + info.key + "], info.value = ["
                        + info.value + "]");
                    if (HOST_ID.equals(info.key)) {
                        hostID = info.value;
                        if (!joinAsHost) {
                            // if join as audience,play host's video view in fullview
                            setRenderToView(hostID, true, false);
                        }
                    }
                    if (CO_HOST_ID.equals(info.key)) {
                        coHostID = info.value;
                        setRenderToView(coHostID, false, false);
                    }
                }
            }

            @Override
            public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode,
                JSONObject extendedData) {
            }
        });

        binding.logoutRoom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpressManager.getInstance().leaveRoom();
                finish();
            }
        });

        binding.switchBtn.setSelected(joinAsHost);
        binding.switchBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                ExpressManager.getInstance().switchFrontCamera(!selected);
            }
        });

        binding.cameraBtn.setSelected(joinAsHost);
        binding.cameraBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                ExpressManager.getInstance().enableCamera(!selected);
            }
        });
        binding.micBtn.setSelected(joinAsHost);
        binding.micBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                ExpressManager.getInstance().enableMic(!selected);
            }
        });

        binding.cohostBtn.setSelected(false);
        binding.cohostBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                if (selected) {
                    ExpressManager.getInstance().setRoomExtraInfo(CO_HOST_ID, "");
                    coHostID = "";
                    binding.cameraBtn.setSelected(false);
                    ExpressManager.getInstance().enableCamera(false);
                    binding.micBtn.setSelected(false);
                    ExpressManager.getInstance().enableMic(false);
                    setRenderToView(coHostID, false, true);
                } else {
                    ExpressManager.getInstance().setRoomExtraInfo(CO_HOST_ID, localParticipant.userID,
                        new IZegoRoomSetRoomExtraInfoCallback() {
                            @Override
                            public void onRoomSetRoomExtraInfoResult(int errorCode) {
                                if (errorCode == 0) {
                                    coHostID = localParticipant.userID;
                                    binding.cameraBtn.setSelected(true);
                                    ExpressManager.getInstance().enableCamera(true);
                                    binding.micBtn.setSelected(true);
                                    ExpressManager.getInstance().enableMic(true);
                                    setRenderToView(coHostID, false, true);
                                }
                            }
                        });
                }
            }
        });
    }

    public void setRenderToView(String userID, boolean toFullView, boolean isLocalVideo) {
        Log.d(TAG, "setRenderToView() called with: userID = [" + userID + "], toFullView = [" + toFullView
            + "], isLocalVideo = [" + isLocalVideo + "]");
        ZegoParticipant participant = ExpressManager.getInstance().getParticipant(userID);
        if (toFullView) {
            if (isLocalVideo) {
                ExpressManager.getInstance().setLocalVideoView(binding.fullViewTexture);
            } else {
                ExpressManager.getInstance().setRemoteVideoView(userID, binding.fullViewTexture);
            }
            if (participant != null) {
                binding.fullViewName.setText(participant.name);
            }
        } else {
            if (TextUtils.isEmpty(userID)) {
                binding.smallViewLayout.setVisibility(View.GONE);
            } else {
                binding.smallViewLayout.setVisibility(View.VISIBLE);
                if (isLocalVideo) {
                    ExpressManager.getInstance().setLocalVideoView(binding.smallViewTexture);
                } else {
                    ExpressManager.getInstance().setRemoteVideoView(userID, binding.smallViewTexture);
                }
                if (participant != null) {
                    binding.smallViewName.setText(participant.name);
                }
            }
        }
    }
}