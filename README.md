# Voluntia

Aplicación web de VoluntaRed para gestionar voluntarios, organizaciones y campañas ambientales y sociales. Incluye los CRUD y el acceso autónomo por perfil, usando las tecnologías y capas de los manuales proporcionados.

## Ejecutar

Requisitos: Java 17 o superior y PostgreSQL activo en localhost:5432. La primera descarga de dependencias Maven necesita Internet.

```powershell
.\preparar-bd.cmd
.\iniciar.cmd
```

Abre http://localhost:8081 y pulsa **Crear cuenta**. Elige voluntario u organización, completa el registro e ingresa con tu correo y contraseña. El administrador conserva el usuario **admin** y la contraseña local inicial **123456**. En IntelliJ importa `pom.xml`, selecciona JDK 17 y ejecuta `VoluntiaApplication`. Se usa 8081 porque otro servicio ocupa 8080 en esta máquina.

También puedes ejecutar el archivo generado con `java -jar target/voluntia-0.0.1-SNAPSHOT.jar`.

## Base de datos

La aplicación se conecta a `VoluntiaDB` y valida el esquema existente. Este cambio no requiere migraciones ni borra registros. Las cuentas existentes de voluntarios y organizaciones con contraseña BCrypt pueden iniciar sesión con su correo y contraseña.

En pgAdmin usa servidor localhost, puerto 5432, usuario postgres y contraseña 123456. Actualiza Databases > VoluntiaDB > Schemas > public > Tables. Para una instalación nueva, ejecuta `database/crear-base.sql` sobre postgres, cambia a VoluntiaDB y ejecuta `database/schema.sql`. `preparar-bd.cmd` automatiza estos pasos sin recrear tablas existentes.

## Funciones disponibles

- CRUD de voluntarios: nombre, correo, contraseña, ubicación, edad y habilidades.
- CRUD de organizaciones: datos de cuenta, ubicación, RUC e información de contacto.
- CRUD de campañas: título, descripción, ubicación, fecha, requisitos, cupos y organización; búsqueda por texto y ubicación.
- Registro público: crear cuenta de voluntario u organización. No se permite crear administradores ni cambiar el rol desde un formulario.
- Organizaciones: crear voluntariados con descripción, consultar **Mis voluntariados**, editar y eliminar los propios, retirar participantes y marcar asistencia en sus campañas. La organización propietaria se obtiene de la sesión.
- Voluntarios: consultar oportunidades, **Unirme al voluntariado**, **Salir del voluntariado** y revisar **Mis participaciones**. La identidad se obtiene de la sesión, sin aceptar IDs de otras cuentas.
- **Mi cuenta**: consultar y editar los datos propios y cambiar la contraseña. Cambiar correo o contraseña cierra la sesión y exige ingresar nuevamente. No se permite eliminar la cuenta propia ni modificar su rol.
- Administrador: conserva los CRUD generales, la gestión de cuentas y las inscripciones del panel.
- Interfaz HTML, CSS y JavaScript con Thymeleaf, adaptable a móvil, validación y confirmaciones de eliminación.
- API REST con DTO, contraseñas BCrypt, sesiones por perfil, permisos por rol y propietario y protección CSRF.

Para probar el flujo: crea una cuenta de organización, ingresa y publica una campaña en **Mis voluntariados**. Cierra sesión, crea una cuenta de voluntario, ingresa, abre la campaña y pulsa **Unirme al voluntariado**. Desde el detalle o **Mis participaciones** puedes salir. Vuelve a ingresar como organización para gestionar los participantes.

Retirar a un participante elimina solamente su inscripción y conserva su cuenta y otras participaciones. Salir o retirar una inscripción pendiente libera el cupo y permite volver a unirse si la campaña no ha empezado. El voluntario no puede cancelar una asistencia registrada. La organización propietaria o el administrador pueden retirar al participante aunque tenga asistencia, eliminando esa participación del historial y liberando el cupo; la interfaz solicita confirmación explícita. Se rechazan inscripciones duplicadas, campañas iniciadas y sobrecupos, incluso con solicitudes simultáneas.

Eliminar una campaña elimina sus inscripciones y reportes relacionados. Solo el administrador puede eliminar cuentas completas. Para eliminar una organización, primero elimina o reasigna sus campañas.

## Estructura

