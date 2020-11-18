### 周四作业

#### 写代码实现Spring Bean的装配，方式越多越好（XML、Annotation都可以）,提交到Github。 

Student.java

```java
public class Student {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
```

Teacher.java

```java
public class Teacher {
    private String subject;
    private String id;
    //    依赖Student类
    private Student student;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "subject='" + subject + '\'' +
                ", id='" + id + '\'' +
                ", student=" + student +
                '}';
    }
}
```

方式一 ：原始方式

```xml
    <bean id="student" class="org.github.yibing.spring.ioc.assemble.Student">
        <property name="id" value="1"/>
        <property name="name" value="xiaoming"/>
    </bean>
    <bean id="teacher" class="org.github.yibing.spring.ioc.assemble.Teacher">
        <property name="subject" value="chinese"/>
        <property name="id" value="1"/>
        <property name="student" ref="student"/>
    </bean>
```



方式二：设置autowire属性为byName

```xml
    <bean id="student" class="org.github.yibing.spring.ioc.assemble.Student">
        <property name="id" value="1"/>
        <property name="name" value="xiaoming"/>
    </bean>
    <!--自动装配  byName-->
        <bean id="teacher" class="org.github.yibing.spring.ioc.assemble.Teacher" 				autowire="byName">
            <property name="subject" value="chinese"/>
            <property name="id" value="1"/>
        </bean>
```

方式三：设置autowire属性为byType(同类型如果有多个，就会无法确定注入哪一个)

```xml
    <!--自动装配  byName-->
        <bean id="teacher" class="org.github.yibing.spring.ioc.assemble.Teacher" autowire="byType">
            <property name="subject" value="chinese"/>
            <property name="id" value="1"/>
        </bean>
```

方式四：javaConfig方式，bean的名称默认是方法名

```java
@Configuration
public class AppConfig {

    @Bean
    public Student student() {
        return new Student();
    }

}
```

方式五：以组件的形式装配

Appconfig.java

```java
// 配置扫描路径
@Configuration
@ComponentScan("org.github.yibing.spring.ioc.assemble")
public class AppConfig {

//    @Bean
//    public Student student() {
//        return new Student();
//    }

}
```

Student.java

```java
// 以组件的形式声明
@Component("student")
public class Student {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
```

#### 周六作业

#### 1.给前面课程提供的 Student/Klass/School 实现自动配置和 Starter。

StudentAutoConfiguration.java

```java
@Configuration
@ConditionalOnClass(Student.class)
@EnableConfigurationProperties(StudentProperties.class)
@ConditionalOnProperty(prefix = "example.student", value = "enabled", matchIfMissing = true)
public class StudentAutoConfiguration {

    @Autowired
    private StudentProperties studentProperties;

    @Bean
    @ConditionalOnMissingBean(Student.class)
    public Student student() {
        Student student = new Student();
        student.setId(studentProperties.getId());
        student.setName(studentProperties.getName());
        return student;
    }
}
```

StudentProperties.java

```java
@ConfigurationProperties(prefix = "example.student")
public class StudentProperties {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

spring.factories

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  example.springboot.starter.StudentAutoConfiguration
```

application.properties

```properties
example.student.id=1
example.student.name=张三
```

mvn install 打包

新建一个springboot项目，引入刚刚打包好的依赖

```xml
        <dependency>
            <groupId>example.springboot</groupId>
            <artifactId>example-spring-boot-starter</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```

在启动类中测试：

```java
@SpringBootApplication
public class Springboot01Application implements CommandLineRunner {

    @Autowired
    private Student student;
    public static void main(String[] args) {
        SpringApplication.run(Springboot01Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        student.init();
        System.out.println(student.getId());
    }
}
```

#### 2.研究一下 JDBC 接口和数据库连接池，掌握它们的设计和用法： 

1）使用 JDBC 原生接口，实现数据库的增删改查操作。 

2）使用事务，PrepareStatement 方式，批处理方式，改进上述操作。 

3）配置 Hikari 连接池，改进上述操作。提交代码到 Github。



DBUtils.java

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

public class DBUtils {
    private static final String driver = "com.mysql.jdbc.Driver";
    private static final String url = "jdbc:mysql://localhost:3306/student?&serverTimezone=Asia/Shanghai";
    private static final String username = "root";
    private static final String password = "123456";
    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static Connection getHikariConnection() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        HikariDataSource dataSource = new HikariDataSource(config);
        try {
            connection = dataSource.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection(Connection connection, Statement statement) {
        try {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```

CRUD

JdbcDemo.java

```java
import java.sql.*;
import java.util.ArrayList;

public class JdbcDemo {
    private static Connection connection = null;

    public static void main(String[] args) {

    }

    // 查询
    @Test
    public void queryStudent() {
        String sql = "select * from student";
        connection = DBUtils.getConnection();
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                System.out.println("id = " + resultSet.getString("id"));
                System.out.println("name = " + resultSet.getString("name"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DBUtils.closeConnection(connection, statement, resultSet);
        }
    }
    // 插入
    @Test
    public void insertOne() {
        Student student = new Student(3, "李华");
        Connection connection = DBUtils.getConnection();
        PreparedStatement psmt = null;
        try {
            psmt = connection.prepareStatement("insert into student values(?,?)");
            psmt.setInt(1, student.getId());
            psmt.setString(2, student.getName());
            int i = psmt.executeUpdate();
            if (i > 0) {
                System.out.println("成功----" + i);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DBUtils.closeConnection(connection, psmt);
        }

    }
    // 删除一个
    @Test
    public void deleteOne() {
        Connection connection = DBUtils.getConnection();
        PreparedStatement psmt = null;
        try {
            psmt = connection.prepareStatement("delete from student where id = ?");
            psmt.setInt(1, 3);
            int i = psmt.executeUpdate();
            if (i > 0) {
                System.out.println("删除成功--" + i + "条");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DBUtils.closeConnection(connection, psmt);
        }
    }
    // 修改
    @Test
    public void updateOne() {
        Student student = new Student(1, "小明");
        Connection connection = DBUtils.getConnection();
        PreparedStatement psmt = null;

        try {
            psmt = connection.prepareStatement("update student set name = ? where id = ?");
            int i = psmt.executeUpdate();
            if (i > 0) {
                System.out.println("已修改" + i + "条");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DBUtils.closeConnection(connection, psmt);
        }

    }
    // 批量插入
    @Test
    public void batchInsert() {
        ArrayList<Student> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new Student("student" + i));
        }
        // 使用 JDBC
//        Connection connection = DBUtils.getConnection();
        // 使用 Hikari连接池
        Connection connection = DBUtils.getHikariConnection();
        String sql = "insert into student (name) values (?)";
        PreparedStatement psmt = null;
        try {
            psmt = connection.prepareStatement(sql);
            connection.setAutoCommit(false);
            for (int i = 0; i < list.size(); i++) {
                if (i == 5) {
                    psmt.setString(1, "sssssssssssssssssssssssssssssssssssssssssssssssssssssss");
                } else {
                    psmt.setString(1, list.get(i).getName());
                }
                psmt.executeUpdate();
            }
            connection.commit();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } finally {
            DBUtils.closeConnection(connection, psmt);
        }
    }
}

```

