package foundIT.demo.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import jakarta.validation.constraints.NotBlank;



@Document(collection = "foundItems")
public class foundItem {
    
    @Id private String id;

    @NotBlank(message = "Item id is Required")
    private String itemId;

    @NotBlank(message = "First Name is Required")
    private String firstName;

    @NotBlank(message = "Last Name is Required")
    private String lastName;


    //@NotBlank(message = "User Type is Required")
    //@NotEmpty(message = "isAppUser must not be empty")
    private boolean isAppUser;



    // THROW SOMETHING IN FOUNDITEM SERVICE

    private String appId;

    private String phoneNum;
    
    private String email;

    @Indexed(expired = "2m")
    private Instant date;



    public foundItem()
    {
    }

    public foundItem(String itemId, String firstName, String lastName, boolean isAppUser, String appId, String phoneNum, String email)
    {
        this.itemId = itemId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isAppUser = isAppUser;
        this.appId = appId;
        this.phoneNum = phoneNum;
        this.email = email;
        this.date = Instant.now();
    }

    public String getId()
    {
        return this.id;
    }
    public void setId(String id)
    {
        this.id = id;
    }

    public String getItemId()
    {
        return this.itemId;
    }
    public void setItemId(String itemId)
    {
        this.itemId = itemId;
    }

    public String getFirstName()
    {
        return this.firstName;
    }
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return this.lastName;
    }
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public boolean getIsAppUser()
    {
        return this.isAppUser;
    }
    public void setIsAppUser(boolean isAppUser)
    {
        this.isAppUser = isAppUser;
    }

    public String getAppId()
    {
        return this.appId;
    }
    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getPhoneNum()
    {
        return this.phoneNum;
    }
    public void setPhoneNum(String phoneNum)
    {
        this.phoneNum = phoneNum;
    }

    public String getEmail()
    {
        return this.email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }

    public Instant getDate()
    {
        return this.date;
    }
    public void setDate(Instant date)
    {
        this.date = date;
    }

}
