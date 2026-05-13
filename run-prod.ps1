# .envを読み込んで環境変数にセット
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#=]+?)\s*=\s*(.*?)\s*$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}

# Spring Boot起動
./gradlew bootRun --args='--spring.profiles.active=prod'