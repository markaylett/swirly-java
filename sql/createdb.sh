#!/bin/sh
sql=${1:-forex.sql}
cat schema.sql user.sql $sql | mysql -h localhost -u root
