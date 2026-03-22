package foundIT.demo.model;


import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

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

    @Indexed(expireAfter = "2m")
    private Instant date;



    /** Creates an empty claim record for framework binding. */
    public foundItem()
    {
    }

    /** Builds a new claim record and timestamps it immediately. */
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

    /** Returns the Mongo id for this claim record. */
    public String getId()
    {
        return this.id;
    }

    /** Sets the Mongo id for this claim record. */
    public void setId(String id)
    {
        this.id = id;
    }

    /** Returns the id of the item being claimed. */
    public String getItemId()
    {
        return this.itemId;
    }

    /** Sets the id of the item being claimed. */
    public void setItemId(String itemId)
    {
        this.itemId = itemId;
    }

    /** Returns the claimant's first name. */
    public String getFirstName()
    {
        return this.firstName;
    }

    /** Sets the claimant's first name. */
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    /** Returns the claimant's last name. */
    public String getLastName()
    {
        return this.lastName;
    }

    /** Sets the claimant's last name. */
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    /** Returns whether the claimant is a student/app user. */
    public boolean getIsAppUser()
    {
        return this.isAppUser;
    }

    /** Sets whether the claimant is a student/app user. */
    public void setIsAppUser(boolean isAppUser)
    {
        this.isAppUser = isAppUser;
    }

    /** Returns the student or app id tied to the claim. */
    public String getAppId()
    {
        return this.appId;
    }

    /** Sets the student or app id tied to the claim. */
    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    /** Returns the phone number captured for the claimant. */
    public String getPhoneNum()
    {
        return this.phoneNum;
    }

    /** Sets the phone number captured for the claimant. */
    public void setPhoneNum(String phoneNum)
    {
        this.phoneNum = phoneNum;
    }

    /** Returns the email captured for the claimant. */
    public String getEmail()
    {
        return this.email;
    }

    /** Sets the email captured for the claimant. */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /** Returns when the claim record was created or last updated. */
    public Instant getDate()
    {
        return this.date;
    }

    /** Sets when the claim record was created or last updated. */
    public void setDate(Instant date)
    {
        this.date = date;
    }

}
