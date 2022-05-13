package im.zego.easyexample.android.cloudmessage;

public class CloudMessageManager {

    private CloudMessageManager() {
    }

    private static final class Holder {

        private static final CloudMessageManager INSTANCE = new CloudMessageManager();
    }

    public static CloudMessageManager getInstance() {
        return Holder.INSTANCE;
    }

    private CloudMessageListener listener;

    public void setListener(CloudMessageListener listener) {
        this.listener = listener;
    }

    private CloudMessage cloudMessage;

    public void onMessageReceived(CloudMessage cloudMessage) {
        this.cloudMessage = cloudMessage;
        if (listener != null) {
            listener.onMessageReceived(cloudMessage);
        }
    }

    public CloudMessage getCloudMessage() {
        return cloudMessage;
    }

    public void clearCloudMessage() {
        cloudMessage = null;
    }

    public interface CloudMessageListener {

        void onMessageReceived(CloudMessage cloudMessage);
    }
}
