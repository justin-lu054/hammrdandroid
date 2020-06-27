#!/bin/sh
gpg --quiet --batch --yes --decrypt --passphrase="$LARGE_SECRET_PASSWORD" \
--output ./app/google-services.json ./app/google-services.json.gpg

gpg --quiet --batch --yes --decrypt --passphrase="$LARGE_SECRET_PASSWORD" \
--output ./app/src/main/res/values/secrets.xml ./app/secrets.xml.gpg