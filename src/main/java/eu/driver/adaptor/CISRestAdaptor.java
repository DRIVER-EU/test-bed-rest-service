package eu.driver.adaptor;

import static springfox.documentation.builders.PathSelectors.regex;

import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import eu.driver.adapter.core.CISAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@ComponentScan
@EnableSwagger2
@SpringBootApplication
public class CISRestAdaptor {

	private Logger log = Logger.getLogger(this.getClass());

	public CISRestAdaptor() throws Exception {
		log.info("Init. CISRestAdaptor");
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			System.out.println("CISRestAdapter started with command-line arguments: " + Arrays.toString(args));
			for (String arg : args) {
				if (arg.indexOf("-config") != -1) {
					StringTokenizer tokenizer = new StringTokenizer(arg, "=");
					String token = tokenizer.nextToken();
					CISAdapter.globalConfigPath = tokenizer.nextToken();
				}
			}
			System.out.println("ConfigPath = " + CISAdapter.globalConfigPath);
		}
		SpringApplication.run(CISRestAdaptor.class, args);
    }
	
	@Bean
    public Docket newsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("CISRestAdaptor")
                .apiInfo(apiInfo())
                .select()
                .paths(regex("/CISRestAdaptor.*"))
                .build();
    }
	
	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("CISAdaptor REST Interface API Doc.")
                .description("This is the CISAdaptor REST Interface API Documentation made with Swagger.")
                .version("1.0")
                .build();
    }
}
