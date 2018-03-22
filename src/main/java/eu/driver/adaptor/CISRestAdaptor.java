package eu.driver.adaptor;

import static springfox.documentation.builders.PathSelectors.regex;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import eu.driver.adaptor.ws.WebSocketServer;
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
	private Boolean restEndpointAvailable = false;
	private Boolean wsEndpointAvailable = false;
	
	private WebSocketServer wsServer = null;
	
	public CISRestAdaptor() throws Exception {
		log.info("Init. CISRestAdaptor");
	}
	
	public static void main(String[] args) throws Exception {
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
