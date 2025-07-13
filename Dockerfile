FROM openjdk:17-jdk

# 환경 변수 설정
ENV ANDROID_SDK_ROOT /opt/android-sdk
ENV PATH ${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

# 필요한 패키지 설치
RUN apt-get update && \
    apt-get install -y wget unzip git && \
    mkdir -p ${ANDROID_SDK_ROOT}

# Android SDK 다운로드 및 설치
RUN cd ${ANDROID_SDK_ROOT} && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-latest.zip && \
    unzip commandlinetools-linux-latest.zip && \
    rm commandlinetools-linux-latest.zip && \
    mkdir -p cmdline-tools/latest && \
    mv cmdline-tools/bin cmdline-tools/lib cmdline-tools/NOTICE.txt cmdline-tools/source.properties cmdline-tools/latest/ 2>/dev/null || true

# SDK 컴포넌트 설치
RUN yes | ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_SDK_ROOT} \
    "platform-tools" \
    "platforms;android-35" \
    "build-tools;35.0.0" \
    "platforms;android-34" \
    "build-tools;34.0.0"

# 작업 디렉토리 설정
WORKDIR /app

# 기본 명령어
CMD ["./gradlew", "assembleDebug"]