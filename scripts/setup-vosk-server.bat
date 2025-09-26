@echo off
echo =================================
echo  Setup Servidor Vosk para AudioToTextPlugin
echo =================================
echo.

echo Verificando si Docker está instalado...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker no está instalado o no está en el PATH
    echo Por favor instala Docker Desktop desde: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo Docker encontrado. Iniciando servidor Vosk...
echo.

cd /d "%~dp0\..\\.docker"

echo Iniciando servidor Vosk con modelo español grande (puede tomar unos minutos la primera vez)...

set BASE_MODELS_DIR=%~dp0..\models
set MODEL_DIR=%BASE_MODELS_DIR%\vosk-model-es-0.42
if not exist "%MODEL_DIR%" (
    echo Descargando modelo a %MODEL_DIR% ...
    if not exist "%BASE_MODELS_DIR%" mkdir "%BASE_MODELS_DIR%" 2>nul
    powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://alphacephei.com/vosk/models/vosk-model-es-0.42.zip' -OutFile \"$env:TEMP\\vosk-model-es-0.42.zip\""
    powershell -NoProfile -Command "Expand-Archive -Path \"$env:TEMP\\vosk-model-es-0.42.zip\" -DestinationPath \"%BASE_MODELS_DIR%\" -Force"
    del "%TEMP%\vosk-model-es-0.42.zip" 2>nul
    if not exist "%MODEL_DIR%" (
        REM Asegurar nombre de carpeta esperado
        if exist "%BASE_MODELS_DIR%\vosk-model-es-0.42" (
            rem ok
        ) else (
            for /d %%D in ("%BASE_MODELS_DIR%\vosk-model-es-0.42*") do (
                ren "%%D" "vosk-model-es-0.42"
            )
        )
    )
)

setx VOSK_MODEL_DIR "%MODEL_DIR%" >nul
set VOSK_MODEL_DIR=%MODEL_DIR%

docker compose version >nul 2>&1
if %errorlevel% EQU 0 (
    docker compose up -d vosk-server
) else (
    docker-compose --version >nul 2>&1
    if %errorlevel% EQU 0 (
        docker-compose up -d vosk-server
    ) else (
        echo.
        echo No se encontró Docker Compose. Instala Docker Desktop reciente o Compose v1.
        echo    Guia: https://docs.docker.com/compose/
        pause
        exit /b 1
    )
)

if %errorlevel% equ 0 (
    echo.
    echo Servidor Vosk iniciado correctamente!
    echo.
    echo El servidor está disponible en: ws://localhost:2700
    echo Interfaz web (opcional): http://localhost:2700
    echo.
    echo Para verificar el estado:
    echo   docker ps
    echo.
    echo Para ver logs:
    echo   docker logs plugandplay-vosk-server
    echo.
    echo Para detener el servidor:
    echo   docker compose stop vosk-server  ^(o^)
    echo   docker-compose stop vosk-server
) else (
    echo.
    echo Error al iniciar el servidor Vosk
    echo Revisa los logs con: docker-compose logs vosk-server
)

echo.
pause
