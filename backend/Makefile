SHELL           := /bin/bash -o pipefail

TESTS :=

SBT   := COURSIER_CACHE=/sbt/.cache sbt -Dsbt.classloader.close=false \
				-Dsbt.supershell=false \
				-Dsbt.global.base=/sbt/.sbt/ \
				-Dsbt.ivy.home=/sbt/.ivy2/ \
				-Divy.home=/sbt/.ivy2/


# Make sure the dependencies are running on the expected port before continuing.
define wait
	@MAX_TRIES=30; TRIES=0; \
	while ! nc -z $${$(1)} $${$(2)} && (( TRIES < MAX_TRIES )); do \
		echo "Waiting on $${$(1)}:$${$(2)}"; \
		sleep 1.000; \
        let TRIES=TRIES+1; \
	done; \
	if (( TRIES == MAX_TRIES )); then \
	    echo "Failed to connect to $(1). Aborting."; \
	    exit 1; \
	fi;
endef

mysql-dependency-connection:
	$(call wait,DB_HOSTNAME:-127.0.0.1,DB_PORT:-3306)

sbt-shell:
	${SBT}

restart:
	${SBT} \~reStart

run:
	${SBT} run

new_migration:
	${SBT} "mg new s"

migrate:
	${SBT} "mg init" 2>/dev/null || true
	${SBT} "mg update" "mg preview" "mg apply" "mg codegen"
