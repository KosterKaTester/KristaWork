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
        File fileToDB;
        while (true)
        {
            //Код ожидания верного ввода названия файла
            System.out.println("Укажите путь до файла (XML):");
            //Получаем строку
            String path = in.nextLine();
            //Проверяем существет ли файл вообще
            fileToDB = new File(path);
            if(fileToDB.exists())
            {
                //Проверяем, что файл является файлом с расширением xml
                if (path.endsWith(".xml")) 
                {    
                    //Если всё хорошо, выходим из бескеонечного цикла и продолжаем работу программы
                    break; 
                }
            }
        }
        in.close(); //Закрываем опрос пути
        //Начинаем работать с ДБ
        //Сначала пытаемся подключится к БД
        Connection c = DriverManager.getConnection(SQL_ADDR, SQL_LOGIN, SQL_PASSWORD);
        try
        {
        
        
        //Получаем инструкцию для работы с БД pSQL 
        Statement statement = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
        
        //Инициализщируем сам парсер
        //Создаём экземпляр "фабрики"
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Из фабрики получаем "Строителя"
        DocumentBuilder builder = factory.newDocumentBuilder();
        //После чего "парсим" документ
        Document document = builder.parse(fileToDB);
        //получаем ноду CATALOG
        Element rootCatalog = document.getDocumentElement();
        //создаём SQL комманду
        String sqlCommand = "INSERT INTO d_cat_catalog(delivery_date, company, uuid) "
                                       + "VALUES('"+rootCatalog.getAttribute("date")+"',"
                                       + "'"+rootCatalog.getAttribute("company")+"',"
                                       + "'"+rootCatalog.getAttribute("uuid")+"')";
        
        //Вызываем комманду
        statement.executeUpdate(sqlCommand);
        //Теперь создаём комманду SELECT
        sqlCommand = "SELECT * FROM d_cat_catalog WHERE (delivery_date = '"
                + rootCatalog.getAttribute("date") + "')AND(company = '"
                 +rootCatalog.getAttribute("company") + "')";
        ResultSet catRs = statement.executeQuery(sqlCommand);

        catRs.last();
        //Получаем ID
        int catId = catRs.getInt("id");       
        //В XML документе получаем все записи с "PLANT"
        NodeList plant = rootCatalog.getElementsByTagName("PLANT");
        //Перебираем их
        for (int i = 0;i < plant.getLength();i++)
        {
            //Получаем элемент из ноды PLANT
            Element elem = (Element) plant.item(i);
            //Создаём SQL комманду, в неё записываем данные из элементов plant
            sqlCommand = "INSERT INTO f_cat_plants(COMMON, BOTANICAL, ZONE, LIGHT, PRICE, AVAILABILITY, CATALOG_ID) "
                                               + "VALUES("
                                               + "'"+elem.getElementsByTagName("COMMON").item(0).getTextContent()+"',"
                                               + "'"+elem.getElementsByTagName("BOTANICAL").item(0).getTextContent()+"',"
                                               + "'"+elem.getElementsByTagName("ZONE").item(0).getTextContent()+"'," 
                                               + "'"+elem.getElementsByTagName("LIGHT").item(0).getTextContent()+"'," 
                                               + "'"+elem.getElementsByTagName("PRICE").item(0).getTextContent().replace("$","")+"'," 
                                               + "'"+elem.getElementsByTagName("AVAILABILITY").item(0).getTextContent()+"'," //
                                               + "'"+catId+"')";
            //Делаем обращение к БД
            statement.executeUpdate(sqlCommand);
       
        }
        
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            //Закрываем соединение с БД
            c.close();
        }
    }
    
}
