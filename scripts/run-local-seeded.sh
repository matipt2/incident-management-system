#!/usr/bin/env bash
set -euo pipefail

export ITL_BOOTSTRAP_MANAGER_USERNAME="${ITL_BOOTSTRAP_MANAGER_USERNAME:-manager}"
export ITL_BOOTSTRAP_MANAGER_EMAIL="${ITL_BOOTSTRAP_MANAGER_EMAIL:-manager@example.com}"
export ITL_BOOTSTRAP_MANAGER_PASSWORD="${ITL_BOOTSTRAP_MANAGER_PASSWORD:-manager123}"
export ITL_DEMO_DATA_ENABLED="${ITL_DEMO_DATA_ENABLED:-true}"
export JWT_SECRET="${JWT_SECRET:-local-development-secret-key-change-me}"

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Starting local stack with development seed data"
echo "Manager login: ${ITL_BOOTSTRAP_MANAGER_USERNAME} / ${ITL_BOOTSTRAP_MANAGER_PASSWORD}"

exec "${script_dir}/run-local.sh" "$@"
