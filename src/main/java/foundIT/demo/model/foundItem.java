package foundIT.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;



@Document(collection = "foundItems")
public class foundItem {
    
    @Id private String id;

    @NotBlank(message = "First Name is Required")
    private String firstName;

    @NotBlank(message = "Last Name is Required")
    private String lastName;


    @NotBlank(message = "User Type is Required")
    private String userType;



    // THROW SOMETHING IN FOUNDITEM SERVICE

    private String appId;

    private String phoneNum;
    
    private String email;



}
