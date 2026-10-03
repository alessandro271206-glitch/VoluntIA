param([string]$PgBin = 'C:\Program Files\PostgreSQL\18\bin')
$ErrorActionPreference = 'Stop'
$psql = Join-Path $PgBin 'psql.exe'
if (!(Test-Path -LiteralPath $psql)) { throw 'No se encontró psql. Indica -PgBin con la carpeta bin de PostgreSQL.' }
$previousPassword = $env:PGPASSWORD
try {
    $env:PGPASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { '123456' }
    $dbUser = if ($env:DB_USER) { $env:DB_USER } else { 'postgres' }
    $exists = & $psql -h localhost -U $dbUser -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='VoluntiaDB'"
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo conectar a PostgreSQL.' }
    if (($exists | Out-String).Trim() -ne '1') {
        & $psql -h localhost -U $dbUser -d postgres -v ON_ERROR_STOP=1 -f (Join-Path $PSScriptRoot '../database/crear-base.sql')
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear VoluntiaDB.' }
    }
    & $psql -h localhost -U $dbUser -d VoluntiaDB -v ON_ERROR_STOP=1 -f (Join-Path $PSScriptRoot '../database/schema.sql')
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo preparar el esquema.' }
    Write-Host 'VoluntiaDB lista. En pgAdmin actualiza Databases > VoluntiaDB > Schemas > public > Tables.'
} finally { $env:PGPASSWORD = $previousPassword }
