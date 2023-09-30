import com.intellij.rt.coverage.report.api.Filters;
import com.intellij.rt.coverage.report.api.ReportApi;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ReportGenerator {
    public static void main(String[] argv) throws IOException {
        File htmlPath = new File("report"); // path to the output HTML directory
        String title = "Code Coverage Report"; // title of the HTML document
        File icPath = new File("src/report/report.ic"); // path to the report.ic
        List<File> reports = Collections.singletonList(icPath);
        List<File> outputRoots = Collections.singletonList(new File("out/production" +
                "/Java_Server/"));
        List<File> sourceRoots = Collections.singletonList(new File("src/src" +
                "/Java_Server"));
        Filters filters = Filters.EMPTY;

        ReportApi.htmlReport(htmlPath, title, null, reports, outputRoots,
                sourceRoots, filters);

    }
}