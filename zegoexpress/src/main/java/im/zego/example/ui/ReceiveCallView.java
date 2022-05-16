package im.zego.example.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import im.zego.example.R;
import im.zego.example.databinding.LayoutReceiveCallBinding;

public class ReceiveCallView extends FrameLayout {

    private LayoutReceiveCallBinding binding;
    private OnReceiveCallViewClickedListener listener;
    private ReceiveCallData receiveCallData;


    public ReceiveCallView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public ReceiveCallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ReceiveCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public ReceiveCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        binding = LayoutReceiveCallBinding.inflate(LayoutInflater.from(context), this, true);
        binding.dialogCallAcceptVoice.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptAudioClicked();
            }
        });
        binding.dialogCallAcceptVideo.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptVideoClicked();
            }
        });
        binding.dialogCallDecline.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeclineClicked();
            }
        });
        binding.dialogReceiveCall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWindowClicked();
            }
        });
        if (receiveCallData != null) {
            setReceiveCallData(receiveCallData);
        }
    }

    public void setReceiveCallData(ReceiveCallData callData) {
        this.receiveCallData = callData;
        if (callData.callType == ReceiveCallData.Voice) {
            binding.dialogCallAcceptVoice.setVisibility(View.VISIBLE);
            binding.dialogCallAcceptVideo.setVisibility(View.GONE);
        } else {
            binding.dialogCallAcceptVoice.setVisibility(View.GONE);
            binding.dialogCallAcceptVideo.setVisibility(View.VISIBLE);
        }
        if (callData.callUserName != null) {
            binding.dialogCallName.setText(callData.callUserName);
        }
        //            binding.dialogCallIcon.setImageDrawable(userIcon);

        if (callData.callType == ReceiveCallData.Voice) {
            binding.dialogCallType.setText(R.string.zego_voice_call);
        } else {
            binding.dialogCallType.setText(R.string.zego_video_call);
        }
    }

    public void setListener(OnReceiveCallViewClickedListener listener) {
        this.listener = listener;
    }

    public interface OnReceiveCallViewClickedListener {

        void onAcceptAudioClicked();

        void onAcceptVideoClicked();

        void onDeclineClicked();

        void onWindowClicked();
    }

    public static class ReceiveCallData {

        public String callUserName;
        public String callUserIcon;
        public String callUserID;
        public int callType;
        public static final int Video = 0;
        public static final int Voice = 1;
    }
}
