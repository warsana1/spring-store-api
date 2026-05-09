# Store API

A Spring Boot 3 REST API for an e-commerce store with JWT authentication, shopping cart management, order processing, and Stripe payment integration.

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4.1, Java 17 |
| Security | Spring Security 6, JWT (JJWT 0.12.6) |
| Database | MySQL 8, Spring Data JPA, Flyway |
| Payments | Stripe Java SDK v29 |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Mapping | MapStruct 1.6.3, Lombok |
| Deployment | Docker, Railway |

## Prerequisites

- Java 17+
- MySQL 8 running on `localhost:3307`
- A [Stripe](https://stripe.com) account (for payment features)

## Setup

**1. Clone and configure environment**

Copy `.env.example` to `.env` and fill in your values:

```env
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_jwt_secret_key
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET_KEY=whsec_...
```

**2. Create the database**

```sql
CREATE DATABASE store_api;
```

**3. Run the application**

```bash
./mvnw spring-boot:run
```

Flyway will automatically apply all migrations and seed sample data on startup.

## API Documentation

Swagger UI is available at `http://localhost:8080/swagger-ui.html` when running locally.

## API Endpoints

### Authentication

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | Login — returns access token + sets refresh token cookie |
| POST | `/auth/refresh` | Cookie | Exchange refresh token for a new access token |
| GET | `/auth/me` | JWT | Get current authenticated user |

### Users

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/users` | Public | Register a new user |
| GET | `/users` | JWT | List all users (sort by `name` or `email`) |
| GET | `/users/{id}` | JWT | Get user by ID |
| PUT | `/users/{id}` | JWT | Update user |
| DELETE | `/users/{id}` | JWT | Delete user |
| POST | `/users/{id}/change-password` | JWT | Change password |

### Products

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/products` | Public | List products (filter by `?categoryId=`) |
| GET | `/products/{id}` | Public | Get product details |
| POST | `/products` | JWT | Create product |
| PUT | `/products/{id}` | JWT | Update product |
| DELETE | `/products/{id}` | JWT | Delete product |

### Cart (anonymous)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/carts` | Public | Create a new cart — returns cart UUID |
| GET | `/carts/{cartId}` | Public | Get cart contents |
| POST | `/carts/{cartId}/items` | Public | Add item to cart |
| PUT | `/carts/{cartId}/items/{productId}` | Public | Update item quantity |
| DELETE | `/carts/{cartId}/items/{productId}` | Public | Remove item |
| DELETE | `/carts/{cartId}/items` | Public | Clear cart |

### Checkout & Orders

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/checkout` | JWT | Initiate checkout — returns order ID + Stripe payment URL |
| POST | `/checkout/webhook` | Public | Stripe webhook receiver (marks order PAID) |
| GET | `/orders` | JWT | List current user's orders |
| GET | `/orders/{orderId}` | JWT | Get order details |

### Admin

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/admin/hello` | ADMIN role | Admin-only endpoint |

## Authentication Flow

1. `POST /auth/login` with `{ "email": "...", "password": "..." }`
2. Response contains a short-lived **access token** (15 min) and sets an HTTP-only cookie with a **refresh token** (7 days)
3. Pass the access token as `Authorization: Bearer <token>` on protected requests
4. Call `POST /auth/refresh` to get a new access token when it expires

## Database Schema

Managed by Flyway. Migrations live in `src/main/resources/db/migration/`:

| Migration | Description |
|---|---|
| V1 | Users, addresses, categories, products, profiles, wishlist |
| V2 | Carts and cart items |
| V3 | Role column on users (USER / ADMIN) |
| V4 | Orders and order items |
| V5 | Seed data — 6 categories, 10 sample products |

## Running with Docker

```bash
docker build -t store-api .
docker run -p 8080:8080 --env-file .env store-api
```

The Dockerfile uses a two-stage build: Maven for compilation, then a minimal Alpine JRE image for the runtime.

## Configuration Profiles

| Profile | Usage |
|---|---|
| `dev` (default) | MySQL on `localhost:3307`, SQL logging enabled |
| `prod` | Database URL from env vars, connection pool capped at 5, only `/health` actuator exposed |

Switch profiles with `--spring.profiles.active=prod`.

## Health Check

```
GET /actuator/health
```

Returns `{"status":"UP"}` — suitable for load balancer / Railway health checks.

## Project Structure

```
src/main/java/com/codewithmosh/store/
├── config/          # Security, JWT, Stripe configuration
├── controllers/     # REST controllers
├── services/        # Business logic
├── repositories/    # Spring Data JPA repositories
├── entities/        # JPA entities
├── dtos/            # Request / response DTOs
├── mappers/         # MapStruct entity↔DTO mappers
├── filters/         # JWT authentication filter
└── exceptions/      # Custom exception classes
```
