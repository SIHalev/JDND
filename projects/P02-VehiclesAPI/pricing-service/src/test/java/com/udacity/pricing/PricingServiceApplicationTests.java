package com.udacity.pricing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.udacity.pricing.domain.price.Price;
import com.udacity.pricing.service.PriceException;
import com.udacity.pricing.service.PricingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PricingServiceApplicationTests {

  @Test
  public void contextLoads() {
  }

  @Test
  public void checkIfThereIsPriceForTheFirstCar() throws PriceException {
    final Price price = PricingService.getPrice(1L);
    assertNotNull(price.getPrice());
  }

  @Test(expected = PriceException.class)
  public void zeroIdCarShouldBeThrowException() throws PriceException {
    PricingService.getPrice(0L);
    fail();
  }

}
