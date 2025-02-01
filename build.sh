#!/bin/bash

# Build the project with Gradle
if ! gradle; then
    echo "Gradle build failed. Exiting."
    exit 1
fi

# Move the built JAR file to the plugins directory
if ! mv -f build/libs/paper-telegram-bridge-1.21.4-v0.21.0.jar $HOME/Desktop/minecraft_server/mcserver/plugins; then
    echo "Failed to move JAR file. Exiting."
    exit 1
fi

podman exec mcserver-paper mc-send-to-console plugman reload PaperTelegramBridge;