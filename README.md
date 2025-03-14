# Grapes Microservices

## Description

This project contains a microservices architecture using Docker and Docker Compose, with services for user management, transactions, deliveries, payments, real-time chats, and data analysis. It also includes an API gateway, front-end services (web and mobile), and infrastructure for monitoring and CI/CD.

## Prerequisites

Before getting started, make sure you have the following installed on your machine:

- [Docker](https://www.docker.com/products/docker-desktop) - Container management
- [Docker Compose](https://docs.docker.com/compose/) - Container orchestration
- [Git](https://git-scm.com/) - To clone the repository
- [Java 21](https://adoptopenjdk.net/) - Required for developing or compiling Java services
- [Node.js](https://nodejs.org/) - Required for frontend services (React)
- [Python 3.10](https://www.python.org/) - Required for the Data Mining service

## Project structure

```plaintext
grapes-microservices/
│── api-gateway/                     # API Gateway (Spring Cloud Gateway)
│── auth-service/                    # Authentication Service
│── transactions-service/            # Transactions Service
│── delivery-service/                # Delivery Service
│── payment-service/                 # Payment Service
│── chat-service/                    # Real-time Chat Service
│── data-mining-service/             # Big Data & Analytics Service
│── frontend-web/                    # Main Web Frontend (React)
│── frontend-mobile/                 # Main Mobile Frontend (Kotlin)
│── config-server/                   # Centralized Configuration Server
│── monitoring/                      # Monitoring Stack (Prometheus, Grafana)
│── ci-cd/                           # CI/CD Config
│── docker/                          # Databases & RabbitMQ images
│── kubernetes/                      # Kubernetes Deployment Files
│── docker-compose.yml               # Local deployment with Docker Compose
│── README.md                        # Project documentation
````

## Launch  project with Docker Compose

1. **Clone the repository**

   Clone this repository on your machine :

    ```bash
    git clone https://github.com/jkom4/grapes-microservices.git
    cd grapes-microservices
    ```

2. **Starting all services with Docker Compose

   To start all the services defined in the `docker-compose.yml` file, run the following command:

    ```bash
    docker-compose up -d
    ```

   This will build and start all your application's containers in the background.

3. **Start specific services

   If you wish to start only certain services, you can do so by specifying the desired services as follows:
    ```bash
    docker-compose up -d api-gateway auth-service payment-service
    ```

   Replace `api-gateway`, `auth-service`, `payment-service` with the names of the services you wish to start.
   NB: DB containers must also be running

## Accessing APIs

Here are the links to the APIs exposed by the various services in your application:

- **API Gateway** : [http://localhost:8090/api-gateway](http://localhost:8090/api-gateway)
- **Auth Service** : [http://localhost:8091/auth-service](http://localhost:8091/auth-service)
- **Transactions Service** : [http://localhost:8092/transactions-service](http://localhost:8092/transactions-service)
- **Delivery Service** : [http://localhost:8093/delivery-service](http://localhost:8093/delivery-service)
- **Payment Service** : [http://localhost:8094/payment-service](http://localhost:8094/payment-service)
- **Chat Service** : [http://localhost:8095/chat-service](http://localhost:8095/chat-service)
- **Data Mining Service** : [http://localhost:8096/data-mining-service](http://localhost:8096/data-mining-service)

## Access Swagger

Swagger is used for API documentation. You can access it via the following links:

- **Swagger API Gateway** : [http://localhost:8090/swagger-ui/index.html](http://localhost:8090/swagger-ui/index.html)
- **Swagger Auth Service** : [http://localhost:8091/swagger-ui/index.html](http://localhost:8091/swagger-ui/index.html)
- **Swagger Transactions Service** : [http://localhost:8092/swagger-ui/index.html](http://localhost:8092/swagger-ui/index.html)
- **Swagger Delivery Service** : [http://localhost:8093/swagger-ui/index.html](http://localhost:8093/swagger-ui/index.html)
- **Swagger Payment Service** : [http://localhost:8094/swagger-ui/index.htmlr](http://localhost:8094/swagger-ui/index.html)

## Stop services

To stop all running services, use the following command:

```bash
docker-compose down
```

This will stop all containers and delete them.

## Development

If you want to make changes to the code or add new features, you can modify the source files and rebuild the Docker images with the following command:
```bash
docker-compose up -d --build
```

This will rebuild the images and restart the services.

## Licenses

This project is licensed under the [MIT license](https://mit-license.org/).