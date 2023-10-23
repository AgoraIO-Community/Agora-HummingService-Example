//
//  QiangChangScoringView.swift
//  Demo
//
//  Created by ZYP on 2023/9/1.
//

import UIKit

protocol MainViewDelegate: NSObjectProtocol {
    func mainViewDidTap(action: MainView.Action)
    func mainViewDidTextFieldRetrun(text: String)
}

class MainView: UIView {
    private let lyricsLabel = UILabel()
    private let textField = UITextField()
    private let scoreLabel = UILabel()
    private let startBtn = UIButton()
    private let qieBtn = UIButton()
    private let endBtn = UIButton()
    private let zoneBtn = UIButton()
    private let indicatorView = UIActivityIndicatorView()
    private let retryBtn = UIButton()
    private let invokeBtn = UIButton()
    
    weak var delegate: MainViewDelegate?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        commonInit()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        backgroundColor = .white
        scoreLabel.textColor = .black
        scoreLabel.numberOfLines = 0
        scoreLabel.textAlignment = .center
        textField.borderStyle = .roundedRect
        textField.clearButtonMode = .always
        textField.returnKeyType = .done
        lyricsLabel.textColor = .black
        lyricsLabel.numberOfLines = 0
        lyricsLabel.textAlignment = .center
        startBtn.setTitle("开始", for: .normal)
        startBtn.backgroundColor = .blue
        startBtn.isHidden = true
        endBtn.setTitle("唱完", for: .normal)
        endBtn.backgroundColor = .blue
        endBtn.isHidden = true
        qieBtn.setTitle("切歌名", for: .normal)
        qieBtn.backgroundColor = .red
        zoneBtn.setTitle("cn", for: .normal)
        zoneBtn.backgroundColor = .red
        retryBtn.setTitle("重试", for: .normal)
        retryBtn.backgroundColor = .red
        invokeBtn.setTitle("invoke", for: .normal)
        invokeBtn.backgroundColor = .red
        retryBtn.isHidden = true
        indicatorView.style = .large
        indicatorView.color = .gray
        
        addSubview(textField)
        addSubview(lyricsLabel)
        addSubview(startBtn)
        addSubview(endBtn)
        addSubview(qieBtn)
        addSubview(scoreLabel)
        addSubview(zoneBtn)
        addSubview(indicatorView)
        addSubview(retryBtn)
        addSubview(invokeBtn)
        
        textField.translatesAutoresizingMaskIntoConstraints = false
        lyricsLabel.translatesAutoresizingMaskIntoConstraints = false
        startBtn.translatesAutoresizingMaskIntoConstraints = false
        endBtn.translatesAutoresizingMaskIntoConstraints = false
        qieBtn.translatesAutoresizingMaskIntoConstraints = false
        scoreLabel.translatesAutoresizingMaskIntoConstraints = false
        zoneBtn.translatesAutoresizingMaskIntoConstraints = false
        retryBtn.translatesAutoresizingMaskIntoConstraints = false
        retryBtn.translatesAutoresizingMaskIntoConstraints = false
        invokeBtn.translatesAutoresizingMaskIntoConstraints = false
        indicatorView.translatesAutoresizingMaskIntoConstraints = false
        
