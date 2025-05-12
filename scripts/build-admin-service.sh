cd ..

./gradlew :admin-service:clean :admin-service:bootJar

cd admin-service

docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t jbj338033/flick-admin-service:latest \
  --push \
  .