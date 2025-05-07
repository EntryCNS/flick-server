cd ..

./gradlew :api-gateway:clean :api-gateway:bootJar

cd api-gateway

docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t jbj338033/flick-api-gateway:latest \
  --push \
  .