cd ..

./gradlew :place-service:clean :place-service:bootJar

cd place-service

docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t jbj338033/flick-place-service:latest \
  --push \
  .