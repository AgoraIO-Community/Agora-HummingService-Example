//
//  RTCManager.swift
//  Demo
//
//  Created by ZYP on 2023/7/3.
//

import AgoraRtcKit
import RTMTokenBuilder

protocol RTCManagerDelegate: NSObjectProtocol {
    func RTCManagerDidUpdateAudioFrame(frame: AgoraAudioFrame)
}

class RTCManager: NSObject {
    var agoraKit: AgoraRtcEngineKit!
    var mediaPlayer: AgoraRtcMediaPlayerProtocol!
    weak var delegate: RTCManagerDelegate?
    var openCompleted = false
    var isRecord = false
    var sampleRate = 16000
    var channels = 1
    
    func initEngine() {
        let config = AgoraRtcEngineConfig()
        config.appId = Config.appId
        config.audioScenario = .chorus
        config.channelProfile = .liveBroadcasting
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: self)
        agoraKit.setAudioFrameDelegate(self)
    }
    
    deinit {
        print("[RTCManager] deinit")
    }
    
    func joinChannel() {
        let option = AgoraRtcChannelMediaOptions()
        option.clientRoleType = .broadcaster
        agoraKit.enableAudio()
        agoraKit.enableLocalAudio(false)
        agoraKit.setClientRole(.broadcaster)
        let ret = agoraKit.joinChannel(byToken: nil,
                                       channelId: Config.channelId,
                                       uid: Config.uid,
                                       mediaOptions: option)
        print("[RTCManager] joinChannel ret \(ret)")
    }
    
    func destory() {
        agoraKit.leaveChannel()
        agoraKit.disableAudio()
    }
    
    public func enableMic(enable: Bool) {
        agoraKit.enableLocalAudio(enable)
        if enable {
            startRecord()
        }
        else {
            stopRecord()
        }
    }
    
    private func startRecord() {
        isRecord = true
    }
    
    private func stopRecord() {
        isRecord = false
    }
}

extension RTCManager: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        print("[RTCManager] didOccurError \(errorCode)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        print("[RTCManager] didJoinedOfUid \(uid)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        print("[RTCManager] didJoinChannel withUid \(uid)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {}
}

// MARK: - AudioFrameDelegate

extension RTCManager: AgoraAudioFrameDelegate {
    func getRecordAudioParams() -> AgoraAudioParams {
        let params = AgoraAudioParams ()
        params.sampleRate = sampleRate
        params.channel = channels
        params.mode = .readOnly
        params.samplesPerCall = 480
        return params
    }
    
    func onRecordAudioFrame(_ frame: AgoraAudioFrame, channelId: String) -> Bool {
        if isRecord {
            invokeRTCManagerDidUpdateAudioFrame(frame: frame)
        }
        return true
    }
    
    func getObservedAudioFramePosition() -> AgoraAudioFramePosition {
        return .record
    }
}

extension RTCManager {
    func invokeRTCManagerDidUpdateAudioFrame(frame: AgoraAudioFrame) {
        if Thread.isMainThread {
            delegate?.RTCManagerDidUpdateAudioFrame(frame: frame)
            return
        }
        
        DispatchQueue.main.async { [weak self] in
            self?.delegate?.RTCManagerDidUpdateAudioFrame(frame: frame)
        }
    }
}
