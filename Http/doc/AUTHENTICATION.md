# Authentication (JWT)

This service uses stateless Bearer JSON Web Tokens (JWT) for authenticating API requests.

- Public endpoints:
  - `POST /api/auth/token` — obtain a JWT by providing credentials
  - OpenAPI docs: `/swagger-ui.html` and `/v3/api-docs/**`
- Protected endpoints:
  - All other endpoints (e.g., `/api/regions`) require the `Authorization` header

## How it works
1. You authenticate with a username/password (stored in the database) to receive a signed JWT.
2. You include the JWT in the `Authorization: Bearer <jwt>` header on subsequent requests.
3. The server validates the JWT signature and expiration on every request. No server-side sessions are used.

JWT contents:
- `sub` — username
- `iat`/`exp` — issued at / expiration
- `iss` — issuer (`ProgressManager-Backend`)
- `roles` — comma-separated roles (e.g., `ROLE_ADMIN`)

## Database users
Users are stored in the `app_user` table. On first run, a default admin user is seeded by Flyway:

- username: `admin`
- password: `admin123`
- roles: `ADMIN`

You can change or add users directly in the database. Passwords must be stored as BCrypt hashes.

## Obtain a token
Request:

```bash
curl -s -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Response:

```json
{"token":"<jwt>"}
```

## Call a protected endpoint
```bash
TOKEN=<jwt>
curl -s "http://localhost:8080/api/regions?status=CLAIMED" \
  -H "Authorization: Bearer $TOKEN"
```

## Configuration
Set via `src/main/resources/application.properties` or environment variables (shown defaults):

- `app.security.jwt.secret` (env: `JWT_SECRET`): signing secret for HS256
- `app.security.jwt.expiration-ms` (env: `JWT_EXPIRATION_MS`): token lifetime in ms (default 3600000 = 1 hour)
- `ADMIN_DEFAULT_PASSWORD` (env only): on startup, the app ensures the `admin` user's password matches this value (default `admin123`).
- DB connection:
  - `DB_URL`, `DB_USER`, `DB_PASSWORD`

docker-compose sets reasonable defaults for local development.

## Notes
- Always use a strong, long `JWT_SECRET` in production.
- Rotate secrets and keep token lifetimes reasonable.
- Prefer HTTPS so tokens are not exposed in transit.
