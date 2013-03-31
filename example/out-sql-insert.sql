Hello, before1
--MODIFY:(replace-string "'" "''")--
--MODIFY:(replace-string "Jörg" "Anton")--
Block1: 'AAA'
Block1: 'BBB'

between 1
between 2
Block2: 2013-03-29
        'AAA' 123
Block2: 1900-01-02
        'BBB' -1
Block2: 2100-12-31
        'CCCCCCC' null
Block2: 1970-11-25
        'Anton' 7777
Block2: 2013-01-01
        'That''s odd' 0


also between 3

***ERROR ROW 1***
java.util.IllegalFormatConversionException: d != java.lang.String:
  Block 3: '%s', %d, %tF
("String" "Number" "Date")

^^^ERROR^^^
  Block 3: 'AAA', 123, 2013-03-29
  Block 3: 'BBB', -1, 1900-01-02
  Block 3: 'CCCCCCC', null, 2100-12-31
  Block 3: 'Anton', 7777, 1970-11-25
  Block 3: 'That''s odd', 0, 2013-01-01


this is the last line.


