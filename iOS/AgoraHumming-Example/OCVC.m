//
//  OCVC.m
//  KScoreEngine-Example
//
//  Created by ZYP on 2023/9/11.
//

#import "OCVC.h"
@import AgoraHummingService;

@interface OCVC ()<AgoraHummingServiceDelegate>

@property (nonatomic, strong)AgoraHummingService *service;

@end

@implementation OCVC

- (void)viewDidLoad {
    [super viewDidLoad];
    /** 以下都是伪代码，不能直接运行 **/
    _service = [AgoraHummingService create];
    
    _service.delegate = self;
    
    AgoraHummingServiceConfig *config = [AgoraHummingServiceConfig new];
    config.appId = @"appid";
    config.rtmToken = @"rtmToken";
    config.userId = @"123";
    config.zone = AHServiceZoneAp;
    config.enableLog = YES;
    config.enableSaveLogToFile = YES;
    config.samplesRate = 16000;
    config.bitsPerSample = 2 * 8;
    config.channels = 1;
    [_service initializeWithConfig:config];
    
    [_service pushPcmData:[NSData new]];
}


#pragma mark - AgoraKScoreEngineDelegate

//- (void)kScoreEngineWithEngine:(AgoraKScoreEngine *)engine onScoreResult:(NSInteger)code msg:(NSString *)msg {
//
//}
- (void)agoraHummingService:(nonnull AgoraHummingService *)service
              onEventResult:(AHServiceEvent)event
                serviceCode:(AHServiceCode)code
                        msg:(nonnull NSString *)msg {
    if (event == AHServiceEventInitialize && code == 0) {
        /// 初始化成功
        [_service switchSongWithName:@"songName"];
    }
    if (event == AHServiceEventInitialize && code != 0) {
        /// 初始化失败
    }
}

- (void)agoraHummingService:(nonnull AgoraHummingService *)service
      onScoreResultWithCode:(AHResultCode)code
                      score:(float)score
                        msg:(nonnull NSString *)msg
                   costTime:(NSInteger)costTime {
    if (code == 0) {
        /// 成功，读取分数score
    }
    else {
        /// 失败，继续等待
    }
}

@end
