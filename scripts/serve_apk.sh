#!/usr/bin/env bash
# Compila el APK release, levanta un contenedor nginx que lo sirve y
# muestra un QR en la terminal apuntando a esa URL, para escanearlo con el celular.
#
# Uso: ./scripts/serve_apk.sh [puerto]
#   puerto: opcional, default 8080
#
# Requiere: docker, qrencode y el wrapper de gradle (./gradlew).

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APK_SRC="${SCRIPT_DIR}/../app/build/outputs/apk/release/app-release.apk"
PORT="${1:-8080}"
CONTAINER_NAME="fitswap-apk-server"
SERVE_DIR="$(mktemp -d)"

if [[ -z "${JAVA_HOME:-}" ]] && [[ -x /opt/android-studio/jbr/bin/java ]]; then
    export JAVA_HOME=/opt/android-studio/jbr
fi

echo "Compilando APK (./gradlew assembleRelease)..."
"${SCRIPT_DIR}/../gradlew" -p "${SCRIPT_DIR}/.." assembleRelease

if [[ ! -f "$APK_SRC" ]]; then
    echo "La build termino pero no encontre el APK en: $APK_SRC" >&2
    exit 1
fi

cp "$APK_SRC" "$SERVE_DIR/app-release.apk"
chmod 755 "$SERVE_DIR"
chmod 644 "$SERVE_DIR/app-release.apk"

# Si ya habia un contenedor con este nombre, lo saco antes de levantar uno nuevo.
docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true

docker run -d \
    --name "$CONTAINER_NAME" \
    -p "${PORT}:80" \
    -v "${SERVE_DIR}:/usr/share/nginx/html:ro" \
    nginx:alpine >/dev/null

IP="$(ip route get 1.1.1.1 | awk '{for(i=1;i<=NF;i++) if ($i=="src") print $(i+1)}')"
URL="http://${IP}:${PORT}/app-release.apk"

echo "Contenedor '${CONTAINER_NAME}' corriendo, sirviendo:"
echo "  ${URL}"
echo
qrencode -t ANSIUTF8 "$URL"
echo
echo "Escanea el QR desde el celular (misma red WiFi) para descargar el APK."
echo "Para bajar el contenedor despues: docker rm -f ${CONTAINER_NAME}"
