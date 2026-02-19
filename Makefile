.PHONY: up down build logs

up:
	docker-compose -f infrastructure/docker-compose.yml up -d

down:
	docker-compose -f infrastructure/docker-compose.yml down

build:
	docker-compose -f infrastructure/docker-compose.yml build

logs:
	docker-compose -f infrastructure/docker-compose.yml logs -f
