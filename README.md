# Lumen Lane Ecommerce

This workspace contains a full-stack ecommerce project built with:

- React + Vite frontend
- Spring Boot + Maven backend
- MySQL database
- JWT authentication with Spring Security

## Included tasks from the screenshots

- DB connection configuration
- User entity and repository
- Basic layered Spring Boot structure
- Register API with validation
- BCrypt password encryption
- React registration UI
- Frontend connected to register API
- Login API with credential validation
- JWT authentication and token generation
- Secured APIs with authentication filter
- Protected routes on the frontend
- Login/logout flow
- Product entity and repository
- Add Product API
- Get All Products API

## Project structure

- `backend` - Spring Boot API
- `frontend` - React storefront

## MySQL setup

1. Create or use a local MySQL server.
2. Update the password in [application.properties](/D:/Front-end/backend/src/main/resources/application.properties).
3. The backend uses the database `ecommerce_store` and can create it automatically.

## Backend run

Maven is not installed in the current environment, but the project is configured for Maven.

1. Install Maven locally or open the backend in an IDE with Maven support.
2. Run:

```powershell
cd D:\Front-end\backend
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`.

## Frontend run

1. Install dependencies:

```powershell
cd D:\Front-end\frontend
npm install
```

2. Start the app:

```powershell
npm run dev
```

The frontend will start on `http://localhost:5173`.

## Notes

- The first registered user becomes `ROLE_ADMIN`, which unlocks the Add Product page.
- Product creation requires a valid JWT token from an admin login.
- The products page shows fallback preview items when the backend is not running yet.
