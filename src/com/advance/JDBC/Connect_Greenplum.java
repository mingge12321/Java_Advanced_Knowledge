package com.advance.JDBC;

import java.sql.*;
import java.util.*;

/**
 * @Auther: 谷天乐
 * @Date: 2018/11/14 16:47
 * @Description:
 * 通过JDBC连接Greenplum
 */
public class Connect_Greenplum {
    //表信息
    static class TbInfo
    {
        //分布键
        String id;
        //日期
        String date;
        //价格
        String amt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getAmt() {
            return amt;
        }

        public void setAmt(String amt) {
            this.amt = amt;
        }
    }

    //表结构
    static class TbStructure{
        //appendonly属性
        boolean appendonly = true;
        //压缩类型
        String compresstype = "zlib";
        //压缩级别
        int compresslevel = 5;
        //表的列和类型，用逗号分隔
        String columnInfo;
        //表名称
        String tbName;
        //分布键
        String distributedKey;

        public String getCompresstype() {
            return compresstype;
        }

        public void setCompresstype(String compresstype) {
            this.compresstype = compresstype;
        }

        public boolean isAppendonly() {
            return appendonly;
        }

        public void setAppendonly(boolean appendonly) {
            this.appendonly = appendonly;
        }

        public int getCompresslevel() {
            return compresslevel;
        }

        public void setCompresslevel(int compresslevel) {
            this.compresslevel = compresslevel;
        }

        public String getColumnInfo() {
            return columnInfo;
        }

        public void setColumnInfo(String columnInfo) {
            this.columnInfo = columnInfo;
        }

        public String getTbName() {
            return tbName;
        }

        public void setTbName(String tbName) {
            this.tbName = tbName;
        }

        public String getDistributedKey() {
            return distributedKey;
        }

        public void setDistributedKey(String distributedKey) {
            this.distributedKey = distributedKey;
        }
    }

    //三大核心接口
    private static Connection conn = null;
    private static PreparedStatement pstmt = null;
    private static ResultSet rs = null;

    //连接数据库
    public static Connection connectGreenplum() throws ClassNotFoundException, SQLException {
        // URL
        String url = "jdbc:pivotal:greenplum://192.168.94.135:5432;DatabaseName=testdw";
        // 数据库用户名
        String username = "gpadmin";
        // 数据库密码
        String password = "gpadmin";
        // 加载驱动
        Class.forName("com.pivotal.jdbc.GreenplumDriver");
        // 获取连接
        conn = DriverManager.getConnection(url, username, password);
        return conn;
    }

