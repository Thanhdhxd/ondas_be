# ══════════════════════════════════════════════════════════
#  Stage 1: Build
#  Dùng JDK đầy đủ để compile và đóng gói JAR
# ══════════════════════════════════════════════════════════
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper trước để tận dụng Docker layer cache:
# Nếu pom.xml không đổi → layer dependencies được cache lại,
# không phải download lại mỗi lần build.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Đảm bảo mvnw có quyền thực thi (cần thiết khi build trên Windows)
RUN chmod +x mvnw

# Download dependencies (layer này được cache nếu pom.xml không đổi)
RUN ./mvnw dependency:go-offline -B

# Copy toàn bộ source code và build JAR
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ══════════════════════════════════════════════════════════
#  Stage 2: Runtime
#  Chỉ dùng JRE (nhỏ hơn ~200MB so với JDK)
# ══════════════════════════════════════════════════════════
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Tạo user riêng để chạy app — không chạy bằng root
RUN addgroup -S ondas && adduser -S ondas -G ondas

# Copy JAR từ stage build
COPY --from=builder /app/target/*.jar app.jar

# Giới hạn RAM heap cho JVM
# (Jenkins ~512m + PostgreSQL ~200m + OS ~200m → còn ~1.1GB cho app)
ENV JAVA_OPTS="-Xmx384m -Xms256m -XX:+UseContainerSupport"

USER ondas

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
