DC_RUN_BACKEND := docker-compose run --rm backend make
DC_RUN_FRONTEND := docker-compose run --rm frontend

build:
	docker-compose build

run:
	docker-compose up

stop:
	docker-compose stop

backend-sbt-shell:
	${DC_RUN_BACKEND} sbt-shell

backend-shell:
	docker-compose run --rm backend bash

clean_local_db:
	@docker kill bootstrap-mysql 2> /dev/null; docker rm bootstrap-mysql 2> /dev/null || true
	@echo "Done"

clean_migrate_generated_code:
	@rm -rf backend/generated_code/*
	@rm -rf backend/migration_manager/target
	@echo "Done"

new_migration:
	@${DC_RUN_BACKEND} new_migration

migrate:
	@${DC_RUN_BACKEND} migrate

frontend_install:
	cd frontend && yarn install

first:
	${MAKE} migrate
	${MAKE} frontend_install
