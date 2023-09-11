package io.agora.agora_kscoreengine_example;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.agora.agora_kscoreengine_example.databinding.MainActivityBinding;
import io.agora.kscore.AgoraKScoreCallback;
import io.agora.kscore.AgoraKScoreCode;
import io.agora.kscore.AgoraKScoreEngine;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IAudioFrameObserver;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.audio.AudioParams;


public class MainActivity extends Activity {
    private final String TAG = "AgoraKScoreEngine" + MainActivity.class.getSimpleName();
    private MainActivityBinding binding;

    private ExecutorService mExecutorCacheService;
    private ExecutorService mExecutorService;

    private AgoraKScoreEngine mEngine;
    private RtcEngine mRtcEngine;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        initView();

        initEngine();
        initRtc();
    }

    private void initData() {
        mExecutorCacheService = new ThreadPoolExecutor(Integer.MAX_VALUE, Integer.MAX_VALUE,
                0, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        mExecutorService = new ThreadPoolExecutor(1, 1,
                0, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    }

    @Override
    protected void onResume() {
        super.onResume();
        handlePermission();

    }

    private void handlePermission() {

        // 需要动态申请的权限
        String permission = Manifest.permission.RECORD_AUDIO;

        //查看是否已有权限
        int checkSelfPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), permission);

        if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            //已经获取到权限  获取用户媒体资源

        } else {

            //没有拿到权限  是否需要在第二次请求权限的情况下
            // 先自定义弹框说明 同意后在请求系统权限(就是是否需要自定义DialogActivity)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

            } else {
                appRequestPermission();
            }
        }

    }

    private void appRequestPermission() {
        String[] permissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
        };
        requestPermissions(permissions, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        }
    }


    private void initEngine() {
        mEngine = new AgoraKScoreEngine.Builder(getApplicationContext())
                .enableLog(true, true)
                .samplesRate(16000, 1, 16)
                .callback(new AgoraKScoreCallback() {
                    @Override
                    public void onScoreResult(int code, String msg) {
                        Log.i(TAG, "onScoreResult: " + code + " " + msg);
                        if (code == AgoraKScoreCode.SUCCESS) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "你太棒了", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                })
                .build();
        mEngine.init();
        mEngine.switchSong("大花轿");
    }

    private void initRtc() {
        RtcEngineConfig config = new RtcEngineConfig();
        config.mContext = getApplicationContext();
        config.mAppId = BuildConfig.APP_ID;
        config.mEventHandler = new IRtcEngineEventHandler() {
            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                super.onJoinChannelSuccess(channel, uid, elapsed);
                Log.i(TAG, "onJoinChannelSuccess: ");
            }

            @Override
            public void onLeaveChannel(RtcStats stats) {
                super.onLeaveChannel(stats);
                Log.i(TAG, "onLeaveChannel: ");
                registerAudioFrame(false);
            }
        };
        config.mAudioScenario = io.agora.rtc2.Constants.AudioScenario.getValue(io.agora.rtc2.Constants.AudioScenario.DEFAULT);

        try {
            mRtcEngine = RtcEngine.create(config);
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

            Log.i(TAG, "SDK version:" + RtcEngine.getSdkVersion());
            mRtcEngine.enableAudio();
            mRtcEngine.setAudioProfile(
                    io.agora.rtc2.Constants.AUDIO_PROFILE_DEFAULT, io.agora.rtc2.Constants.AUDIO_SCENARIO_GAME_STREAMING
            );

            mRtcEngine.setRecordingAudioFrameParameters(16000, 1, io.agora.rtc2.Constants.RAW_AUDIO_FRAME_OP_MODE_READ_ONLY, 640);

            //joinChannel();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinChannel() {
        registerAudioFrame(true);
        int ret = mRtcEngine.joinChannel(
                KeyCenter.getRtcToken(KeyCenter.CHANNEL_ID, KeyCenter.getUserUid()), KeyCenter.CHANNEL_ID,
                KeyCenter.getUserUid(),
                new ChannelMediaOptions() {{
                    publishMicrophoneTrack = true;
                    publishCustomAudioTrack = true;
                    autoSubscribeAudio = true;
                    clientRoleType = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
                }});
        Log.i(TAG, "joinChannel: " + ret);
    }

    public boolean updateRoleSpeak(boolean isSpeak) {
        int ret = io.agora.rtc2.Constants.ERR_OK;
        ret += mRtcEngine.updateChannelMediaOptions(new ChannelMediaOptions() {{
            publishMicrophoneTrack = isSpeak;
            publishCustomAudioTrack = isSpeak;
        }});
        return ret == io.agora.rtc2.Constants.ERR_OK;
    }

    private void initView() {
        binding.btnStartSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinChannel();
                //updateRoleSpeak(true);
            }
        });
        binding.btnStopSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRoleSpeak(false);

                mRtcEngine.leaveChannel();
            }
        });

        binding.btnSetSongName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEngine.switchSong(binding.inputSongName.getText().toString());
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != mEngine) {
            mEngine.release();
        }

        RtcEngine.destroy();
    }

    private void registerAudioFrame(boolean enable) {
        if (enable) {
            mRtcEngine.registerAudioFrameObserver(new IAudioFrameObserver() {
                @Override
                public boolean onPlaybackAudioFrameBeforeMixing(String channelId, int uid, int type, int samplesPerChannel, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer buffer, long renderTimeMs, int avsync_type) {
                    return false;
                }

                @Override
                public boolean onPublishAudioFrame(String channelId, int type, int samplesPerChannel, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer buffer, long renderTimeMs, int avsync_type) {
                    return false;
                }

                @Override
                public boolean onRecordAudioFrame(String channelId, int type, int samplesPerChannel, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer buffer, long renderTimeMs, int avsync_type) {
                    int length = buffer.remaining();
                    byte[] origin = new byte[length];
                    buffer.get(origin);
                    buffer.flip();

                    Log.i(TAG, "onRecordAudioFrame: " + length);
                    mEngine.pushPcmData(origin);

                    return false;
                }

                @Override
                public boolean onPlaybackAudioFrame(String channelId, int type, int samplesPerChannel, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer buffer, long renderTimeMs, int avsync_type) {
                    return false;
                }

                @Override
                public boolean onMixedAudioFrame(String channelId, int type, int samplesPerChannel, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer buffer, long renderTimeMs, int avsync_type) {
                    return false;
                }

                @Override
                public boolean onEarMonitoringAudioFrame(int type, int samplesPerChannel, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer buffer, long renderTimeMs, int avsync_type) {
                    return false;
                }

                @Override
                public int getObservedAudioFramePosition() {
                    return 0;
                }

                @Override
                public AudioParams getRecordAudioParams() {
                    return null;
                }

                @Override
                public AudioParams getPlaybackAudioParams() {
                    return null;
                }

                @Override
                public AudioParams getMixedAudioParams() {
                    return null;
                }

                @Override
                public AudioParams getEarMonitoringAudioParams() {
                    return null;
                }

                @Override
                public AudioParams getPublishAudioParams() {
                    return null;
                }
            });
        } else {
            mRtcEngine.registerAudioFrameObserver(null);
        }
    }

}
