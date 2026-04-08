#!/bin/bash
# ========================================
# 🚀 Deploy to VPS Bisnet Script
# ========================================
# Usage: ./deploy.sh [start|stop|restart|status|logs]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
COMPOSE_FILE="docker-compose-prod.yml"
PROJECT_NAME="order-management"
LOG_FILE="deploy.log"

# Functions
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  Order Management - VPS Deployment Script${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
}

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

check_system_requirements() {
    log_info "Checking system requirements..."

    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi

    # Check available memory
    local total_mem=$(free -m | awk 'NR==2{printf "%.0f", $2}')
    local available_mem=$(free -m | awk 'NR==2{printf "%.0f", $7}')

    log_info "Total RAM: ${total_mem} MB"
    log_info "Available RAM: ${available_mem} MB"

    if [ "$total_mem" -lt 3500 ]; then
        log_warning "System has less than 3.5GB RAM. Deployment may fail."
    fi

    # Check disk space
    local available_disk=$(df -h . | awk 'NR==2{print $4}')
    log_info "Available disk space: $available_disk"

    log_info "System requirements check completed."
}

deploy_start() {
    print_header
    log_info "Starting deployment..."

    check_system_requirements

    # Stop existing containers if any
    log_info "Stopping existing containers..."
    docker-compose -f "$COMPOSE_FILE" down 2>&1 | tee -a "$LOG_FILE" || true

    # Pull latest images (if using pre-built images)
    # docker-compose -f "$COMPOSE_FILE" pull 2>&1 | tee -a "$LOG_FILE"

    # Build and start services
    log_info "Building and starting services..."
    docker-compose -f "$COMPOSE_FILE" up -d --build 2>&1 | tee -a "$LOG_FILE"

    # Wait for services to be healthy
    log_info "Waiting for services to be healthy..."
    sleep 30

    # Check service status
    check_services_health

    log_info "Deployment completed successfully!"
    show_access_info
}

deploy_stop() {
    print_header
    log_info "Stopping all services..."

    docker-compose -f "$COMPOSE_FILE" down 2>&1 | tee -a "$LOG_FILE"

    log_info "All services stopped."
}

deploy_restart() {
    print_header
    log_info "Restarting all services..."

    deploy_stop
    sleep 5
    deploy_start
}

deploy_status() {
    print_header
    log_info "Checking service status..."

    docker-compose -f "$COMPOSE_FILE" ps 2>&1 | tee -a "$LOG_FILE"
}

deploy_logs() {
    print_header
    log_info "Showing logs from all services..."

    docker-compose -f "$COMPOSE_FILE" logs --tail=100 -f 2>&1 | tee -a "$LOG_FILE"
}

check_services_health() {
    log_info "Checking service health..."

    local services=("consul:8500" "postgres:5432" "api-gateway:8080" "order-service:8081" "product-service:8082" "customer-service:8083")
    local unhealthy=0

    for service_info in "${services[@]}"; do
        IFS=':' read -r name port <<< "$service_info"

        if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            log_info "✓ $name is healthy"
        else
            log_error "✗ $name is unhealthy or not ready"
            ((unhealthy++))
        fi
    done

    if [ $unhealthy -gt 0 ]; then
        log_warning "$unhealthy service(s) are not healthy yet. They may still be starting up."
        log_info "Check logs: docker-compose -f $COMPOSE_FILE logs"
    else
        log_info "All services are healthy! ✅"
    fi
}

show_access_info() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  🌐 Access Information${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    echo -e "${GREEN}API Gateway (Public):${NC}"
    echo "  URL: http://<YOUR-VPS-IP>:8080"
    echo "  Swagger UI: http://<YOUR-VPS-IP>:8080/swagger-ui.html"
    echo ""
    echo -e "${YELLOW}Services (Internal - Via Gateway):${NC}"
    echo "  Order Service:  /api/orders"
    echo "  Product Service: /api/products"
    echo "  Customer Service: /api/customers"
    echo ""
    echo -e "${GREEN}Admin Endpoints:${NC}"
    echo "  Consul UI: http://<YOUR-VPS-IP>:8500/ui"
    echo "  Health Check: http://<YOUR-VPS-IP>:8080/actuator/health"
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo ""

    log_info "IMPORTANT: Replace <YOUR-VPS-IP> with your actual VPS IP address"
}

setup_swap() {
    print_header
    log_info "Setting up swap space (2 GB)..."

    # Create 2 GB swap file
    if [ ! -f /swapfile ]; then
        sudo fallocate -l 2G /swapfile
        sudo chmod 600 /swapfile
        sudo mkswap /swapfile
        sudo swapon /swapfile
        echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

        log_info "Swap space created and enabled (2 GB)"
    else
        log_warning "Swap file already exists. Skipping..."
    fi
}

# Main script logic
case "${1:-start}" in
    start)
        deploy_start
        ;;
    stop)
        deploy_stop
        ;;
    restart)
        deploy_restart
        ;;
    status)
        deploy_status
        ;;
    logs)
        deploy_logs
        ;;
    swap)
        setup_swap
        ;;
    health)
        check_services_health
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|logs|swap|health}"
        echo ""
        echo "Commands:"
        echo "  start   - Deploy and start all services"
        echo "  stop    - Stop all services"
        echo "  restart - Restart all services"
        echo "  status  - Show service status"
        echo "  logs    - Show logs from all services"
        echo "  swap    - Setup 2 GB swap space"
        echo "  health  - Check all service health"
        exit 1
        ;;
esac

log_info "Script execution completed."
