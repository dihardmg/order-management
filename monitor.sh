#!/bin/bash
# ========================================
# 📊 Monitoring Script for VPS Deployment
# ========================================
# Usage: ./monitor.sh {resources|services|logs|all}

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

COMPOSE_FILE="docker-compose-prod.yml"

print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  VPS Resource Monitoring Script${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
}

# Monitor System Resources
monitor_resources() {
    print_header
    echo -e "${GREEN}System Resources Overview${NC}"
    echo ""

    # RAM Usage
    echo -e "${YELLOW}💾 RAM Usage:${NC}"
    free -h

    # Calculate usage percentage
    local total_mem=$(free -m | awk 'NR==2{print $2}')
    local used_mem=$(free -m | awk 'NR==2{print $3}')
    local usage_percent=$((used_mem * 100 / total_mem))

    echo -e "  Usage: ${usage_percent}%"
    echo ""

    if [ $usage_percent -gt 90 ]; then
        echo -e "${RED}⚠️  WARNING: High RAM usage (>90%)${NC}"
    elif [ $usage_percent -gt 80 ]; then
        echo -e "${YELLOW}⚠️  NOTICE: High RAM usage (>80%)${NC}"
    else
        echo -e "${GREEN}✓ RAM usage is acceptable${NC}"
    fi
    echo ""

    # Swap Usage
    echo -e "${YELLOW}💿 Swap Usage:${NC}"
    free -h | grep -A2 "Swap"
    echo ""

    # CPU Usage
    echo -e "${YELLOW}🖥️ CPU Usage:${NC}"
    top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%id/\1%/" | awk '{print "  CPU Usage: " $1"%'}'
    echo ""

    # Disk Usage
    echo -e "${YELLOW}💽 Disk Usage:${NC}"
    df -h /
    echo ""

    # Docker Resource Usage
    echo -e "${YELLOW}🐳 Docker Container Resources:${NC}"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}\n"
}

# Monitor Docker Services
monitor_services() {
    print_header
    echo -e "${GREEN}Docker Services Status${NC}"
    echo ""

    docker-compose -f "$COMPOSE_FILE" ps
    echo ""

    echo -e "${GREEN}Service Health Check:${NC}"
    echo ""

    # Check each service health
    local services=(
        "consul:8500:Consul"
        "postgres:5432:PostgreSQL"
        "api-gateway:8080:API Gateway"
        "order-service:8081:Order Service"
        "product-service:8082:Product Service"
        "customer-service:8083:Customer Service"
    )

    local healthy=0
    local unhealthy=0

    for service_info in "${services[@]}"; do
        IFS=':' read -r name port display_name <<< "$service_info"

        if curl -sf "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo -e "${GREEN}✓${NC} $display_name is ${GREEN}healthy${NC} (port $port)"
            ((healthy++))
        else
            echo -e "${RED}✗${NC} $display_name is ${RED}unhealthy${NC} or not responding (port $port)"
            ((unhealthy++))
        fi
    done

    echo ""
    echo -e "Healthy Services: ${GREEN}$healthy${NC}/6"
    echo -e "Unhealthy Services: ${RED}$unhealthy${NC}/6"
}

# Show Service Logs
show_logs() {
    print_header
    echo -e "${GREEN}Recent Service Logs (Last 50 Lines)${NC}"
    echo ""

    # Show logs from all services
    docker-compose -f "$COMPOSE_FILE" logs --tail=50
}

# Generate Deployment Report
generate_report() {
    local report_file="deployment-report-$(date +%Y%m%d-%H%M%S).txt"

    print_header
    echo -e "${GREEN}Generating deployment report...${NC}"

    {
        echo "=========================================="
        echo "ORDER MANAGEMENT - DEPLOYMENT REPORT"
        echo "=========================================="
        echo ""
        echo "Generated: $(date)"
        echo ""

        echo "=== SYSTEM RESOURCES ==="
        echo "RAM Usage:"
        free -h
        echo ""
        echo "Swap Usage:"
        free -h | grep -A2 "Swap"
        echo ""
        echo "Disk Usage:"
        df -h /
        echo ""

        echo "=== DOCKER SERVICES ==="
        docker-compose -f "$COMPOSE_FILE" ps
        echo ""

        echo "=== SERVICE HEALTH ==="
        curl -s http://localhost:8080/actuator/health | jq . || echo "Gateway not responding"
        curl -s http://localhost:8081/actuator/health | jq . || echo "Order service not responding"
        curl -s http://localhost:8082/actuator/health | jq . || echo "Product service not responding"
        curl -s http://localhost:8083/actuator/health | jq . || echo "Customer service not responding"
        echo ""

        echo "=== CONSUL SERVICES ==="
        curl -s http://localhost:8500/v1/catalog/services | jq .
        echo ""

    } | tee "$report_file"

    echo "Report saved to: $report_file"
}

