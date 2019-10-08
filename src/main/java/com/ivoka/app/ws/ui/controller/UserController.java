package com.ivoka.app.ws.ui.controller;

import com.ivoka.app.ws.exceptions.UserServiceException;
import com.ivoka.app.ws.service.AddressService;
import com.ivoka.app.ws.service.UserService;
import com.ivoka.app.ws.shared.dto.AddressDto;
import com.ivoka.app.ws.shared.dto.UserDto;
import com.ivoka.app.ws.ui.model.request.UserDetailsRequestModel;
import com.ivoka.app.ws.ui.model.response.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users") // http://localhost:8080/users
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AddressService addressService;

    @GetMapping(path="/{id}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest getUser(@PathVariable String id){
        UserRest returnValue;

        UserDto userDto = userService.getUserByUserId(id);

        ModelMapper modelMapper = new ModelMapper();
        returnValue = modelMapper.map(userDto, UserRest.class);

        return returnValue;
    }

    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
            )
    public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws UserServiceException {
        UserRest returnValue;

        if(userDetails.getFirstName().isEmpty()) throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        ModelMapper modelMapper = new ModelMapper();
        UserDto userDto = modelMapper.map(userDetails, UserDto.class);

        UserDto createdUser = userService.createUser(userDto);
        returnValue = modelMapper.map(createdUser, UserRest.class);

        return returnValue;
    }

    @PutMapping(path = {"/{id}"},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails){
        UserRest returnValue;

        if(userDetails.getFirstName().isEmpty()) throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        ModelMapper modelMapper = new ModelMapper();

        UserDto userDto;
        userDto = modelMapper.map(userDetails, UserDto.class);

        UserDto updatedUser = userService.updateUser(id, userDto);
        returnValue = modelMapper.map(updatedUser, UserRest.class);

        return returnValue;
    }

    @DeleteMapping(path = {"/{id}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public OperationStatusModel deleteUser(@PathVariable String id){
        OperationStatusModel returnValue = new OperationStatusModel();

        userService.deleteUser(id);

        returnValue.setOperationName(RequestOperationName.DELETE.name());
        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

        return returnValue;
    }

    @GetMapping(
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "1") int page,  @RequestParam(value = "limit", defaultValue = "1") int limit) {

        List<UserRest> returnValue = new ArrayList<>();

        List<UserDto> users = userService.getUsers(page, limit);

        for(UserDto userDto : users) {
            returnValue.add(new ModelMapper().map(userDto, UserRest.class));
        }

        return returnValue;
    }

    @GetMapping(path = "/{id}/addresses",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, "application/hal+json"}
    )
    public Resources<AddressesRest> getUserAddresses(@PathVariable String id) {
        List<AddressesRest> addressesListRestModel = new ArrayList<>();

        List<AddressDto> addressesDto = addressService.getAddresses(id);

        if(addressesDto != null && !addressesDto.isEmpty()) {
            java.lang.reflect.Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
            addressesListRestModel = new ModelMapper().map(addressesDto, listType);

            for (AddressesRest addressRest : addressesListRestModel) {
                Link addressLink = linkTo(methodOn(UserController.class).getUserAddress(id, addressRest.getAddressId())).withRel("address");
                addressRest.add(addressLink);

                Link userLink = linkTo(methodOn(UserController.class).getUser(id)).withRel("user");
                addressRest.add(userLink);

            }
        }
        return new Resources<>(addressesListRestModel);
    }

    @GetMapping(path = "/{userId}/addresses/{addressId}",
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, "application/hal+json" })
    public Resource<AddressesRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId) {

        AddressDto addressesDto = addressService.getAddress(addressId);

        ModelMapper modelMapper = new ModelMapper();

        Link addressLink = linkTo(methodOn(UserController.class).getUserAddress(userId, addressId))
                .withSelfRel();

        Link userLink = linkTo(UserController.class)
                .slash(userId)
                .withRel("user");

        Link addressesLink = linkTo(methodOn(UserController.class).getUserAddresses(userId))
                .withRel("addresses");

        AddressesRest addressesRestModel = modelMapper.map(addressesDto, AddressesRest.class);

        addressesRestModel.add(addressLink);
        addressesRestModel.add(userLink);
        addressesRestModel.add(addressesLink);

        return new Resource<>(addressesRestModel);
    }

}