```text
src/main/java/com/voluntared/voluntia/
  entity/       Entidades JPA y relaciones
  repository/   Interfaces JpaRepository
  service/      Reglas de negocio y transacciones
  dto/          Datos de entrada/salida sin exponer contraseñas
  controller/   Controladores Thymeleaf y API REST
  config/       Acceso por perfil con Spring Security
src/main/resources/
  templates/    Vistas HTML con Thymeleaf
  static/css/   Diseño visual
  static/js/    Confirmaciones, búsqueda y control de envío
database/       Scripts PostgreSQL
scripts/        Preparación e inicio para Windows
```

## API

Cada recurso `/api/voluntarios`, `/api/organizaciones` y `/api/campanias` dispone de:

| Método | Ruta | Acción |
| --- | --- | --- |
| GET | /api/recurso | Listar |
| GET | /api/recurso/{id} | Consultar |
| POST | /api/recurso | Crear, devuelve 201 |
| PUT | /api/recurso/{id} | Actualizar |
| DELETE | /api/recurso/{id} | Eliminar, devuelve 204 |

Rutas adicionales: `/api/voluntarios/{id}/historial`, `/api/organizaciones/{id}/campanias` y `/api/campanias?q=limpieza&ubicacion=Lima`.

La consulta de campañas es pública. Las organizaciones pueden crear, editar y eliminar únicamente sus campañas. Los endpoints `/api/voluntarios/**` y `/api/organizaciones/**` son exclusivos del administrador. El perfil propio se gestiona mediante `/mi-cuenta`, y el historial personal mediante `/mis-participaciones`.

Inicia sesión en `/login` y conserva la cookie `JSESSIONID`. Para POST, PUT y DELETE consulta `/api/csrf` y envía `token` en el encabezado indicado por `headerName`, con la misma sesión. Los errores de validación devuelven 400, los registros inexistentes 404, los permisos insuficientes 403 y los conflictos de integridad 409. Una ruta protegida de la API sin sesión devuelve 401; las escrituras sin token CSRF devuelven 403.

| Método | Ruta | Acción y perfil |
| --- | --- | --- |
| POST | /api/campanias/{id}/mi-inscripcion | Voluntario: unirse, sin cuerpo ni ID de otra cuenta; devuelve 201 |
| DELETE | /api/campanias/{id}/mi-inscripcion | Voluntario: salir; devuelve 204 |
| DELETE | /api/campanias/{id}/participantes/{voluntarioId} | Propietaria o administrador: retirar participante; devuelve 204 |

Ejemplo para que el administrador registre un voluntario por la API; el registro público se realiza mediante `/registro`:

```json
{"nombre":"Ana Pérez","email":"ana@example.org","contrasena":"ejemplo123","ubicacion":"Lima","edad":22,"habilidades":"Reciclaje y organización"}
```

Para organizaciones se usan `ruc` de once dígitos e `infoContacto` en lugar de edad/habilidades. Para campañas: `titulo`, `descripcion`, `ubicacion`, `fecha` (ej. `2026-11-20T09:00:00`), `requisitos` y `cupos`. El administrador debe indicar `organizacionId`; para una organización se asigna desde la sesión y se ignora cualquier valor enviado. Al editar una cuenta se puede omitir la contraseña para conservarla.

## Configuración y pruebas

Variables opcionales: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `ADMIN_PASSWORD` y `PORT`. Los valores predeterminados corresponden a la instalación local solicitada.

```powershell
.\iniciar.cmd -Pruebas
```

Las pruebas de integración usan PostgreSQL. Las pruebas transaccionales revierten sus registros; la prueba de concurrencia crea y elimina sus propias cuentas y campañas. Verifican los CRUD, registro, inicio de sesión, perfil propio, cierre de sesión al cambiar credenciales, permisos sobre cuentas y campañas ajenas, participación, retiro sin eliminar cuentas, corrección de asistencia mediante retiro autorizado, API, CSRF y solicitudes simultáneas.

## Alcance

La implementación utiliza Java, Spring Boot, JPA, DTO, Thymeleaf, HTML, CSS, JavaScript y Spring Security, adaptando las capas y ejemplos de los manuales a Voluntia. Los cambios se realizan directamente en los archivos de `src`, sin generadores Python. La aplicación no necesita Python. Se eliminaron las carpetas auxiliares `tmp` y `.tools`, incluidos los antiguos generadores Python y los textos temporales de los manuales.

IA, mapas, filtros por causa, reportes de impacto y Swagger quedan fuera de esta entrega. La tabla `reporte_impacto` se conserva, sin módulo visual ni endpoints. El cliente utiliza Thymeleaf y JavaScript siguiendo los manuales; Angular no forma parte de esta versión.
