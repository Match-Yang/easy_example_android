# ZEGOCLOUD easy example
<p align="center">
    <a href="https://github.com/ZEGOCLOUD/easy_example_android/releases"><img src="https://img.shields.io/github/downloads/ZEGOCLOUD/easy_example_android/total" alt="Platform"/></a>
    <a href="https://discord.gg/EtNRATttyp">
        <img src="https://img.shields.io/discord/980014613179555870?color=5865F2&logo=discord&logoColor=white" alt="ZEGOCLOUD"/>
    </a>
    <p>
    <p>Click the search button below to search documentation or error code</p>
    <a href="https://docs.zegocloud.com/">
        <img width="589" alt="image" src="https://user-images.githubusercontent.com/5242852/170823639-2a684b4c-0bad-445a-b04e-5d4b6912e900.png">
    </a>
    </p>
</p>

ZEGOCLOUD's easy example is a simple wrapper around our RTC product. You can refer to the sample code for quick integration.

## Getting started

### Prerequisites

* [Android Studio Bumblebee or later](https://developer.android.com/studio)
* An Android device or Simulator that is running on Android 5.1 or later and supports audio and video. We recommend you use a real device.
* Create a project in [ZEGOCLOUD Admin Console](https://console.zegocloud.com/). For details, see [ZEGO Admin Console - Project management](https://docs.zegocloud.com/article/1271).

###  Clone the repository
1. Clone the easy example Github repository. 

### Modify the project configurations
![](media/16496764650900/16496772462635.png)
You need to modify `appID` and `serverSecret` to your own account, which can be obtained in the [ZEGO Admin Console](https://console.zegocloud.com/).

### Run the sample code

1. Connect the Android device to your computer.

2. Open Android Studio, select the Android device you are using,click the **Run 'app'** in the upper center to run the sample code and experience the Live Audio Room service.

## Integrate into your own project

### Introduce SDK

In your `setting.gradle` file, add the jitpack maven .
``` groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url 'https://www.jitpack.io' } // <- Add this line
    }
}
```

### Import the source code module

1. import the `:zegoexpress` module to your project,Choose the `:zegoexpress` directory. 
![](docs/images/import_zegoexpress_module.jpg)


2. add
dependency in your app's build.gradle's dependencies:
![](docs/images/zegoexpress_module.gif)
```groovy
dependencies{
   implementation project(':zegoexpress') 
}
```

### Method call
The calling sequence of the SDK interface is as follows:
createEngine --> joinRoom --> setLocalVideoView/setRemoteVideoView --> leaveRoom

#### Create engine
Before using the SDK function, you need to create the SDK first. We recommend creating it when the application starts. The sample code is as follows:
```java
 ExpressManager.getInstance().createEngine(getApplication(), AppCenter.appID);
```

#### Join room
When you want to communicate with audio and video, you need to call the join room interface first. According to your business scenario, you can set different audio and video controls through options, such as:

1. call scene：[.autoPlayVideo, .autoPlayAudio, .publishLocalAudio, .publishLocalVideo]
2. Live scene - host: [.autoPlayVideo, .autoPlayAudio, .publishLocalAudio, .publishLocalVideo]
3. Live scene - audience:[.autoPlayVideo, .autoPlayAudio]
4. Chat room - host:[.autoPlayAudio, .publishLocalAudio]
5. Chat room - audience:[.autoPlayAudio]

The following sample code is an example of a call scenario:
```java
   private void joinRoom(Callback callback) {
        String username = binding.username.getText().toString();
        String roomid = binding.roomid.getText().toString();
        String userID = System.currentTimeMillis() + "";
        ZegoUser user = new ZegoUser(userID, username);
        String token = ExpressManager.generateToken(userID, AppCenter.appID, AppCenter.serverSecret);
        int mediaOptions = ZegoMediaOptions.autoPlayAudio | ZegoMediaOptions.autoPlayVideo |
            ZegoMediaOptions.publishLocalAudio | ZegoMediaOptions.publishLocalVideo;
        ExpressManager.getInstance().joinRoom(roomid, user, token, mediaOptions, callback);
    }
```
#### set video view
If your project needs to use the video communication function, you need to set the View for displaying the video, call `setLocalVideoView` for the local video, and call `setRemoteVideoView` for the remote video.

**setLocalVideoView:**
```java
   ExpressManager.getInstance().setLocalVideoView(binding.localTexture);
```

**setLocalVideoView:**
```java
    public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                if (updateType == ZegoUpdateType.ADD) {
                    for (int i = 0; i < userList.size(); i++) {
                        ZegoUser user = userList.get(i);
                        TextureView remoteTexture = binding.remoteTexture;
                        ExpressManager.getInstance().setRemoteVideoView(user.userID, remoteTexture);
                    }
                } else {
                    
                }
            }
```

#### leave room
When you want to leave the room, you can call the leaveroom interface.
```java
 ExpressManager.getInstance().leaveRoom();
```
