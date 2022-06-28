package im.zego.expresssample.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;
import com.zego.express.ExpressManager;
import com.zego.express.ExpressManager.ExpressManagerHandler;
import com.zego.express.ZegoDeviceUpdateType;
import im.zego.expresssample.databinding.ActivityAudioBinding;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;

/**
 *  activity show one-one audio communicate
 */
public class AudioActivity extends AppCompatActivity {

    private ActivityAudioBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.voiceMicBtn.setSelected(true);
        binding.voiceMicBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                ExpressManager.getInstance().enableMic(!selected);
            }
        });
        binding.voiceHangupBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.voiceSpeakerBtn.setSelected(true);
        binding.voiceSpeakerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                ExpressManager.getInstance().enableSpeaker(!selected);
            }
        });

        ExpressManager.getInstance().setExpressHandler(new ExpressManagerHandler() {
            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                if (updateType == ZegoUpdateType.ADD) {
                    for (int i = 0; i < userList.size(); i++) {
                        ZegoUser user = userList.get(i);
                        binding.voiceName.setText(user.userName);
                    }
                } else {
                    binding.voiceName.setText("");
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ExpressManager.getInstance().leaveRoom();
    }
}