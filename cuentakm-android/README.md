# CuentaKm — Android

App nativa Android (Kotlin + Jetpack Compose) que consume la **misma API** que `cuentakm-web`
(`CuentaKm.Api`). No hay backend nuevo: la app es un cliente más.

## Requisitos

- Android Studio (ya instalado en esta máquina) con JDK 21 embebido.
- Android SDK 35 + build-tools 35.
- Un AVD: `Pixel_8` o `Pixel_9_Pro` (ya creados).

## Configuración local (no versionada)

`local.properties` ya trae `sdk.dir`. Agregá estas claves según el entorno:

```properties
# Backend local visto desde el emulador (10.0.2.2 = host)
API_BASE_URL_DEBUG=http://10.0.2.2:5005/

# Backend publicado (Render). Sin esto, la app avisa en pantalla.
API_BASE_URL_RELEASE=https://<tu-servicio>.onrender.com/

# OAuth client de tipo WEB de Google Cloud (el mismo Google:ClientId del backend)
GOOGLE_WEB_CLIENT_ID=xxxxxxxx.apps.googleusercontent.com
```

> `API_BASE_URL_DEBUG` y `API_BASE_URL_RELEASE` se inyectan como `BuildConfig.API_BASE_URL`
> por build type. El `baseUrl` se normaliza para terminar en `/`.

## Correr

```powershell
# CLI
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug

# o desde Android Studio: Open → esta carpeta → Run ▶
```

## Estructura

```
app/src/main/java/com/cuentakm/app/
├── core/          ApiResult, apiCall (reintentos), ApiConfig
├── data/
│   ├── auth/      GoogleSignInClient (Credential Manager)
│   ├── local/     SessionStore (DataStore)
│   ├── mapper/    DTO → dominio
│   ├── remote/    CuentaKmApi (Retrofit), DTOs, AuthInterceptor
│   └── repository/
├── di/            NetworkModule (Hilt)
├── domain/model/  MaintenanceType, MaintenanceStatus, VehicleSummary…
├── ui/            theme, components, navigation, login, home, consult, load
└── util/          Patente (normalizador), Format
```

## Endpoints usados (sin inventar rutas)

| Método | Ruta | Uso |
|---|---|---|
| POST | `api/auth/google` | Login con idToken |
| GET | `api/vehicles/{patente}` | Resumen por servicio |
| GET | `api/vehicles/{patente}/maintenances` | Historial |
| POST | `api/vehicles/{patente}/maintenances` | Cargar cambio |
| DELETE | `api/vehicles/{patente}/maintenances/{id}` | Eliminar registro |
| PUT | `api/vehicles/{patente}/km` | Actualizar kilometraje |

## Decisiones de sostenibilidad

- **Un solo módulo `:app`** con capas separadas por paquete. Se parte en módulos cuando duela.
- **Version catalog** (`gradle/libs.versions.toml`) como única fuente de versiones.
- **Sin dynamic color**: la identidad es la misma que la web.
- **Reintento de red** espejo de `src/api/client.ts` (Render Free duerme el backend).
- **Tolerancia a cambios del backend**: `ignoreUnknownKeys`, `coerceInputValues` y descarte
  de tipos de servicio desconocidos.
- **`network_security_config`** con cleartext sólo en `debug`.

## Pendientes antes de publicar

Ver `android-app-pendientes.md` en la memoria del repo. Resumen:

1. OAuth client **Android** (package + SHA-1) en Google Cloud.
2. SHA-1 de **Play App Signing** además del keystore local.
3. Versionar la API (`/api/v1`) antes de que haya apps instaladas.
4. Keystore de release + AAB firmado.
5. Play Console: política de privacidad, Data safety, testing cerrado (cuentas personales nuevas).
