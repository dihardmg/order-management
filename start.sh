#!/bin/bash

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${BLUE}======================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}======================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Check if Docker and Docker Compose are installed
check_prerequisites() {
    print_header "Checking Prerequisites"

    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    print_success "Docker is installed"

    if ! command -v docker-compose &> /dev/null; then
        # Try docker compose (v2)
        if ! docker compose version &> /dev/null; then
            print_error "Docker Compose is not installed. Please install Docker Compose first."
            exit 1
        fi
        DOCKER_COMPOSE="docker compose"
    else
        DOCKER_COMPOSE="docker-compose"
    fi
    print_success "Docker Compose is installed"
}

# Build and start all services
start_services() {
    print_header "Building and Starting All Services"

    echo -e "${YELLOW}This will take several minutes on first run...${NC}"
    echo ""

    # Build and start services
    $DOCKER_COMPOSE up -d --build

    if [ $? -eq 0 ]; then
        print_success "All services started successfully!"
        echo ""
        show_urls
    else
        print_error "Failed to start services. Check logs with: $DOCKER_COMPOSE logs"
        exit 1
    fi
}

# Stop all services
stop_services() {
    print_header "Stopping All Services"
    $DOCKER_COMPOSE down
    print_success "All services stopped"
}

# Stop and remove all data
stop_with_cleanup() {
    print_header "Stopping Services and Removing Data"
    read -p "$(echo -e ${YELLOW}Are you sure? This will delete all data! [y/N]: ${NC})" -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        $DOCKER_COMPOSE down -v
        print_success "All services stopped and data removed"
    else
        print_warning "Cleanup cancelled"
    fi
}

# Show service URLs
show_urls() {
    print_header "Service URLs"

    echo -e "${GREEN}Management UIs:${NC}"
    echo "  • Consul:          http://localhost:8500/ui"
    echo "  • pgAdmin 4:       http://localhost:5050"
    echo ""
    echo -e "${GREEN}Microservices:${NC}"
    echo "  • API Gateway:     http://localhost:8080"
    echo "  • Order Service:   http://localhost:8081"
    echo "  • Product Service: http://localhost:8082"
    echo "  • Customer Service http://localhost:8083"
    echo ""
    echo -e "${GREEN}Default Credentials:${NC}"
    echo "  • PostgreSQL:      admin/admin123"
    echo "  • pgAdmin 4:       admin@admin.com / admin123"
    echo ""
}

# Show service status
show_status() {
    print_header "Service Status"
    $DOCKER_COMPOSE ps
}

# Show logs
show_logs() {
    if [ -z "$1" ]; then
        print_header "All Service Logs"
        $DOCKER_COMPOSE logs -f
    else
        print_header "Logs for $1"
        $DOCKER_COMPOSE logs -f "$1"
    fi
}

# Restart services
restart_services() {
    print_header "Restarting All Services"
    $DOCKER_COMPOSE restart
    print_success "All services restarted"
}

# Health check
health_check() {
    print_header "Health Check"

    echo "Checking service health..."
    echo ""

    # Consul
    if curl -s http://localhost:8500/v1/status/leader > /dev/null; then
        print_success "Consul is healthy"
    else
        print_error "Consul is not responding"
    fi

    # PostgreSQL
    if docker exec postgres pg_isready -U admin > /dev/null 2>&1; then
        print_success "PostgreSQL is healthy"
    else
        print_error "PostgreSQL is not responding"
    fi

    # Services
    for port in 8081 8082 8083 8080; do
        if curl -s http://localhost:$port/actuator/health > /dev/null 2>&1; then
            service_name=$(docker ps --format '{{.Names}}' --filter "publish=$port")
            print_success "$service_name is healthy"
        else
            print_error "Service on port $port is not responding"
        fi
    done
}

# Show menu
show_menu() {
    clear
    print_header "Order Management Microservices"
    echo ""
    echo "1) Start all services"
    echo "2) Stop all services"
    echo "3) Stop with cleanup (remove data)"
    echo "4) Show service status"
    echo "5) Show service URLs"
    echo "6) Show logs (all services)"
    echo "7) Show logs (specific service)"
    echo "8) Restart services"
    echo "9) Health check"
    echo "0) Exit"
    echo ""
    read -p "Select an option: " choice

    case $choice in
        1) check_prerequisites; start_services; read -p "Press Enter to continue...";;
        2) stop_services; read -p "Press Enter to continue...";;
        3) stop_with_cleanup; read -p "Press Enter to continue...";;
        4) show_status; read -p "Press Enter to continue...";;
        5) show_urls; read -p "Press Enter to continue...";;
        6) show_logs;;
        7) read -p "Enter service name: " service_name; show_logs "$service_name";;
        8) restart_services; read -p "Press Enter to continue...";;
        9) health_check; read -p "Press Enter to continue...";;
        0) echo "Goodbye!"; exit 0;;
        *) print_error "Invalid option"; sleep 2;;
    esac
}

# Main script
if [ "$1" == "start" ]; then
    check_prerequisites
    start_services
elif [ "$1" == "stop" ]; then
    stop_services
elif [ "$1" == "cleanup" ]; then
    stop_with_cleanup
elif [ "$1" == "status" ]; then
    show_status
elif [ "$1" == "logs" ]; then
    if [ -z "$2" ]; then
        show_logs
    else
        show_logs "$2"
    fi
elif [ "$1" == "urls" ]; then
    show_urls
elif [ "$1" == "health" ]; then
    health_check
else
    # Show interactive menu
    while true; do
        show_menu
    done
fi
