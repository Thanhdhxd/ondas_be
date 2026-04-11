pipeline {
    agent any

    environment {
        // ── Docker Hub ─────────────────────────────────────────
        DOCKERHUB_USERNAME = 'dinhtrieuxtnd'   // ← Thay username của bạn
        IMAGE_NAME         = "${DOCKERHUB_USERNAME}/ondas-be"
        IMAGE_TAG          = "${BUILD_NUMBER}"

        // ── Deploy dir trên VPS host (mounted vào Jenkins container) ──
        DEPLOY_DIR         = '/opt/ondas-app'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timeout(time: 20, unit: 'MINUTES')
        timestamps()
    }

    stages {

        // ── Stage 1: Checkout ──────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo "Checkout hoàn tất - Branch: ${env.GIT_BRANCH}"
            }
        }

        // ── Stage 2: Build với Maven ──────────────────────────
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests -B --no-transfer-progress'
                echo 'Build JAR thành công'
            }
        }

        // ── Stage 3: Unit Tests ────────────────────────────────
        stage('Test') {
            steps {
                sh 'mvn test -B --no-transfer-progress'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        // ── Stage 4: Build Docker Image ────────────────────────
        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
                echo "Docker image: ${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }

        // ── Stage 5: Push lên Docker Hub ──────────────────────
        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_TOKEN'
                )]) {
                    sh "echo '${DOCKER_TOKEN}' | docker login -u '${DOCKER_USER}' --password-stdin"
                    sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                    sh "docker push ${IMAGE_NAME}:latest"
                    sh "docker logout"
                    echo "Đã push image lên Docker Hub"
                }
            }
        }

        // ── Stage 6: Deploy (cùng VPS, không cần SSH) ─────────
        stage('Deploy') {
            steps {
                // Dùng Secret File credential: toàn bộ .env được lưu 1 file trong Jenkins
                // → copy thẳng vào deploy dir, không lộ từng biến trong logs
                withCredentials([
                    file(credentialsId: 'prod-env-file', variable: 'ENV_FILE')
                ]) {
                    // Bước 1: Copy file .env vào thư mục app (mounted từ VPS host)
                    sh """
                        cp \${ENV_FILE} ${DEPLOY_DIR}/.env
                        chmod 600 ${DEPLOY_DIR}/.env
                    """

                    // Bước 2: Copy docker-compose.prod.yml vào thư mục deploy
                    sh "cp docker-compose.prod.yml ${DEPLOY_DIR}/docker-compose.prod.yml"

                    // Bước 3: Pull image mới và restart (dùng host Docker qua socket)
                    sh """
                        docker compose -f ${DEPLOY_DIR}/docker-compose.prod.yml \
                            --env-file ${DEPLOY_DIR}/.env \
                            pull ondas-be

                        docker compose -f ${DEPLOY_DIR}/docker-compose.prod.yml \
                            --env-file ${DEPLOY_DIR}/.env \
                            up -d --force-recreate ondas-be

                        docker image prune -f
                    """

                    echo "Deploy thành công!"
                    echo "App: http://103.245.237.251:8080"
                }
            }
        }
    }

    post {
        success {
            echo """
            ╔══════════════════════════════════════╗
            ║  BUILD & DEPLOY THÀNH CÔNG!         ║
            ║  App:     http://103.245.237.251:8080 ║
            ║  Jenkins: http://103.245.237.251:9090 ║
            ╚══════════════════════════════════════╝
            """
        }
        failure {
            echo 'Pipeline thất bại. Xem Console Output để biết chi tiết.'
        }
        always {
            sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
            cleanWs()
        }
    }
}
