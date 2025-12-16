.PHONY: setup build test up down clean logs db-migrate help

help:
	@echo "Order Processing System - Available Commands:"
	@echo "  make test        - Run tests"
	@echo "  make up          - Start all services"
	@echo "  make down        - Stop all services"

up:
	cd order-processing-system && mvn clean install
	cd product-catalog-service && mvn clean install
	docker-compose build
	docker-compose up -d

test:
	cd order-processing-system && mvn clean verify

down:
	docker-compose down
