gpg --quiet --batch --yes --decrypt --passphrase="$LARGE_SECRET_PASSWORD" \
--output ./app/google-services.json ./app/google-services.json.gpg