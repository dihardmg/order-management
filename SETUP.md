# Setup Instructions

## Prerequisites
- Docker Desktop (Windows/macOS) or Docker Engine (Linux)
- Git
- Make (optional, for Linux/macOS)

## Fresh Clone Setup

### Windows Users (Git Bash / WSL)
```bash
# Clone the repository
git clone https://github.com/dihardmg/order-management.git
cd order-management

# Ensure proper line endings (LF)
git config core.autocrlf input
git rm --cached -r .
git reset --hard

# Start the services
docker compose up -d --build
```

### macOS Users
```bash
# Clone the repository
git clone <repository-url>
cd order-management

# Ensure proper line endings (LF)
git config core.autocrlf input
git rm --cached -r .
git reset --hard

# Start the services
docker compose up -d --build
```

### Linux Users
```bash
# Clone the repository
git clone <repository-url>
cd order-management

# Ensure proper line endings (LF)
git config core.autocrlf input
git rm --cached -r .
git reset --hard

# Start the services
docker compose up -d --build
```

## Troubleshooting

### PostgreSQL Container Fails to Start (Exit Code 127)
This is typically caused by Windows line endings (CRLF) in shell scripts.

**Solution:**
```bash
# Remove old volumes and containers
docker compose down -v
docker system prune -f

# Reset git line endings
git config core.autocrlf input
git rm --cached -r .
git reset --hard

# Restart
docker compose up -d --build
```

### Manual Line Ending Fix
If the above doesn't work, manually fix line endings:

**Windows (Git Bash):**
```bash
dos2unix docker/init-postgres-fixed.sh
```

**macOS/Linux:**
```bash
sed -i '' 's/\r$//' docker/init-postgres-fixed.sh
```

Then restart:
```bash
docker compose down -v
docker compose up -d --build
```

## Service URLs
- **API Gateway**: http://localhost:8080
- **Order Service**: http://localhost:8081
- **Product Service**: http://localhost:8082
- **Customer Service**: http://localhost:8083
- **Consul**: http://localhost:8500
- **pgAdmin**: http://localhost:5050

## Default Credentials
- **PostgreSQL**: admin/admin123
- **pgAdmin**: admin@admin.com/admin123

## Stopping Services
```bash
docker compose down
```

## Removing All Data
```bash
docker compose down -v
docker system prune -f
```
