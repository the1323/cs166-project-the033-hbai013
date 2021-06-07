# cs166-project-the033-hbai013

Init envirement : 
RUN following commands

source ./postgresql/startPostgreSQL.sh

source ./postgresql/createPostgreDB.sh

useless : 

psql -h localhost -p $PGPORT $USER"_DB" < sql/create.sql 


for queries:
psql -h localhost -p $PGPORT $USER"_DB"

git pull https://github.com/the1323/cs166-project-the033-hbai013.git
