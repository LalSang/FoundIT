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


    public foundItem(String firstName, String lastName, String userType, String appId, String phoneNum, String email)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.appId = appId;
        this.phoneNum = phoneNum;
        this.email = email;
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

    public String getUserType()
    {
        return this.userType;
    }
    public void setUserType(String userType)
    {
        this.userType = userType;
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

}
