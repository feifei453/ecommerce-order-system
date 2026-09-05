$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

if (-not (Test-Path ".env.local")) {
    Copy-Item ".env.local.example" ".env.local"
}

docker compose --env-file .env.local -f docker-compose.local.yml up -d
docker compose --env-file .env.local -f docker-compose.local.yml ps
