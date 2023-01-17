CREATE SCHEMA siconv AUTHORIZATION postgres;

CREATE USER owner_siconv_p WITH PASSWORD 'owner_siconv_p';
CREATE USER usr_siconv_p WITH PASSWORD 'usr_siconv_p';

GRANT USAGE ON SCHEMA siconv TO usr_siconv_p;
GRANT USAGE ON SCHEMA siconv TO owner_siconv_p;

ALTER ROLE postgres SET search_path TO siconv;
ALTER ROLE usr_siconv_p SET search_path TO siconv;
ALTER ROLE owner_siconv_p SET search_path TO siconv;

