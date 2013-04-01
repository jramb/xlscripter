lein run example/test.xls  example/out-tabsep1.txt  example/tabsep.clj
lein run example/test.xlsx example/out-tabsep2.txt  example/tabsep.clj
lein run example/test.xlsx example/out-emacstab.txt example/emacstab.clj

lein run example/data.xlsx example/out-sql-insert.sql example/templater.clj example/sql-template.sql
lein run example/data.xlsx example/out-sql-insert.sql example/templater.clj example/test.tmpl

lein run example/data.xlsx example/out-sql-insert.sql xlscripter.transformer/templater example/test.tmpl
lein run example/data.xlsx example/out-sql-insert.sql :tmpl example/test.tmpl
