package com.ivoka.app.ws.ui.model.response;

import com.ivoka.app.ws.shared.dto.UserDto;
import lombok.Data;
import org.springframework.hateoas.ResourceSupport;

@Data
public class AddressesRest extends ResourceSupport {
    private String addressId;
    private String city;
    private String country;
    private String streetName;
    private String postalCode;
    private String type;
}
