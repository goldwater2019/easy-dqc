

#### oracle

> oracle 中没有databases的概念, 但是用user来作为catalog信息

```sql
SELECT USERNAME FROM ALL_USERS ORDER BY USERNAME; 
```

> oracle的show tables与众不同

```sql
-- 关注所有有权限的表
SELECT owner, table_name FROM all_tables;
-- 只关注属于自己的表
SELECT  table_name FROM user_tables;
```

> oracle 查看建表语句
```sql
SELECT all_tab.column_name, all_tab.data_type, all_tab.data_length, (SELECT COMMENTS FROM user_col_comments t where t.TABLE_NAME = all_tab.TABLE_NAME and t.COLUMN_NAME = all_tab.column_name) FROM all_tab_columns all_tab WHERE all_tab.TABLE_NAME = 'MY_TABLE'
```

> oracle 查看表的主键
```sql
SELECT cols.table_name, cols.column_name, cols.position, cons.status, cons.owner FROM all_constraints cons, all_cons_columns cols WHERE cols.table_name = 'TABLE_NAME' AND cons.constraint_type = 'P' AND cons.constraint_name = cols.constraint_name AND cons.owner = cols.owner ORDER BY cols.table_name, cols.position;
```

> oracle 查看表的索引

```sql
SELECT  * FROM    all_indexes WHERE   table_name = 'TABLE_NAME'
```