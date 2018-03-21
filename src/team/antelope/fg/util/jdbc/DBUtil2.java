package team.antelope.fg.util.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/*
 * �õ����ӳص�������Ƕ��̣߳���ÿһ���߳�һ�㶼ֻ��Ҫһ�����ݿ����ӣ�
 * ������ThreadLocal�洢Connectionûë��
 */

/**
 * ���ݿ⹤����
 * @author ���Ĳ�
 * @time:2017��11��11�� ����10:42:57
 * @Description:TODO
 */
public class DBUtil2 {
	static{
		Properties prop = new Properties();
		try {
			prop.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("config/jdbc/jdbc.properties"));
			String url = prop.getProperty("url");
			String username = prop.getProperty("username");
			String password = prop.getProperty("password");
			String driver = prop.getProperty("driver");
			int pool_max_size = Integer.parseInt(prop.getProperty("pool_max_size"));
			int pool_min_size = Integer.parseInt(prop.getProperty("pool_min_size"));
			ConnectionPool2.setUrl(url);
			ConnectionPool2.setUsername(username);
			ConnectionPool2.setPassword(password);
			ConnectionPool2.setDriver(driver);
			System.out.println("assdd"+pool_min_size);
			ConnectionPool2.setPool_max_size(pool_max_size);
			ConnectionPool2.setPool_min_size(pool_min_size);
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
			System.out.println("��ȡ�����ļ�����");
		} catch (NumberFormatException e){
			e.printStackTrace();
			System.out.println("����ת������");
			System.exit(0);
		}
		
	}
	private static ConnectionPool2 cp = ConnectionPool2.getInstance();
	private static ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection> ();
//	/**
//	 * ��ȡ���ݿ�����  û����ThreadLocal֮ǰ
//	 * @return 
//	 * Connection
//	 */
//	public static Connection getConn(){
//		Connection conn = cp.getConn();
//		return conn;
//	}
	/**
	 * ��ȡ���ݿ�����   ��ThreadLocal֮��  Ҫ�õ�����ֻ�����̱߳�������
	 * @return 
	 * Connection
	 */
	public static Connection getConn(){
		Connection conn = threadLocal.get();
		if(conn == null){
			conn = cp.getConn();
			threadLocal.set(conn);
		}
		return conn;
	}
	
	/*
	 * ��ȡPreparedStatement    
	 * Override
	 */
	public static PreparedStatement getStmt(Connection conn, String sql){
		PreparedStatement pstmt = null;
		if(conn != null){
			try {
				pstmt = conn.prepareStatement(sql);
			} catch (SQLException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
		return pstmt;
	}
	/*
	 * ��ȡPreparedStatement   �Զ����ɵ�key  
	 * Override
	 */
	public static PreparedStatement getStmt(Connection conn, String sql, int autoGeneratedKey){
		PreparedStatement pstmt = null;
		if(conn != null){
			try {
				pstmt = conn.prepareStatement(sql, autoGeneratedKey);
			} catch (SQLException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
		return pstmt;
	}
	/* Override
	 */
	public static Statement getStmt(Connection conn){
		Statement stmt = null;
		if(conn != null){
			try {
				stmt = conn.createStatement();
			} catch (SQLException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
		return stmt;
	}
	/*
	 * ��ѯ
	 */
	public static ResultSet exeQuery(PreparedStatement stmt){
		ResultSet rs = null;
		if(stmt != null){
			try {
				rs = stmt.executeQuery();
			} catch (SQLException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
		return rs;
	}
	/*
	 * ��ѯ
	 */
	public static ResultSet exeQuery (Statement stmt, String sql){
		ResultSet rs = null;
		if(stmt != null){
			try {
				rs = stmt.executeQuery(sql);
			} catch (SQLException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
		return rs;
	}
	/*
	 * ���ܲ�ѯ,���û�ȡ���ӣ����û�ȡ��䣬�����ֶ��ر�   
	 */
	public static Object exeQuery (String sql,Object params[],IResultSetHandler rsh){
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			conn = getConn();
			pstmt = conn.prepareStatement(sql);
			for(int i=0; i<params.length; i++){
				pstmt.setObject(i+1, params[i]);/*ע��  i+1*/
			}
			rs = pstmt.executeQuery();
			
			return rsh.handler(rs);	//����rsh����
			
		} catch(SQLException e){
			e.printStackTrace();
		} finally{
			close(pstmt);  
			close();
		}
		return null;
	}
	
	public static int exeUpdate(PreparedStatement pstmt){
		int affectRow = 0;
		if(pstmt != null){
			try {
				affectRow = pstmt.executeUpdate();
			} catch (SQLException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
		return affectRow;
	}
	
//	/*
//	 * �ر���Դ
//	 */
//	public static void close(Connection conn){
//		if(conn != null){
//			cp.close(conn);
//		}
//	}
	/*
	 *�ر����Ӳ��ô���conn�ˣ�Ҫ����Ҳֻ�ܴ�threadLocal��conn 
	 */
	public static void close(){
		Connection conn = threadLocal.get();
		if(conn != null){
			cp.close(conn);
			//�����ǰ�߳��ϰ�conn
			threadLocal.remove();
		}
	}
	public static void close (Statement stmt){
		if(stmt != null){
			try {
				stmt.close();
				stmt = null;
			} catch (SQLException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
		
	}

	public static void close(ResultSet rs) {
		// TODO �Զ����ɵķ������
		if(rs != null){
			try {
				rs.close();
				rs = null;
			} catch (SQLException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
	}
	
}