        textField.widthAnchor.constraint(equalToConstant: 200).isActive = true
        textField.heightAnchor.constraint(equalToConstant: 45).isActive = true
        textField.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        textField.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor).isActive = true
        
        scoreLabel.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
        scoreLabel.rightAnchor.constraint(equalTo: rightAnchor).isActive = true
        scoreLabel.topAnchor.constraint(equalTo: textField.bottomAnchor).isActive = true
        
        startBtn.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        startBtn.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
        startBtn.widthAnchor.constraint(equalToConstant: 80).isActive = true
        startBtn.heightAnchor.constraint(equalToConstant: 45).isActive = true
        
        endBtn.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        endBtn.topAnchor.constraint(equalTo: startBtn.bottomAnchor, constant: 20).isActive = true
        endBtn.widthAnchor.constraint(equalToConstant: 80).isActive = true
        endBtn.heightAnchor.constraint(equalToConstant: 45).isActive = true
        
        qieBtn.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
        qieBtn.topAnchor.constraint(equalTo: topAnchor, constant: 200).isActive = true
        qieBtn.widthAnchor.constraint(equalToConstant: 80).isActive = true
        qieBtn.heightAnchor.constraint(equalToConstant: 45).isActive = true
        
        zoneBtn.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
        zoneBtn.topAnchor.constraint(equalTo: qieBtn.bottomAnchor, constant: 5).isActive = true
        zoneBtn.widthAnchor.constraint(equalToConstant: 80).isActive = true
        zoneBtn.heightAnchor.constraint(equalToConstant: 45).isActive = true
        
        lyricsLabel.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
        lyricsLabel.rightAnchor.constraint(equalTo: rightAnchor).isActive = true
        lyricsLabel.bottomAnchor.constraint(equalTo: safeAreaLayoutGuide.bottomAnchor).isActive = true
        
        indicatorView.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        indicatorView.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
        
        retryBtn.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        retryBtn.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
        retryBtn.widthAnchor.constraint(equalToConstant: 80).isActive = true
        retryBtn.heightAnchor.constraint(equalToConstant: 45).isActive = true
        
        invokeBtn.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        invokeBtn.topAnchor.constraint(equalTo: endBtn.bottomAnchor, constant: 20).isActive = true
        invokeBtn.widthAnchor.constraint(equalToConstant: 80).isActive = true
        invokeBtn.heightAnchor.constraint(equalToConstant: 45).isActive = true
    }
    
    private func commonInit() {
        startBtn.addTarget(self, action: #selector(buttonTap(_:)), for: .touchUpInside)
        endBtn.addTarget(self, action: #selector(buttonTap(_:)), for: .touchUpInside)
        qieBtn.addTarget(self, action: #selector(buttonTap(_:)), for: .touchUpInside)
        zoneBtn.addTarget(self, action: #selector(buttonTap(_:)), for: .touchUpInside)
        retryBtn.addTarget(self, action: #selector(buttonTap(_:)), for: .touchUpInside)
        invokeBtn.addTarget(self, action: #selector(buttonTap(_:)), for: .touchUpInside)
        textField.delegate = self
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        endEditing(true)
    }
    
    @objc func buttonTap(_ sender: UIButton) {
        if sender == startBtn {
            startBtn.isHidden = true
            endBtn.isHidden = false
            endBtn.setTitle("唱完", for: .normal)
            delegate?.mainViewDidTap(action: .start)
            return
        }
        if sender == endBtn {
            startBtn.isHidden = false
            endBtn.isHidden = true
            delegate?.mainViewDidTap(action: .end)
            return
        }
        if sender == qieBtn {
            delegate?.mainViewDidTap(action: .qie)
            return
        }
        if sender == zoneBtn {
            delegate?.mainViewDidTap(action: .zone)
            return
        }
        if sender == retryBtn {
            delegate?.mainViewDidTap(action: .retry)
            return
        }
        if sender == invokeBtn {
            delegate?.mainViewDidTap(action: .invoke)
            return
        }
    }
    
    func updateOkTime(num: Int) {
        endBtn.setTitle("唱完\(num)", for: .normal)
    }
    
    func setLyrics(text: String) {
        lyricsLabel.text = text
    }
    
    func setSongName(name: String) {
        textField.text = name
    }
    
    func setScore(string: String, color: UIColor = .black) {
        scoreLabel.text = string
        scoreLabel.textColor = color
    }
    
    func setCompleted() {
        startBtn.isHidden = false
        endBtn.isHidden = true
    }
    
    func setLoading(loading: Bool) {
        if loading {
            startBtn.isHidden = true
            indicatorView.startAnimating()
        }
        else {
            indicatorView.stopAnimating()
        }
    }
    
    func setRetry(enable: Bool) {
        retryBtn.isHidden = !enable
        startBtn.isHidden = enable
    }
    
    func setZone(name: String) {
        zoneBtn.setTitle(name, for: .normal)
    }
    
    func showStartBtn() {
        startBtn.isHidden = false
    }
}

extension MainView: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        delegate?.mainViewDidTextFieldRetrun(text: textField.text ?? "")
        textField.resignFirstResponder()
        return true
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let currentText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string) ?? ""
        delegate?.mainViewDidTextFieldRetrun(text: currentText)
        return true
    }
}

extension MainView {
    enum Action {
        case start
        case end
        case qie
        case zone
        case retry
        case invoke
    }
}
