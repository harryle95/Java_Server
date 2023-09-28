//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.ValueSource;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class ContentServerTest {
//    private Path workDir;
//
//    @BeforeEach
//    void initPath() {
//        workDir = Path.of("", "src/test/utility/json/resources");
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {
//            "localhost:8080 firstMissingID.txt",
//            "localhost:8080 oneID.txt",
//            "localhost:8080 sameID_old_F_new_F.txt",
//            "localhost:8080 sameID_old_F_new_T.txt",
//            "localhost:8080 sameID_old_T_new_F.txt",
//            "localhost:8080 sameID_old_T_new_T_old_eq_new.txt",
//            "localhost:8080 sameID_old_T_new_T_old_gt_new.txt",
//            "localhost:8080 sameID_old_T_new_T_old_lt_new.txt",
//            "localhost:8080 secondNoID.txt",
//            "localhost:8080 twoID.txt",
//            "localhost:8080 twoIDNotInOrder.txt",
//    })
//    void testFormatMessage(String args) throws IOException {
//        String[] argv = args.split(" ");
//        String fileName = argv[1];
//        argv[1] = String.valueOf(workDir.resolve(argv[1]));
//        ContentServer client = new ContentServer(argv);
//        Path expFilePath = workDir.resolve("exp" + fileName);
//        String body = String.join("\n", Files.readAllLines(expFilePath));
//        String message = String.format("PUT /%s HTTP/1.1\r\nHost: localhost:8080\r\nAccept: application/json\r\nContent-Type: application/json\r\nContent-Length: %d\r\n\r\n%s ", client.getFileName(), body.length(), body).trim();
//        assertEquals(message, client.formatMessage().toString());
//    }
//
//}