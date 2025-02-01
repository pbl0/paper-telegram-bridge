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

# Stop the existing container, but continue even if it fails
podman stop mcserver-paper || echo "Failed to stop mcserver-paper. Continuing..."

# Run the new container
podman run \
        --cgroups=no-conmon \
        --rm \
        --sdnotify=conmon \
        --replace \
        --name mcserver-paper \
        -v /home/pablo/Desktop/minecraft_server/mcserver:/data \
        -e UID=1000 \
        -e GID=1000 \
        -e TYPE=PAPER \
        -e MEMORY=8G \
        -e VERSION=1.21.4 \
        -e USE_AIKAR_FLAGS=true \
        -e CREATE_CONSOLE_IN_PIPE=true \
        -e ENABLE_RCON=false \
        -e TZ=Europe/Madrid \
        -p 25565:25565 \
        -p 4321:8080 \
        -p 24454:24454/udp \
        -p 19132:19132/udp \
        -e SPIGET_RESOURCES="28140,10079,69584,2124,60088,59773,73638,93051,62325,47136,6245,80279,34315,88135" \
        -e EULA=TRUE docker.io/itzg/minecraft-server