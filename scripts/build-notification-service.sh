cd ..

./gradlew :notification-service:clean :notification-service:bootJar

cd notification-service

docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t jbj338033/flick-notification-service:latest \
  --push \
  .