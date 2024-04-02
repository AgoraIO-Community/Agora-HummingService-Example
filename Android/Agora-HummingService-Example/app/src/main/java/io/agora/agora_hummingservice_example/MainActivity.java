package io.agora.agora_hummingservice_example;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.agora.humming.HummingService;
import io.agora.humming.HummingServiceCallback;
import io.agora.humming.HummingServiceConfig;
import io.agora.humming.ResultCode;
import io.agora.humming.ServiceCode;
import io.agora.humming.ServiceEvent;
import io.agora.humming.ServiceZone;
import io.agora.humming_sdk_example.BuildConfig;
import io.agora.humming_sdk_example.R;
import io.agora.humming_sdk_example.databinding.MainActivityBinding;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.IAudioFrameObserver;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.audio.AudioParams;


public class MainActivity extends Activity {
    private final String TAG = "AgoraHummingService" + MainActivity.class.getSimpleName();
    private MainActivityBinding binding;

    private ExecutorService mExecutorCacheService;
    private ExecutorService mExecutorService;

    private HummingService mHummingService;
    private HummingServiceConfig mHummingServiceConfig;
    private RtcEngine mRtcEngine;

    private int mInitCount = 0;
    private ServiceZone mServiceZone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        initView();

        initHumming();
        initRtc();
    }

    private void initData() {
        mExecutorCacheService = new ThreadPoolExecutor(Integer.MAX_VALUE, Integer.MAX_VALUE,
                0, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        mExecutorService = new ThreadPoolExecutor(1, 1,
                0, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

        mServiceZone = ServiceZone.getCode(2);
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


    private void initHumming() {
        mHummingService = HummingService.create();
        mHummingServiceConfig = new HummingServiceConfig() {
            {
                this.context = getApplicationContext();
                this.appId = KeyCenter.APP_ID;
                this.rtmToken = KeyCenter.getRtmToken2(KeyCenter.getUserUid());
//                this.rtmToken = KeyCenter.getRtmToken(KeyCenter.getUserUid());
                this.userId = String.valueOf(KeyCenter.getUserUid());
                this.serviceZone = mServiceZone;
                this.enableLog = true;
                this.enableSaveLogToFile = true;
                this.samplesRate = 16000;
                this.channels = 1;
                this.bitsPerSample = 16;
//                this.enableTimer = false;
                this.callback = new HummingServiceCallback() {
                    @Override
                    public void onScoreResult(ResultCode code, String msg, float score, long costTime) {
                        if (code == ResultCode.SUCCESS) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "你太棒了", Toast.LENGTH_SHORT).show();
                                    binding.tvResult.setText(binding.tvResult.getText() + String.format(Locale.getDefault(), "score:%3.2f  coastTime:%dms\n",
                                            score, costTime));
                                }
                            });
                        }
                    }

                    @Override
                    public void onEventResult(@NonNull ServiceEvent event, @NonNull ServiceCode code, @NonNull String msg) {
                        if (ServiceEvent.INITIALIZE == event) {
                            if (ServiceCode.ERROR_INITIALIZE_FAIL == code) {
                                initialize();
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "init失败：" + msg, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else if (ServiceCode.SUCCESS == code) {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        initSuccessUI();
                                    }
                                });
                            }
                        }
                    }
                };
            }
        };

        initialize();
    }

    private void initialize() {
        mInitCount++;
        if (mInitCount <= 10) {
            mHummingService.initialize(mHummingServiceConfig);
        }
    }

    private void initSuccessUI() {
        binding.btnStartSpeak.setEnabled(true);
        binding.btnStopSpeak.setEnabled(false);
        binding.btnSetSongName.setEnabled(true);

        mHummingService.switchSong("大花轿");
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
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.btnStopSpeak.setEnabled(true);
                    }
                });
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
            mRtcEngine.setChannelProfile(io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

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
        binding.btnStartSpeak.setEnabled(false);
        binding.btnStopSpeak.setEnabled(false);
        binding.btnSetSongName.setEnabled(false);

        binding.version.setText(BuildConfig.VERSION_NAME);

        binding.btnStartSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinChannel();
                binding.btnStartSpeak.setEnabled(false);
                binding.btnStopSpeak.setEnabled(false);
                binding.btnSetSongName.setEnabled(false);
                //updateRoleSpeak(true);
            }
        });
        binding.btnStopSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRoleSpeak(false);
                binding.btnStartSpeak.setEnabled(true);
                binding.btnStopSpeak.setEnabled(false);
                binding.btnSetSongName.setEnabled(true);
                mRtcEngine.leaveChannel();
            }
        });

        binding.btnSetSongName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(binding.inputSongName.getText().toString())) {
                    Toast.makeText(MainActivity.this, "请输入歌曲名", Toast.LENGTH_SHORT).show();
                    return;
                }
                binding.btnStartSpeak.setEnabled(true);
                binding.btnStopSpeak.setEnabled(false);
                mHummingService.switchSong(binding.inputSongName.getText().toString());
                Toast.makeText(MainActivity.this, "歌曲设置成功", Toast.LENGTH_SHORT).show();
            }
        });

        final String[] regions = getResources().getStringArray(R.array.region);
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, regions);
        binding.regionSpinner.setAdapter(regionAdapter);
        binding.regionSpinner.setSelection(2);
        binding.regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "set region: " + regions[position]);
                mServiceZone = ServiceZone.getCode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.btnSetRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRoleSpeak(false);
                mRtcEngine.leaveChannel();
                mInitCount = 0;
                initHumming();
                Toast.makeText(MainActivity.this, "设置服务区域成功", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret = mHummingService.invoke();
                Log.i(TAG, "invoke: " + ret);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        HummingService.destroy();

        RtcEngine.destroy();
    }

    private void registerAudioFrame(boolean enable) {
        if (enable) {
            mRtcEngine.registerAudioFrameObserver(new IAudioFrameObserver() {

                @Override
                public boolean onRecordAudioFrame(String channelId, int type, int samplesPerChannel, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer buffer, long renderTimeMs, int avsync_type) {
                    int length = buffer.remaining();
                    byte[] origin = new byte[length];
                    buffer.get(origin);
                    buffer.flip();

                    mHummingService.pushPcmData(origin);

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
                public boolean onPlaybackAudioFrameBeforeMixing(String channelId, int userId, int type, int samplesPerChannel, int bytesPerSample, int channels, int samplesPerSec, ByteBuffer buffer, long renderTimeMs, int avsync_type, int rtpTimestamp) {
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
            });
        } else {
            mRtcEngine.registerAudioFrameObserver(null);
        }
    }

}
