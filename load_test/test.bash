#!/bin/bash

# 기본 설정
THREADS=10
CONNECTIONS=10
DURATION=1 #요청 기간(초)
RATE=100 #초당 요청 전송량
SCRIPT="post.lua"
URL="https://stunning-capybara-4p76v6x55q6fj64x-8080.app.github.dev/api/store"

# 실행
wrk -t$THREADS -c$CONNECTIONS -d$DURATION -R$RATE -s $SCRIPT $URL
