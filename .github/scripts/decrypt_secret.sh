#!/bin/sh
gpg --quiet --batch --yes --decrypt --passphrase="$LARGE_SECRET_PASSWORD" \
--output $HOME/app/google-services.json $HOME/app/google-services.json.gpg