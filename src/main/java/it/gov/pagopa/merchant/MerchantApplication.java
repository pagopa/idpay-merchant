package it.gov.pagopa.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "it.gov.pagopa")
public class MerchantApplication {

  public static void main(String[] args) {
    SpringApplication.run(MerchantApplication.class, args);
  }

}
