package im.zego.easyexample.android.cloudmessage;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class CloudMessage implements Parcelable {

    public String roomID;
    public String targetUserID;
    public String callerUserID;
    public String callerUserName;
    public String callerIconUrl;
    public String callType;

    public CloudMessage() {

    }

    protected CloudMessage(Parcel in) {
        roomID = in.readString();
        targetUserID = in.readString();
        callerUserID = in.readString();
        callerUserName = in.readString();
        callerIconUrl = in.readString();
        callType = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roomID);
        dest.writeString(targetUserID);
        dest.writeString(callerUserID);
        dest.writeString(callerUserName);
        dest.writeString(callerIconUrl);
        dest.writeString(callType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CloudMessage> CREATOR = new Creator<CloudMessage>() {
        @Override
        public CloudMessage createFromParcel(Parcel in) {
            return new CloudMessage(in);
        }

        @Override
        public CloudMessage[] newArray(int size) {
            return new CloudMessage[size];
        }
    };

    public String toJsonString() {
        Map<String, String> result = new HashMap<>();
        result.put("roomID", roomID);
        result.put("targetUserID", targetUserID);
        result.put("callerUserID", callerUserID);
        result.put("callerUserName", callerUserName);
        result.put("callerIconUrl", callerIconUrl);
        result.put("callType", callType);
        JSONObject jsonObject = new JSONObject(result);
        return jsonObject.toString();
    }

    public static CloudMessage parseFromMap(Map<String, String> map) {
        CloudMessage cloudMessage = new CloudMessage();
        cloudMessage.roomID = map.get("roomID");
        cloudMessage.targetUserID = map.get("targetUserID");
        cloudMessage.callerUserID = map.get("callerUserID");
        cloudMessage.callerUserName = map.get("callerUserName");
        cloudMessage.callerIconUrl = map.get("callerIconUrl");
        cloudMessage.callType = map.get("callType");
        return cloudMessage;
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
