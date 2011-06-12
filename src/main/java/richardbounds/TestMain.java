package richardbounds;

import java.io.IOException;
import java.util.Arrays;

public class TestMain {

	public static void main(String[] args) throws IOException {
		new JolokiaServer(8000, new ChartProvider() {
			
			public Iterable<JMXChart> getCharts() {
				return Arrays.asList(
						new JMXChart("java.lang:type=Memory",          "HeapMemoryUsage",  "used"),
						new JMXChart("java.lang:type=OperatingSystem", "SystemLoadAverage", null ),
						new JMXChart("java.lang:type=Memory",          "HeapMemoryUsage",  "used"),
						new JMXChart("java.lang:type=OperatingSystem", "SystemLoadAverage", null )
						);
			}
		}).start();
	}

}
