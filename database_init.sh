myuser=root
mydb=isu4_qualifier
# brew で mysqlをinstallすると以下のような設定になってる
# dummy_log.sqlを実行する時にこの設定を書き換えてるのでデフォルトを残しておく
mysql -u ${myuser} -e "set @@sql_mode='STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION';"
mysql -u ${myuser} -e "DROP DATABASE IF EXISTS ${mydb}; CREATE DATABASE ${mydb}"
mysql -u ${myuser} ${mydb} < sql/schema.sql
mysql -u ${myuser} ${mydb} < sql/dummy_users.lite.sql
mysql -u ${myuser} ${mydb} < sql/dummy_log.sql
