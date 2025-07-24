# JDK 21 슬림 이미지 사용
FROM eclipse-temurin:21-jdk-alpine

# jar 파일을 컨테이너에 복사
COPY target/jeommechu-0.0.1-SNAPSHOT.jar app.jar


# 컨테이너에서 열 포트
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
