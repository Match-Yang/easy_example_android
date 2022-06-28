package com.zego.express;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public class ExpressManager {

    private ExpressManager() {
    }

    private static final class Holder {

        private static final ExpressManager INSTANCE = new ExpressManager();
    }

    public static ExpressManager getInstance() {
        return Holder.INSTANCE;
    }

    private static final String TAG = "ExpressManager";
    // key is UserID, value is participant model
    private Map<String, ZegoParticipant> participantMap = new HashMap<>();
    // key is streamID, value is participant model
    private Map<String, ZegoParticipant> streamUserMap = new HashMap<>();
    private Map<String, WeakReference<TextureView>> streamViewMap = new HashMap<>();
    private ZegoParticipant localParticipant;
    private int mediaOptions;
    private String roomID;
    private ExpressManagerHandler handler;

    public void createEngine(Application application, long appID) {
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.scenario = ZegoScenario.GENERAL;
        profile.application = application;
        ZegoEngineConfig config = new ZegoEngineConfig();
        ZegoExpressEngine.setEngineConfig(config);
        ZegoExpressEngine.createEngine(profile, new IZegoEventHandler() {
            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                super.onRoomUserUpdate(roomID, updateType, userList);
                Log.d(TAG, "onRoomUserUpdate() called with: roomID = [" + roomID + "], updateType = [" + updateType
                    + "], userList = [" + userList + "]");
                if (updateType == ZegoUpdateType.ADD) {
                    for (ZegoUser zegoUser : userList) {
                        ZegoParticipant participant = new ZegoParticipant(zegoUser.userID, zegoUser.userName);
                        participant.streamID = generateStreamID(participant.userID, roomID);
                        participantMap.put(participant.userID, participant);
                        streamUserMap.put(participant.streamID, participant);
                    }
                } else {
                    for (ZegoUser zegoUser : userList) {
                        ZegoParticipant participant = participantMap.get(zegoUser.userID);
                        if (participant != null) {
                            participantMap.remove(participant.userID);
                            streamUserMap.remove(participant.streamID);
                            WeakReference<TextureView> weakReference = streamViewMap.remove(participant.streamID);
                            if (weakReference != null) {
                                weakReference.clear();
                            }
                        }
                    }
                }
                if (handler != null) {
                    handler.onRoomUserUpdate(roomID, updateType, userList);
                }
            }

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,
                JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                for (ZegoStream zegoStream : streamList) {
                    if (updateType == ZegoUpdateType.ADD) {
                        WeakReference<TextureView> weakReference = streamViewMap.get(zegoStream.streamID);
                        if (weakReference != null) {
                            playStream(zegoStream.streamID, weakReference.get());
                        } else {
                            playStream(zegoStream.streamID, null);
                        }
                    } else {
                        stopPlayStream(zegoStream.streamID);
                    }
                }
            }

            @Override
            public void onRemoteCameraStateUpdate(String streamID, ZegoRemoteDeviceState state) {
                super.onRemoteCameraStateUpdate(streamID, state);
                Log.d(TAG,
                    "onRemoteCameraStateUpdate() called with: streamID = [" + streamID + "], state = [" + state + "]");
                ZegoParticipant participant = streamUserMap.get(streamID);
                if (participant != null) {
                    boolean isDeviceOpen = ZegoRemoteDeviceState.OPEN == state;
                    participant.camera = isDeviceOpen;
                    ZegoDeviceUpdateType type =
                        isDeviceOpen ? ZegoDeviceUpdateType.cameraOpen : ZegoDeviceUpdateType.cameraClose;
                    if (handler != null) {
                        handler.onRoomUserDeviceUpdate(type, participant.userID, roomID);
                    }
                }
            }

            @Override
            public void onRemoteMicStateUpdate(String streamID, ZegoRemoteDeviceState state) {
                super.onRemoteMicStateUpdate(streamID, state);
                Log.d(TAG,
                    "onRemoteMicStateUpdate() called with: streamID = [" + streamID + "], state = [" + state + "]");
                ZegoParticipant participant = streamUserMap.get(streamID);
                if (participant != null) {
                    boolean isDeviceOpen = ZegoRemoteDeviceState.OPEN == state;
                    participant.mic = isDeviceOpen;
                    ZegoDeviceUpdateType type =
                        isDeviceOpen ? ZegoDeviceUpdateType.micUnmute : ZegoDeviceUpdateType.micMute;
                    if (handler != null) {
                        handler.onRoomUserDeviceUpdate(type, participant.userID, roomID);
                    }
                }
            }

            @Override
            public void onNetworkQuality(String userID, ZegoStreamQualityLevel upstreamQuality,
                ZegoStreamQualityLevel downstreamQuality) {
                super.onNetworkQuality(userID, upstreamQuality, downstreamQuality);
                ZegoParticipant participant = participantMap.get(userID);
                if (participant != null) {
                    if (Objects.equals(userID, localParticipant.userID)) {
                        participant.network = downstreamQuality;
                    } else {
                        participant.network = upstreamQuality;
                    }
                }
            }

            @Override
            public void onRoomTokenWillExpire(String roomID, int remainTimeInSecond) {
                super.onRoomTokenWillExpire(roomID, remainTimeInSecond);
                Log.d(TAG, "onRoomTokenWillExpire() called with: roomID = [" + roomID + "], remainTimeInSecond = ["
                    + remainTimeInSecond + "]");
                if (handler != null) {
                    handler.onRoomTokenWillExpire(roomID, remainTimeInSecond);
                }
            }

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
                Log.d(TAG,
                    "onRoomStateUpdate() called with: roomID = [" + roomID + "], state = [" + state + "], errorCode = ["
                        + errorCode + "], extendedData = [" + extendedData + "]");
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode,
                JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                Log.d(TAG, "onPublisherStateUpdate() called with: streamID = [" + streamID + "], state = [" + state
                    + "], errorCode = [" + errorCode + "], extendedData = [" + extendedData + "]");
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode,
                JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                Log.d(TAG, "onPlayerStateUpdate() called with: streamID = [" + streamID + "], state = [" + state
                    + "], errorCode = [" + errorCode + "], extendedData = [" + extendedData + "]");
            }
        });
    }

    public void joinRoom(String roomID, ZegoUser zegoUser, String token, int mediaOptions,
        IZegoRoomLoginCallback callback) {
        participantMap.clear();
        streamUserMap.clear();
        if (TextUtils.isEmpty(token)) {
            Log.d(TAG, "Error: [joinRoom] token is empty, please enter a right token");
            return;
        }
        this.roomID = roomID;
        this.mediaOptions = mediaOptions;
        ZegoParticipant participant = new ZegoParticipant(zegoUser.userID, zegoUser.userName);
        participant.streamID = generateStreamID(participant.userID, roomID);
        localParticipant = participant;
        participantMap.put(participant.userID, participant);
        streamUserMap.put(participant.streamID, participant);
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.token = token;
        // if you need limit participant count, you can change the max member count
        config.maxMemberCount = 0;
        config.isUserStatusNotify = true;
        ZegoExpressEngine.getEngine().loginRoom(roomID, zegoUser, config, callback);

        boolean publishLocalAudio = ZegoMediaOptions.autoPublishLocalAudio(mediaOptions);
        boolean publishLocalVideo = ZegoMediaOptions.autoPublishLocalVideo(mediaOptions);
        if (publishLocalAudio || publishLocalVideo) {
            ZegoExpressEngine.getEngine().startPublishingStream(participant.streamID);
            ZegoExpressEngine.getEngine().enableCamera(publishLocalVideo);
            ZegoExpressEngine.getEngine().muteMicrophone(!publishLocalAudio);
            participant.mic = publishLocalAudio;
            participant.camera = publishLocalVideo;
        }
    }

    public void setLocalVideoView(TextureView textureView) {
        Log.d(TAG, "setLocalVideoView() called with: textureView = [" + textureView + "]");
        if (TextUtils.isEmpty(roomID)) {
            Log.d(TAG, "Error: [setLocalView] You need to join the room first and then set the videoView");
            return;
        }
        if (localParticipant == null || localParticipant.userID == null) {
            Log.d(TAG, "Error: [setLocalView] please login room pre");
            return;
        }
        String localUserID = localParticipant.userID;
        ZegoParticipant participant;
        if (participantMap.get(localUserID) == null) {
            participant = new ZegoParticipant(localUserID);
        } else {
            participant = participantMap.get(localUserID);
        }
        participant.streamID = generateStreamID(localUserID, roomID);
        localParticipant = participant;

        participantMap.put(participant.userID, participant);
        streamUserMap.put(participant.streamID, participant);
        ZegoExpressEngine.getEngine().startPreview(generateCanvas(textureView));
    }

    public void setRemoteVideoView(String userID, TextureView textureView) {
        Log.d(TAG, "setRemoteVideoView() called with: userID = [" + userID + "], textureView = [" + textureView + "]");
        if (TextUtils.isEmpty(roomID)) {
            Log.d(TAG, "Error: [setRemoteVideoView] You need to join the room first and then set the videoView");
            return;
        }
        if (TextUtils.isEmpty(userID)) {
            Log.d(TAG, "Error: [setRemoteVideoView] userID is empty, please enter a right userID");
            return;
        }

        ZegoParticipant participant;
        if (participantMap.get(userID) == null) {
            participant = new ZegoParticipant(userID);
        } else {
            participant = participantMap.get(userID);
        }
        participant.streamID = generateStreamID(userID, roomID);
        participantMap.put(participant.userID, participant);
        streamUserMap.put(participant.streamID, participant);
        playStream(participant.streamID, textureView);
        streamViewMap.put(participant.streamID, new WeakReference<>(textureView));
    }

    public void enableCamera(boolean enable) {
        Log.d(TAG, "enableCamera() called with: enable = [" + enable + "]");
        ZegoExpressEngine.getEngine().enableCamera(enable);
        localParticipant.camera = enable;
    }

    public void enableMic(boolean enable) {
        ZegoExpressEngine.getEngine().muteMicrophone(!enable);
        localParticipant.mic = !enable;
    }

    public void enableSpeaker(boolean enable) {
        ZegoExpressEngine.getEngine().muteSpeaker(!enable);
    }

    public void switchFrontCamera(boolean front) {
        ZegoExpressEngine.getEngine().useFrontCamera(front);
    }

    public void leaveRoom() {
        Log.d(TAG, "leaveRoom() called");
        participantMap.clear();
        streamUserMap.clear();
        streamViewMap.clear();
        ZegoExpressEngine.getEngine().logoutRoom();
    }

    private void playStream(String streamID, TextureView textureView) {
        boolean autoPlayVideo = ZegoMediaOptions.autoPlayVideo(mediaOptions);
        boolean autoPlayAudio = ZegoMediaOptions.autoPlayAudio(mediaOptions);
        if (autoPlayAudio || autoPlayVideo) {
            ZegoParticipant participant = streamUserMap.get(streamID);
            ZegoCanvas canvas = generateCanvas(textureView);
            ZegoExpressEngine.getEngine().startPlayingStream(streamID, canvas);
            if (!autoPlayVideo) {
                ZegoExpressEngine.getEngine().mutePlayStreamVideo(streamID, true);
            }
            if (!autoPlayAudio) {
                ZegoExpressEngine.getEngine().mutePlayStreamAudio(streamID, true);
            }
        }
    }

    private void stopPlayStream(String streamID) {
        ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
    }

    private String generateStreamID(String userID, String roomID) {
        if (TextUtils.isEmpty(userID)) {
            Log.d(TAG, "Error: [generateStreamID] userID is empty, please enter a right userID");
            return "";
        }
        if (TextUtils.isEmpty(roomID)) {
            Log.d(TAG, "Error: [generateStreamID] roomID is empty, please enter a right roomID");
            return "";
        }
        String streamID = roomID + userID + "_main";
        return streamID;
    }

    private ZegoCanvas generateCanvas(TextureView textureView) {
        ZegoCanvas canvas = new ZegoCanvas(textureView);
        canvas.viewMode = ZegoViewMode.ASPECT_FILL;
        return canvas;
    }

    public ZegoParticipant getLocalParticipant() {
        return localParticipant;
    }

    public ZegoParticipant getParticipant(String userID) {
        return participantMap.get(userID);
    }

    public void setExpressHandler(ExpressManagerHandler handler) {
        this.handler = handler;
    }

    /**
     * for security,token should be generated in server side, this method is only used for demo test,and may be
     * deprecated in future update. https://docs.zegocloud.com/article/11649
     *
     * @param userID
     * @param appID
     * @param serverSecret
     * @return
     */
    public static String generateToken(String userID, long appID, String serverSecret) {
        try {
            return TokenServerAssistant.generateToken(appID, userID, serverSecret, 60 * 60 * 24).data;
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public interface ExpressManagerHandler {

        void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList);

        void onRoomUserDeviceUpdate(ZegoDeviceUpdateType updateType, String userID, String roomID);

        void onRoomTokenWillExpire(String roomID, int remainTimeInSecond);
    }
}