# Get metrics from all services
get_metrics() {
    print_header
    echo -e "${GREEN}Collecting Metrics from All Services${NC}"
    echo ""

    # API Gateway
    echo -e "${BLUE}API Gateway Metrics:${NC}"
    curl -s http://localhost:8080/actuator/metrics | jq '.metrics[] | select(.name | contains("jvm")) | {name: .name, measurement: .measurements[0].value}' || echo "Gateway metrics not available"
    echo ""

    # Order Service
    echo -e "${BLUE}Order Service Metrics:${NC}"
    curl -s http://localhost:8081/actuator/metrics | jq '.metrics[] | select(.name | contains("jvm")) | {name: .name, measurement: .measurements[0].value}' || echo "Order service metrics not available"
    echo ""

    # Product Service
    echo -e "${BLUE}Product Service Metrics:${NC}"
    curl -s http://localhost:8082/actuator/metrics | jq '.metrics[] | select(.name | contains("jvm")) | {name: .name, measurement: .measurements[0].value}' || echo "Product service metrics not available"
    echo ""

    # Customer Service
    echo -e "${BLUE}Customer Service Metrics:${NC}"
    curl -s http://localhost:8083/actuator/metrics | jq '.metrics[] | select(.name | contains("jvm")) | {name: .name, measurement: .measurements[0].value}' || echo "Customer service metrics not available"
    echo ""
}

# Alert on resource thresholds
check_alerts() {
    print_header
    echo -e "${GREEN}Checking Resource Alerts...${NC}"
    echo ""

    local alerts=0

    # Check RAM usage
    local total_mem=$(free -m | awk 'NR==2{print $2}')
    local used_mem=$(free -m | awk 'NR==2{print $3}')
    local usage_percent=$((used_mem * 100 / total_mem))

    if [ $usage_percent -gt 90 ]; then
        echo -e "${RED}🚨 ALERT: Critical RAM usage: ${usage_percent}%${NC}"
        ((alerts++))
    elif [ $usage_percent -gt 80 ]; then
        echo -e "${YELLOW}⚠️  WARNING: High RAM usage: ${usage_percent}%${NC}"
        ((alerts++))
    fi

    # Check disk usage
    local disk_usage=$(df / | awk 'NR==2 {print $5}' | sed 's/%//')
    disk_usage=${disk_usage%.*}

    if [ ${disk_usage%.*} -gt 80 ]; then
        echo -e "${YELLOW}⚠️  WARNING: High disk usage: ${disk_usage}%${NC}"
        ((alerts++))
    fi

    # Check swap usage
    local swap_total=$(free -m | awk 'NR==3{print $2}')
    local swap_used=$(free -m | awk 'NR==3{print $3}')

    if [ $swap_used -gt 1000 ]; then
        echo -e "${YELLOW}⚠️  WARNING: High swap usage: ${swap_used} MB${NC}"
        ((alerts++))
    fi

    if [ $alerts -eq 0 ]; then
        echo -e "${GREEN}✓ No resource alerts${NC}"
    else
        echo -e "${YELLOW}⚠️  Total alerts: $alerts${NC}"
    fi
}

# Main script logic
case "${1:-resources}" in
    resources)
        monitor_resources
        ;;
    services)
        monitor_services
        ;;
    logs)
        show_logs
        ;;
    report)
        generate_report
        ;;
    metrics)
        get_metrics
        ;;
    alerts)
        check_alerts
        ;;
    all)
        monitor_resources
        echo ""
        monitor_services
        echo ""
        check_alerts
        ;;
    *)
        echo "Usage: $0 {resources|services|logs|report|metrics|alerts|all}"
        echo ""
        echo "Commands:"
        echo "  resources - Monitor system resources (CPU, RAM, Disk)"
        echo "  services  - Check Docker services status and health"
        echo "  logs      - Show recent service logs"
        echo "  report    - Generate deployment report"
        echo "  metrics   - Collect metrics from all services"
        echo "  alerts    - Check for resource usage alerts"
        echo "  all       - Run all monitoring checks"
        exit 1
        ;;
esac
