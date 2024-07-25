gomobile bind -v -target="android/arm,android/arm64,android/amd64" -androidapi=21
xcopy /y /f "./tts_server_lib.aar" "../app/libs/tts_server_lib.aar"
pause