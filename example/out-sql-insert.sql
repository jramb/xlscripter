declare
  -- well, you COULD just produce insert statements below,
  -- but this way we have more control.
  procedure load
    (p_str in varchar2,
     p_num in number,
     p_date in date)
  is
    r my_table%rowtype;
  begin
    r.str_col := p_str;
    r.num_col := p_num;
    r.date_col := p_date;
    r.creation_date := sysdate;
    insert into my_table values r;
  end;
begin
-- now comes the data part
-- the template i in the unspeakable (but powerful) format
-- described in http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html
-- Note that the literal % must be written %%

load('AAA', 123, to_date('2013-03-29','YYYY-MM-DD'));
load('CCCCCCC', null, to_date('2100-12-31','YYYY-MM-DD'));
load('Jörg', 7777, to_date('1970-11-25','YYYY-MM-DD'));
load('That''s odd', 0, to_date('2013-01-01','YYYY-MM-DD'));

end;

rollback; -- eventually you will want to write "commit;" here

