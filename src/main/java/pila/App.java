package pila;

//библиотеки парсера
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//библиотеки для паботы с I/O
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
//библиотеки для работы с SQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
        
public class  App
{
    
    //Базовые настройки SQL
private static final String SQL_ADDR = "jdbc:postgresql://localhost:5433/123";
private static final String SQL_LOGIN = "postgres";
private static final String SQL_PASSWORD = "1234";


    public static void main( String[] args ) throws ParserConfigurationException, SAXException, IOException
    {
        //Начало программы, сначала спрашиваем путь до файла
        //Чтобы не открывать сканнер много раз, откроем его один раз, а закроем после успешного поиска файла
        Scanner in = new Scanner(System.in);
        File file_to_db;
        while (true)
        {
            //Код ожидания верного ввода названия файла
            System.out.println("Укажите путь до файла (XML):");
            //Получаем строку
            String path = in.nextLine();
            //Проверяем существет ли файл вообще
            file_to_db = new File(path);
            if(file_to_db.exists())
            {
                //Проверяем, что файл является файлом с расширением xml
                if (path.endsWith(".xml")) break; //Если всё хорошо, выходим из бескеонечного цикла и продолжаем работу программы
            }
        }
        in.close(); //Закрываем опрос пути
        //Начинаем работать с ДБ
        try
        {
        //Сначала пытаемся подключится к БД
        Connection c = DriverManager.getConnection(SQL_ADDR, SQL_LOGIN, SQL_PASSWORD);
        //Получаем инструкцию для работы с БД pSQL
        Statement statement = c.createStatement();
        
        //Инициализщируем сам парсер
        //Создаём экземпляр "фабрики"
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Из фабрики получаем "Строителя"
        DocumentBuilder builder = factory.newDocumentBuilder();
        //После чего "парсим" документ
        Document document = builder.parse(file_to_db);
        //получаем ноду CATALOG
        Element root_catalog = document.getDocumentElement();
        //создаём SQL комманду
        String SQL_COMMAND = "INSERT INTO d_cat_catalog(delivery_date, company, uuid) "
                                       + "VALUES('"+root_catalog.getAttribute("date")+"',"
                                       + "'"+root_catalog.getAttribute("company")+"',"
                                       + "'"+root_catalog.getAttribute("uuid")+"')";
        
        //Вызываем комманду
        statement.executeUpdate(SQL_COMMAND);
        //Теперь создаём комманду SELECT
        SQL_COMMAND = "SELECT * FROM d_cat_catalog WHERE (delivery_date = '"
                + root_catalog.getAttribute("date") + "')AND(company = '"
                 +root_catalog.getAttribute("company") + "')";
        ResultSet catRs = statement.executeQuery(SQL_COMMAND);
        catRs.next();
        //Получаем ID
        int catId = catRs.getInt("id");       
        //В XML документе получаем все записи с "PLANT"
        NodeList plant = root_catalog.getElementsByTagName("PLANT");
        //Перебираем их
        for (int i = 0;i < plant.getLength();i++)
        {
            //Получаем элемент из ноды PLANT
            Element elem = (Element) plant.item(i);
            //Создаём SQL комманду, в неё записываем данные из элементов plant
            SQL_COMMAND = "INSERT INTO f_cat_plants(COMMON, BOTANICAL, ZONE, LIGHT, PRICE, AVAILABILITY, CATALOG_ID) "
                                               + "VALUES("
                                               + "'"+elem.getElementsByTagName("COMMON").item(0).getTextContent()+"',"
                                               + "'"+elem.getElementsByTagName("BOTANICAL").item(0).getTextContent()+"',"
                                               + "'"+elem.getElementsByTagName("ZONE").item(0).getTextContent()+"'," 
                                               + "'"+elem.getElementsByTagName("LIGHT").item(0).getTextContent()+"'," 
                                               + "'"+elem.getElementsByTagName("PRICE").item(0).getTextContent().replace("$","")+"'," 
                                               + "'"+elem.getElementsByTagName("AVAILABILITY").item(0).getTextContent()+"'," //
                                               + "'"+catId+"')";
            //Делаем обращение к БД
            statement.executeUpdate(SQL_COMMAND);
       
        }
        //Закрываем соединение с БД
        c.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    
    }
    
}
