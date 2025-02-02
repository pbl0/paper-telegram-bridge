#!/bin/bash
# Script that builds and moves the plugin to my plugins/ directory and reloads the plugin with plugmanX command.

# Extract version and api-version from plugin.yml
PLUGIN_YML="src/main/resources/plugin.yml"
VERSION=$(grep -E '^version:' "$PLUGIN_YML" | awk '{print $2}' | tr -d '"')
API_VERSION=$(grep -E '^api-version:' "$PLUGIN_YML" | awk '{print $2}' | tr -d '"')

if [[ -z "$VERSION" || -z "$API_VERSION" ]]; then
    echo "Failed to extract version or api-version from plugin.yml. Exiting."
    exit 1
fi

# Build the project with Gradle
if ! gradle; then
    echo "Gradle build failed. Exiting."
    exit 1
fi

# Move the built JAR file to the plugins directory
JAR_FILE="build/libs/paper-telegram-bridge-${API_VERSION}-v${VERSION}.jar"
PLUGINS_DIR="$HOME/Desktop/minecraft_server/mcserver/plugins"

if ! mv -f "$JAR_FILE" "$PLUGINS_DIR"; then
    echo "Failed to move JAR file. Exiting."
    exit 1
fi

podman exec mcserver-paper mc-send-to-console plugman reload PaperTelegramBridge;