    //关闭数据库连接
    public static  void closeConnection(){
        if(rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(pstmt!=null){
            try {
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    //查询方法
    public static ResultSet query(String sql) throws SQLException, ClassNotFoundException {
        Connection conn = connectGreenplum();
        pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();
        return rs;
    }

    //通用增删改
    public static void update(String sql,Object []values) throws SQLException, ClassNotFoundException {
        //获取数据库链接
        conn=connectGreenplum();
        try {
            //预编译
            pstmt=conn.prepareStatement(sql);
            //获取ParameterMetaData()对象
            ParameterMetaData pmd=pstmt.getParameterMetaData();
            //获取参数个数
            int number=pmd.getParameterCount();
            //循环设置参数值
            for (int i = 1; i <=number; i++) {
                pstmt.setObject(i, values[i-1]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            closeConnection();
        }
    }



    //新增方法
    public static Integer insert(String sql,TbInfo tbInfo) throws SQLException, ClassNotFoundException {
        Connection conn = connectGreenplum();
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,tbInfo.getId());
        pstmt.setString(2,tbInfo.getDate());
        pstmt.setString(3,tbInfo.getAmt());
        Integer rs = pstmt.executeUpdate();
        return rs;
    }

    //删除方法
    public static Integer delete(String sql,TbInfo tbInfo) throws SQLException, ClassNotFoundException {
        conn = connectGreenplum();
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,tbInfo.getId());
        Integer rs = pstmt.executeUpdate();
        return rs;
    }

    //修改方法
    public static Integer update(String sql,TbInfo tbInfo) throws SQLException, ClassNotFoundException {
        conn = connectGreenplum();
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,tbInfo.getAmt());
        pstmt.setString(2,tbInfo.getId());
        Integer rs = pstmt.executeUpdate();
        return rs;
    }

    //输出
    public static void output(ResultSet rs) throws SQLException {
        while(rs.next())
        {
            System.out.println(
                    "id："+rs.getString("id")+
                    " 日期:"+rs.getString("date")+
                    " 价格:"+rs.getString("amt"));
        }
    }

    /**
     * @Author 谷天乐
     * @Description 输出查询结果
     * @Date 2019/2/13 10:33
     * @Param [rs]
     * @return void
     **/
    public static void generalOutput(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等
        int columnCount = md.getColumnCount();
        while(rs.next())
        {
            StringBuffer sb = new StringBuffer();
            for (int i = 1; i <= columnCount; i++) {
                sb.append(rs.getObject(i)+" ");
            }
            System.out.println(sb);
        }
    }

    /**
     *
     * @Title: resultSetToList
     * @Description: ResultSet转换成list
     * 调用方法如下
     * String selectSql = "sql语句";
    ResultSet rs = query(selectSql);
    List<TbInfo> tbInfos = resultSetToList(rs);
     * 遍历方法如下
    Iterator it = tbInfos.iterator();
    while(it.hasNext()) {
    Map hm = (Map)it.next();
    logger.info(hm.get("key的名称"));
    }
     * @Author: chenjinyao
     * @param: @param rs
     * @param: @return
     * @param: @throws java.sql.SQLException
     * @return: List
     * @throws
     */
    public static List resultSetToList(ResultSet rs,String columnName) throws java.sql.SQLException {
        if (rs == null)
            return Collections.EMPTY_LIST;
        ResultSetMetaData md = rs.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等
        int columnCount = md.getColumnCount(); //返回此 ResultSet 对象中的列数
        List list = new ArrayList();
        Map rowData;
        while (rs.next()) {
            rowData = new HashMap(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));
            }
            if(columnName==null) {
                list.add(rowData);
            }else {
                list.add(rowData.get(columnName));
            }
        }
        return list;
    }

    //返回手机号list
    public static List<String> resultSetToList2(ResultSet rs) throws java.sql.SQLException {
        if (rs == null)
            return null;
        ResultSetMetaData md = rs.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等
        int columnCount = md.getColumnCount(); //返回此 ResultSet 对象中的列数
        List list = new ArrayList();
        Map rowData;
        while (rs.next()) {
            rowData = new HashMap(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(rowData.get("serial_number"));
        }
        return list;
    }

    //增加表的列
    public static Integer addColumn(String tableName,String column) throws SQLException, ClassNotFoundException {
        conn = connectGreenplum();
        String sql = "ALTER TABLE "+tableName+" ADD COLUMN "+column+";";
        pstmt = conn.prepareStatement(sql);
        Integer rs = pstmt.executeUpdate();
        return rs;
    }

    //删除表的列
    public static Integer dropColumn(String tableName,String column) throws SQLException, ClassNotFoundException {
        conn = connectGreenplum();
        String sql = "ALTER TABLE "+tableName+" DROP COLUMN "+column+";";
        pstmt = conn.prepareStatement(sql);
        Integer rs = pstmt.executeUpdate();
        return rs;
    }

    //删除表
    public static Integer dropTable(String tableName) throws SQLException, ClassNotFoundException {
        conn = connectGreenplum();
        String sql = "DROP TABLE if EXISTS "+tableName+";";
        pstmt = conn.prepareStatement(sql);
        Integer rs = pstmt.executeUpdate();
        return rs;
    }

    //复制外部表
    public static Integer copyExternalTable(String tableName) throws SQLException, ClassNotFoundException {
        conn = connectGreenplum();
        String sql = "CREATE TABLE "+tableName+"_copy as SELECT * FROM "+tableName+";";
        pstmt = conn.prepareStatement(sql);
        Integer rs = pstmt.executeUpdate();
        return rs;
    }

    //删除外部表
    public static Integer dropExternalTable(String tableName) throws SQLException, ClassNotFoundException {
        conn = connectGreenplum();
        String sql = "DROP EXTERNAL TABLE if EXISTS "+tableName+";";
        pstmt = conn.prepareStatement(sql);
        Integer rs = pstmt.executeUpdate();
        return rs;
    }

    //创建表
    public static Integer createTable(String tbName,String columnInfo,String distributedKey) throws SQLException, ClassNotFoundException {
        conn = connectGreenplum();
        TbStructure tbStructure = new TbStructure();
        String sql = "CREATE TABLE "+tbName+" ("+columnInfo+")\n" +
                "WITH (appendonly="+tbStructure.isAppendonly()+", " +
                "compresstype="+tbStructure.getCompresstype()+",\n" +
                "compresslevel="+tbStructure.getCompresslevel()+")   DISTRIBUTED BY ("+distributedKey+");";
        pstmt = conn.prepareStatement(sql);
        Integer rs = pstmt.executeUpdate();
        return rs;
    }

    //创建可读外部表，需要启动gpfdist服务，再导入csv
    //需要启动gpfdist服务： gpfdist -d /data/load_files -p 8081 -l /home/gpadmin/log &
    public static Integer createExternalTable(String tbName,String columnInfo,String location,
                                              String format,String delimiter) throws SQLException, ClassNotFoundException {
        conn = connectGreenplum();
        String sql = "CREATE EXTERNAL TABLE "+tbName+" ("+columnInfo+") \n" +
                "LOCATION ("+"\'"+location+"\'"+")\n" +
                "FORMAT "+"\'"+format+"\'"+" (DELIMITER "+"\'"+delimiter+"\'"+")\n";
        pstmt = conn.prepareStatement(sql);
        Integer rs = pstmt.executeUpdate();
        System.out.println("成功创建外部表");
        return rs;
    }

    public static void main(String[] args) {
        try {
            // 插入功能
            /*String insertSql = "insert into tb_cp_02 values(?,?,?);";
            TbInfo tbInfo1 = new TbInfo();
            tbInfo1.setId("7");
            tbInfo1.setDate("2013-06-01");
            tbInfo1.setAmt("500.00");
            insert(insertSql,tbInfo1);*/
            // 删除功能
            /*String deleteSql = "delete from tb_cp_02 where id = ?";
            TbInfo tbInfo1 = new TbInfo();
            tbInfo1.setId("2");
            delete(deleteSql,tbInfo1);*/

            // 修改功能
            /*String updateSql = "update tb_cp_02 set amt = ? where id = ?";
            TbInfo tbInfo1 = new TbInfo();
            tbInfo1.setId("3");
            tbInfo1.setAmt("1001.0");
            update(updateSql,tbInfo1);*/

            /*for (TbInfo tb:tbInfos
                 ) {
                System.out.println(tb.getId());
            }*/
            //dropTable("tb_tag_1_read");
            //createTable("foo","a int, b text","a");
//            dropExternalTable("tb_tag_1_read");
//            createExternalTable("tb_tag_1_read","id text,school_commun_flag text,wire_tv_flag text",
//                    "gpfdist://mdw:8081/20190108.csv","CSV",";");
            //update("update tb_cp_02 set amt = ? where id = ?",new Object[]{"1000","7"});


            //查询
            String selectSql = "select * from tb_cp_02";
            ResultSet rs = query(selectSql);
            generalOutput(rs);
/*            List<TbInfo> tbInfos = resultSetToList(rs);
            Iterator it = tbInfos.iterator();
            while(it.hasNext()) {
                Map hm = (Map)it.next();
                System.out.println(hm.get("id"));
                System.out.println(hm.get("amt"));
                System.out.println(hm.get("date"));
            }*/

            //List<String> date = resultSetToList2(rs);
            //System.out.println(date);
            //addColumn("ta_ovm_cust_01_day_20190108","description text");
            //dropColumn("ta_ovm_cust_01_day_20190108","description");

            //copyExternalTable("tb_tag_1");
            closeConnection();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}