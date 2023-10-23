//
//  ViewController.swift
//  KScoreEngine-Example
//
//  Created by ZYP on 2023/9/11.
//

import UIKit
import AgoraHummingService
import AgoraRtcKit
import RTMTokenBuilder

class MainViewController: UIViewController {
    private let mainView = MainView()
    private let rtcManager = RTCManager()
    private var service: AgoraHummingService!
    private let config = AgoraHummingServiceConfig()
    let songNames = ["十年", "明月几时有"]
    var songName = ""
    var currentIndex = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        handleNextSongName()
        mainView.setLoading(loading: true)
        /** service init **/
        config.setZone(.cn)
        serviceInit()
        
        /** rtc init **/
        rtcManager.channels = config.channels
        rtcManager.sampleRate = config.samplesRate
        rtcManager.delegate = self
        rtcManager.initEngine()
        rtcManager.joinChannel()
    }
    
    override func didMove(toParent parent: UIViewController?) {
        if parent == nil {
            rtcManager.destory()
        }
    }
    
    deinit {
        AgoraHummingService.destory()
        log(text: "MainViewController deinit")
    }
    
    private func setupUI() {
        view.addSubview(mainView)
        mainView.frame = view.bounds
        mainView.delegate = self
    }
    
    private func serviceInit() {
        service = AgoraHummingService.create()
        service.delegate = self
        let humToken = TokenBuilder.buildToken2(Config.appId, appCertificate: Config.cer, userUuid: "\(Config.uid)")
        config.appId = Config.appId
        config.rtmToken = humToken
        config.enableLog = true
        config.enableSaveLogToFile = true
        config.userId = "\(Config.uid)"
        config.bitsPerSample = 2 * 8
        config.channels = 1
        config.samplesRate = 16000
        config.timerIntervalInMs = 1000
//        config.enableTimer = false
        service.initialize(with: config)
    }
    
    fileprivate func handleStart() {
        title = songName
        
        mainView.setScore(string: "--")
        service.switchSong(withName: songName)
        rtcManager.enableMic(enable: true)
    }
    
    fileprivate func handleEnd() {
        mainView.setScore(string: "--")
        rtcManager.enableMic(enable: false)
    }
    
    /// 切歌
    fileprivate func handleNextSongName() {
        songName = songNames[currentIndex]
        title = songName
        mainView.setSongName(name: songName)
        if currentIndex == songNames.count - 1 {
            currentIndex = 0
        }
        else {
            currentIndex += 1
        }
    }
    
    fileprivate func updateZone(zone: AHServiceZone) {
        if zone == config.zone() {
            return
        }
        config.setZone(zone)
        mainView.setLoading(loading: true)
        
        serviceInit()
    }
    
    fileprivate func showZoenSheet() {
        let vc = UIAlertController()
        let a1 = UIAlertAction(title: "cn", style: .default) { [weak self](_) in
            self?.updateZone(zone: .cn)
            self?.mainView.setZone(name: "cn")
        }
        let a2 = UIAlertAction(title: "eu", style: .default) { [weak self](_) in
            self?.updateZone(zone: .eu)
            self?.mainView.setZone(name: "eu")
        }
        let a3 = UIAlertAction(title: "ap", style: .default) { [weak self](_) in
            self?.updateZone(zone: .ap)
            self?.mainView.setZone(name: "ap")
        }
        let a4 = UIAlertAction(title: "取消", style: .cancel);
        vc.addAction(a1)
        vc.addAction(a2)
        vc.addAction(a3)
        vc.addAction(a4)
        present(vc, animated: true)
    }
    
    private func parse(pitchFileString: String) -> [Double] {
        if pitchFileString.contains("\r\n") {
            let array = pitchFileString.split(separator: "\r\n").map({ Double($0)! })
            return array
        }
        else {
            let array = pitchFileString.split(separator: "\n").map({ Double($0)! })
            return array
        }
    }
    
    private func log(text: String) {
        print("[MainView \(text)]")
    }
    
}

extension MainViewController: MainViewDelegate {
    func mainViewDidTextFieldRetrun(text: String) {
        songName = text
    }
    
    func mainViewDidTap(action: MainView.Action) {
        switch action {
        case .start:
            handleStart()
            break
        case .end:
            handleEnd()
            break
        case .qie:
            handleNextSongName()
            break
        case .zone:
            showZoenSheet()
            break
        case .retry:
            serviceInit()
            break
        case .invoke:
            service.invoke()
            break
        }
    }
}

extension MainViewController: RTCManagerDelegate {
    func RTCManagerDidUpdateAudioFrame(frame: AgoraAudioFrame) {
        guard let buffer = frame.buffer else {
            return
        }
        let count = frame.channels * frame.bytesPerSample * frame.samplesPerChannel
        let data = Data(bytes: buffer, count: count)
        service.pushPcmData(data)
    }
}

extension MainViewController: AgoraHummingServiceDelegate {
    func agoraHummingService(_ service: AgoraHummingService,
                             onScoreResultWithCode code: AHResultCode,
                             score: Float,
                             msg: String,
                             costTime: Int) {
        let success = code == 0
        if success {
            handleEnd()
        }
        
        let string = "\(success ? "ok:\(score)" : "ing...")"
        mainView.setScore(string: string)
        if success {
            rtcManager.enableMic(enable: false)
            mainView.setCompleted()
        }
    }
    
    func agoraHummingService(_ service: AgoraHummingService,
                             onEventResult event: AHServiceEvent,
                             serviceCode code: AHServiceCode,
                             msg: String) {
        print("onEventResult code:\(code) msg:\(msg)")
        if event == .initialize, code == 0 {
            mainView.setLoading(loading: false)
            mainView.setRetry(enable: false)
            mainView.showStartBtn()
        }
        if event == .initialize, code != 0 {
            mainView.setLoading(loading: false)
            mainView.setRetry(enable: true)
        }
    }
}

