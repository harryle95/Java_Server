import com.intellij.rt.coverage.report.api.Filters;
import com.intellij.rt.coverage.report.api.ReportApi;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ReportGenerator {
    public static File xmlPath = new File("report/coverage.xml");
    public static List<File> reports = Collections.singletonList(new File("src/report" +
            "/report.ic"));
    public static List<File> outputRoots = Collections.singletonList(new File("out" +
            "/production" +
            "/Java_Server/"));
    public static List<File> sourceRoots = Collections.singletonList(new File("src" +
            "/src" +
            "/Java_Server"));
    public static Filters filters = new Filters(
            Collections.<Pattern>emptyList(),
            Collections.<Pattern>emptyList(),
            Collections.<Pattern>singletonList(
                    Pattern.compile("Coverage.IgnoreCoverage")));
    ;
    public static Document doc;
    public static NodeList nList;

    public static void generateReports() throws IOException {
        ReportApi.xmlReport(xmlPath, reports, outputRoots, sourceRoots, filters);
        ReportApi.htmlReport(new File("report/html/"), "Coverage Report", null, reports, outputRoots, sourceRoots, filters);
    }

    public static void parseCoverageReport() {
        try {
            DocumentBuilder builder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(xmlPath);
            doc.getDocumentElement().normalize();
            nList = doc.getElementsByTagName("counter");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void calculateCoverage(int index) throws IOException {
        Node node = nList.item(index);
        NamedNodeMap attributes = node.getAttributes();
        float missed = Float.parseFloat(attributes.getNamedItem(
                "missed").getNodeValue());
        float covered = Float.parseFloat(attributes.getNamedItem(
                "covered").getNodeValue());
        float coverage = (covered / (covered + missed)) * 100;
        String type = attributes.getNamedItem("type").getNodeValue();
        FileWriter writer = new FileWriter("report/" + type);
        writer.write(String.format("%.2f", coverage));
        writer.close();
    }

    public static void main(String[] argv) throws IOException {
        generateReports();
        parseCoverageReport();
        calculateCoverage(nList.getLength() - 1);
        calculateCoverage(nList.getLength() - 2);
        calculateCoverage(nList.getLength() - 3);
        calculateCoverage(nList.getLength() - 4);
    }
}