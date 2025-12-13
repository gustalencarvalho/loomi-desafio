.PHONY: setup build test up down clean logs db-migrate help

help:
	@echo "Order Processing System - Available Commands:"
	@echo "  make setup       - Setup environment (build Docker images)"
	@echo "  make build       - Build the application"
	@echo "  make test        - Run tests"
	@echo "  make up          - Start all services"
	@echo "  make down        - Stop all services"
	@echo "  make clean       - Remove containers and volumes"
	@echo "  make logs        - View application logs"
	@echo "  make db-migrate  - Run database migrations"

setup:
	docker-compose build

build:
	cd order-processing-system && mvn clean install

test:
	cd order-processing-system && mvn clean test

up:
	docker-compose up -d
	@echo "Waiting for services to be ready..."
	sleep 10
	@echo "Services are up. Application running on http://localhost:8080"

down:
	docker-compose down

clean:
	docker-compose down -v
	mvn clean

logs:
	docker-compose logs -f order-processing-system

db-migrate:
	@echo "Migrations handled automatically by Hibernate"
