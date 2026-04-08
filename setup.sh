#!/bin/bash
# Unix/Linux/macOS Setup Script for Order Management
# Run this script to fix line endings and start Docker services

set -e

echo "Setting up Order Management Project..."

# Set Git to use LF line endings
echo "Configuring Git line endings..."
git config core.autocrlf input

# Reset all files to use LF
echo "Resetting files to use LF line endings..."
git rm --cached -r .
git reset --hard HEAD

# Fix shell script line endings manually
echo "Fixing shell script line endings..."
find . -name "*.sh" -type f -exec sed -i 's/\r$//' {} \;
echo "Shell scripts fixed!"

# Clean up Docker
echo "Cleaning up Docker containers and volumes..."
docker compose down -v
docker system prune -f

# Start services
echo "Starting Docker services..."
docker compose up -d --build

echo ""
echo "Setup complete!"
echo "Services will be available at:"
echo "  - API Gateway: http://localhost:8080"
echo "  - Consul: http://localhost:8500"
echo "  - pgAdmin: http://localhost:5050"
echo ""
echo "To check service status, run: docker compose ps"
echo "To view logs, run: docker compose logs -f"
