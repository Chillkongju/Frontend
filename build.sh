#!/bin/bash
echo "Docker로 Android 앱 빌드 시작..."
docker-compose up --build
echo "빌드 완료! app/build/outputs/apk/debug/ 폴더를 확인하세요."