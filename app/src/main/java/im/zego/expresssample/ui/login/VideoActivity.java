package im.zego.expresssample.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;
import com.zego.express.ExpressManager;
import com.zego.express.ExpressManager.ExpressManagerHandler;
import com.zego.express.ZegoDeviceUpdateType;
import com.zego.express.ZegoParticipant;
import im.zego.expresssample.databinding.ActivityVideoBinding;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;

public class VideoActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityVideoBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ZegoParticipant localParticipant = ExpressManager.getInstance().getLocalParticipant();
        setRenderToView(localParticipant.userID, false, true);

        binding.logoutRoom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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

        ExpressManager.getInstance().setExpressHandler(new ExpressManagerHandler() {
            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                if (updateType == ZegoUpdateType.ADD) {
                    for (int i = 0; i < userList.size(); i++) {
                        ZegoUser user = userList.get(i);
                        setRenderToView(user.userID, true, false);
                    }
                } else {
                    setRenderToView("", true, false);
                }
            }

            @Override
            public void onRoomUserDeviceUpdate(ZegoDeviceUpdateType updateType, String userID, String roomID) {

            }

            @Override
            public void onRoomTokenWillExpire(String roomID, int remainTimeInSecond) {

            }
        });
    }

    /**
     * @param userID       if userID is empty,no render
     * @param toFullView   render to full texture or not
     * @param isLocalVideo is self device's video or not
     */
    public void setRenderToView(String userID, boolean toFullView, boolean isLocalVideo) {
        Log.d(TAG, "setRenderToView() called with: userID = [" + userID + "], toFullView = [" + toFullView
            + "], isLocalVideo = [" + isLocalVideo + "]");
        ZegoParticipant participant = ExpressManager.getInstance().getParticipant(userID);
        if (toFullView) {
            if (TextUtils.isEmpty(userID)) {

            } else {
                if (isLocalVideo) {
                    ExpressManager.getInstance().setLocalVideoView(binding.fullViewTexture);
                } else {
                    ExpressManager.getInstance().setRemoteVideoView(userID, binding.fullViewTexture);
                }
                if (participant != null) {
                    binding.fullViewName.setText(participant.name);
                }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ExpressManager.getInstance().leaveRoom();
    }
}