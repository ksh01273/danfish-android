# DanFish 릴리스 키스토어

## 키스토어 생성 명령어

```bash
keytool -genkey -v \
  -keystore danfish-release.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias danfish
```

## 환경변수 설정

```
KEYSTORE_PASSWORD=<설정한 비밀번호>
KEY_PASSWORD=<설정한 비밀번호>
```

> ⚠️ .jks 파일은 절대 Git에 커밋하지 마세요!