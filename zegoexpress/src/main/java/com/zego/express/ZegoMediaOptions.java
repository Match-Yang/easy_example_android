package com.zego.express;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ZegoMediaOptions {

    public static final int autoPlayAudio = 1;
    public static final int autoPlayVideo = 2;
    public static final int publishLocalAudio = 4;
    public static final int publishLocalVideo = 8;

    @IntDef({autoPlayAudio, autoPlayVideo, publishLocalAudio, publishLocalVideo})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ZegoMediaOptionsFlag {

    }

    public static boolean autoPlayAudio(int options) {
        return options > 0 && (options & autoPlayAudio) != 0;
    }

    public static boolean autoPlayVideo(int options) {
        return options > 0 && (options & autoPlayVideo) != 0;
    }

    public static boolean autoPublishLocalAudio(int options) {
        return options > 0 && (options & publishLocalAudio) != 0;
    }

    public static boolean autoPublishLocalVideo(int options) {
        return options > 0 && (options & publishLocalVideo) != 0;
    }
}
