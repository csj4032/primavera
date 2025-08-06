#!/bin/bash

# Primavera Docker Infrastructure Manager - Root Wrapper
# This is a convenience wrapper that delegates to the actual script

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ACTUAL_SCRIPT="$SCRIPT_DIR/infrastructure/scripts/docker-manager.sh"

if [[ ! -f "$ACTUAL_SCRIPT" ]]; then
    echo "Error: Docker manager script not found at $ACTUAL_SCRIPT"
    exit 1
fi

# Forward all arguments to the actual script
exec "$ACTUAL_SCRIPT" "$@"