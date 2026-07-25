#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 ProfPayWallet Release Builder${NC}"
echo "=================================="

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${RED}❌ Error: .env file not found!${NC}"
    echo ""
    echo "Please create a .env file with the following content:"
    echo ""
    echo "KEYSTORE_FILE=path/to/your/keystore.jks"
    echo "KEYSTORE_PASSWORD=your_keystore_password"
    echo "KEY_ALIAS=your_key_alias"
    echo "KEY_PASSWORD=your_key_password"
    echo ""
    exit 1
fi

# Load .env file
export $(grep -v '^#' .env | xargs)

# Validate required variables
if [ -z "$KEYSTORE_FILE" ] || [ -z "$KEYSTORE_PASSWORD" ] || [ -z "$KEY_ALIAS" ] || [ -z "$KEY_PASSWORD" ]; then
    echo -e "${RED}❌ Error: Missing required environment variables in .env${NC}"
    exit 1
fi

# Check if keystore file exists
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo -e "${RED}❌ Error: Keystore file not found: $KEYSTORE_FILE${NC}"
    exit 1
fi

echo -e "${YELLOW}📦 Building Docker image...${NC}"

# Build Docker image with build args
docker build \
    -f docker/Dockerfile.release \
    --build-arg KEYSTORE_FILE="$KEYSTORE_FILE" \
    --build-arg KEYSTORE_PASSWORD="$KEYSTORE_PASSWORD" \
    --build-arg KEY_ALIAS="$KEY_ALIAS" \
    --build-arg KEY_PASSWORD="$KEY_PASSWORD" \
    -t telegramwallet-builder \
    .

echo -e "${YELLOW}📥 Extracting APK...${NC}"

# Create output directory
mkdir -p ./build-output

# Extract APK from container
docker run --rm \
    -v "$(pwd)/build-output:/out" \
    telegramwallet-builder \
    cp /app/app-release.apk /out/

# Get APK info
APK_PATH="./build-output/app-release.apk"
if [ -f "$APK_PATH" ]; then
    APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
    echo ""
    echo -e "${GREEN}✅ Build successful!${NC}"
    echo "=================================="
    echo -e "📱 APK Location: ${YELLOW}$APK_PATH${NC}"
    echo -e "📦 APK Size: ${YELLOW}$APK_SIZE${NC}"
    echo ""
else
    echo -e "${RED}❌ Build failed! APK not found.${NC}"
    exit 1
fi
