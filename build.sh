#!/bin/bash
set -e

# Install JDK 21
curl -fsSL https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.6%2B7/OpenJDK21U-jdk_x64_linux_hotspot_21.0.6_7.tar.gz -o /tmp/jdk.tar.gz
tar -xzf /tmp/jdk.tar.gz -C /tmp
export JAVA_HOME=/tmp/jdk-21.0.6+7
export PATH=$JAVA_HOME/bin:$PATH

# Increase Node.js memory limit for webpack bundling
export NODE_OPTIONS="--max-old-space-size=7168"

# Build
chmod +x gradlew
./gradlew :composeApp:jsBrowserDistribution --no-daemon --stacktrace
