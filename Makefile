test-sqlite:
	lein run -i example/data.xlsx -o example/out-sqlite.log -t :sqlite test-sqlite.db

test-template:
	lein run -i example/data.xlsx -o example/out-sql-insert.sql -t :template example/test.tmpl


package:
	lein uberjar
