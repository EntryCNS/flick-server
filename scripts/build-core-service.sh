cd ..

./gradlew :core-service:clean :core-service:bootJar

cd core-service

docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t jbj338033/flick-core-service:latest \
  --push \
  .