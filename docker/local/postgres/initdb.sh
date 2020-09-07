#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER harbeidsgiverbackend WITH PASSWORD 'harbeidsgiverbackend';
    CREATE DATABASE harbeidsgiverbackend;
    CREATE SCHEMA harbeidsgiverbackend;
    GRANT ALL PRIVILEGES ON DATABASE harbeidsgiverbackend TO harbeidsgiverbackend;
EOSQL

psql -v ON_ERROR_STOP=1 --username "harbeidsgiverbackend" --dbname "harbeidsgiverbackend" <<-EOSQL

    create table bakgrunnsjobb (
    jobb_id uuid unique not null,
    type VARCHAR(100) not null,
    behandlet timestamp,
    opprettet timestamp not null,

    status VARCHAR(50) not null,
    kjoeretid timestamp not null,

    forsoek int not null default 0,
    maks_forsoek int not null,
    data jsonb
)

EOSQL
