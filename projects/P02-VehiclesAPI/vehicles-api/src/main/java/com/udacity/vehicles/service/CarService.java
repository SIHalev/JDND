package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.Address;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final WebClient mapsWebClient;
    private final WebClient pricingWebClient;

    public CarService(CarRepository repository,
        @Qualifier("maps") WebClient mapsWebClient,
        @Qualifier("pricing") WebClient pricingWebClient) {
        this.repository = repository;
        this.mapsWebClient = mapsWebClient;
        this.pricingWebClient = pricingWebClient;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        Car car = repository.findById(id)
            .orElseThrow(() -> new CarNotFoundException(String.format("Car with id %s is missing", id)));

        final Mono<ClientResponse> exchangePrice = pricingWebClient.get()
            .uri(uriBuilder -> uriBuilder.path("/services/price")
                .queryParam("vehicleId", id)
                .build())
            .exchange();

        final Location location = car.getLocation();
        final Mono<ClientResponse> exchangeLocation = mapsWebClient.get()
            .uri(uriBuilder -> uriBuilder.path("/maps")
                .queryParam("lat", location.getLat())
                .queryParam("lon", location.getLon())
                .build())
            .exchange();

        return Mono.zip(exchangePrice, exchangeLocation).map(tuple -> {
            final Price price = tuple.getT1().bodyToMono(Price.class).block();
            final Address address = tuple.getT2().bodyToMono(Address.class).block();

            location.setAddress(address.getAddress());
            location.setCity(address.getCity());
            location.setState(address.getState());
            location.setZip(address.getZip());
            car.setLocation(location);

            car.setPrice(price.getPrice().toPlainString());

            return car;
        }).block();
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        repository.findById(id).ifPresent(repository::delete);
        throw new CarNotFoundException(String.format("Car with id %s is missing", id));
    }
}
