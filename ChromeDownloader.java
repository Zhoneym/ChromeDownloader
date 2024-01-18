import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
public class ChromeDownloader
{
    public static void main(String[] args) throws IOException, URISyntaxException, ParserConfigurationException, SAXException
    {
        String sessionid = UUID.randomUUID().toString().toUpperCase();
        String requestid = UUID.randomUUID().toString().toUpperCase();
	System.out.println("Download tool for Google Chrome offline installation package for x64 architecture Windows");
        System.out.println("sessionid: " + "{" + sessionid + "}");
        System.out.println("requestid: " + "{" + requestid + "}");
        Map < String, Map < String, String >> fetcher = new HashMap < > ();
        fetcher.put("1", Map.of("channel", "x64-stable-multi-chrome", "appid", "{8A69D345-D564-463C-AFF1-A69D9E530F96}"));
        fetcher.put("2", Map.of("channel", "x64-beta-multi-chrome", "appid", "{8A69D345-D564-463C-AFF1-A69D9E530F96}"));
        fetcher.put("3", Map.of("channel", "x64-dev-statsdef_1", "appid", "{8A69D345-D564-463C-AFF1-A69D9E530F96}"));
        fetcher.put("4", Map.of("channel", "x64-canary", "appid", "{4EA16AC7-FD5A-47C3-875B-DBF4A2008C20}"));
        System.out.println("1. Stable");
        System.out.println("2. Beta");
        System.out.println("3. Dev");
        System.out.println("4. Canary");
	System.out.println("Please select a version with an update frequency that suits you:");
        try (Scanner scanner = new Scanner(System.in)) {
            String choice;
            do {
                choice = scanner.nextLine();
                if(!choice.matches("^[1-4]$"))
                {
                    System.out.println("Incorrect input, please enter again:");
                }
            } while (!choice.matches("^[1-4]$"));
            String channel = fetcher.get(choice).get("channel");
            String appid = fetcher.get(choice).get("appid");
            System.out.println("appid: " + appid);
            URI uri = new URI("https://tools.google.com/service/update2");
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Google Update/1.3.32.7;winhttp;cup-ecdsa");
            connection.setRequestProperty("Host", "tools.google.com");
            connection.setDoOutput(true);
            String requestBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><request protocol=\"3.0\" version=\"1.3.23.9\" shell_version=\"1.3.21.103\" ismachine=\"0\" sessionid=\"" + sessionid + "\" installsource=\"ondemandcheckforupdate\" requestid=\"" + requestid + "\" dedup=\"cr\"><hw physmemory=\"1200000\" sse=\"1\" sse2=\"1\" sse3=\"1\" ssse3=\"1\" sse41=\"1\" sse42=\"1\" avx=\"1\"/><os platform=\"win\" version=\"10.0\" arch=\"x64\"/><app appid=\"" + appid + "\" version=\"\" nextversion=\"\" ap=\"" + channel + "\" lang=\"zh-CN\"><updatecheck/></app></request>";
            byte[] byteArray = requestBody.getBytes("UTF-8");
            connection.getOutputStream().write(byteArray);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(connection.getInputStream());
            NodeList urlNodes = doc.getElementsByTagName("url");
            List < String > downloadLinks = new ArrayList < > ();
            for(int i = 0; i < urlNodes.getLength(); i++)
            {
                Element urlElement = (Element) urlNodes.item(i);
                String codebase = urlElement.getAttribute("codebase");
                NodeList packageNodes = doc.getElementsByTagName("package");
                for(int j = 0; j < packageNodes.getLength(); j++)
                {
                    Element packageElement = (Element) packageNodes.item(j);
                    String name = packageElement.getAttribute("name");
                    downloadLinks.add(codebase + name);
                }
            }
            System.out.println("Download links:");
            for(int i = 0; i < downloadLinks.size(); i++)
            {
                System.out.println((i + 1) + ". " + downloadLinks.get(i));
            }
            System.out.println();
            System.out.println("Do you want to download? (yes/y to download, no/n to not download)");
            String downloadChoice;
            do {
                downloadChoice = scanner.nextLine();
                if(!downloadChoice.matches("^(yes|y|no|n)$"))
                {
                    System.out.println("Incorrect input, please enter again:");
                }
            } while (!downloadChoice.matches("^(yes|y|no|n)$"));
            if(downloadChoice.equalsIgnoreCase("yes") || downloadChoice.equalsIgnoreCase("y"))
            {
                System.out.println("Please enter the number of the link you want to download:");
                int linkNumber = -1;
                do {
                    String numberChoice = scanner.nextLine();
                    try
                    {
                        linkNumber = Integer.parseInt(numberChoice);
                        if(linkNumber < 1 || linkNumber > downloadLinks.size())
                        {
                            System.out.println("Incorrect input, please enter again:");
                            continue;
                        }
                    }
                    catch(NumberFormatException e)
                    {
                        System.out.println("Incorrect input, please enter again:");
                        continue;
                    }
                } while (linkNumber < 1 || linkNumber > downloadLinks.size());
                String downloadLink = downloadLinks.get(linkNumber - 1);
                String fileName = downloadLink.substring(downloadLink.lastIndexOf('/') + 1);
                try
                {
                    downloadFile(downloadLink, fileName);
                }
                catch(URISyntaxException e)
                {
                    System.out.println("Invalid URL: " + downloadLink);
                }
            }
        }
    }
    public static void downloadFile(String downloadLink, String fileName) throws IOException, URISyntaxException
    {
        HttpURLConnection downloadConnection = (HttpURLConnection) new URI(downloadLink).toURL().openConnection();
        int fileSize = downloadConnection.getContentLength();
        try(ReadableByteChannel rbc = Channels.newChannel(downloadConnection.getInputStream()); FileOutputStream fos = new FileOutputStream(fileName))
        {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
            long totalBytesRead = 0;
            int bytesRead;
            while((bytesRead = rbc.read(buffer)) != -1)
            {
                buffer.flip();
                fos.getChannel().write(buffer);
                buffer.clear();
                totalBytesRead += bytesRead;
                double progress = (double) totalBytesRead / fileSize * 100;
                System.out.format("Download progress: %.2f %%%n", progress);
            }
        }
        System.out.println("Download complete!");
    }
}