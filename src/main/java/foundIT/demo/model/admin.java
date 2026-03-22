package foundIT.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;



@Document(collection = "admins")
public class admin {
    
    @Id private String id;

   // @NotBlank(message = "First Name is Required")
    private String firstName;

    //@NotBlank(message = "Last Name is Required")
    private String lastName;

    @NotBlank(message = "Username is Required")
    private String username;

    @NotBlank(message = "Password is Required")
    private String password;

    /** Creates an empty admin model for framework binding. */
    public admin()
    {
    }

    /** Builds an admin model from the supplied profile fields. */
    public admin(String username, String password, String firstName, String lastName)
    {
     this.username = username;
     this.password = password;
     this.firstName = firstName;
     this.lastName = lastName;
    }

    /** Returns the admin's username. */
    public String getUsername()
    {
        return this.username;
    }

    /** Returns the Mongo id for this admin. */
    public String getId()
    {
        return this.id;
    }

    /** Sets the admin username. */
    public void setUsername(String un)
    {
        this.username = un;
    }

    /** Sets the admin password value. */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /** Returns the stored password value. */
    public String getPassword ()
    {
        return this.password;
    }

    /** Returns the admin's first name. */
    public String getFirstName ()
    {
        return this.firstName;
    }

    /** Returns the admin's last name. */
    public String getLastName()
    {
        return this.lastName;
    }

    /** Sets the admin's first name. */
    public void setFirstName(String fn)
    {
        this.firstName = fn;
    }

    /** Sets the admin's last name. */
    public void setLastName(String ln)
    {
        this.lastName = ln;
    }
}
