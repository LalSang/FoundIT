package foundIT.demo.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;



@Document(collection = "items")
public class item {
    
    @Id private String id;

    @NotBlank(message = "Admin Id is Required")
    private String adminId;

    @NotBlank(message = "Item Type is Required")
    private String itemType;

    @NotBlank(message = "Description is Required")
    private String description;

    @NotBlank(message = "Location is Required")
    private String location;

    @NotBlank(message = "Date is Required")
    private String date;


    

}
