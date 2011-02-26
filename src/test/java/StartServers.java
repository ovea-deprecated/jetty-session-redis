import com.ovea.jetty.session.JettyServer;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class StartServers {
    public static void main(String... args) throws Exception {
        JettyServer container1 = new JettyServer("src/test/webapp1");
        JettyServer container2 = new JettyServer("src/test/webapp2");
        container1.start();
        container2.start();
    }
}
