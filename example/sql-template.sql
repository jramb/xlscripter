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
-- Positional notation helps to select columns (%3$s selects the third argument (column))
--MODIFY:(replace-string "'" "''")--
--BEGIN_DATA:[2-2]--
load('%1$s', %2$d, to_date('%3$tF','YYYY-MM-DD'));
--END_DATA--
--new section, just to show off
--BEGIN_DATA:[3-]--
load('%1$s', %2$d, to_date('%3$tF','YYYY-MM-DD'));
--END_DATA--
end;

rollback; -- eventually you will want to write "commit;" here
