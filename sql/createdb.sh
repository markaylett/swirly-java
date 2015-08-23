#!/bin/sh
set -e
sql=${1:-forex.sql}
if [ ! -f $sql ]; then
    echo "file not found: $sql" 1>&2
    exit 1
fi
cat schema.sql user.sql $sql | mysql -h localhost -u root
