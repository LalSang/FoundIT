package foundIT.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;



@Document(collection = "admins")
public class admin {
    
    @Id private String id;

    @NotBlank(message = "First Name is Required")
    private String firstName;

    @NotBlank(message = "Last Name is Required")
    private String lastName;

    @NotBlank(message = "Username is Required")
    private String username;

    @NotBlank(message = "Password is Required")
    private String password;


    public admin(String username, String password, String firstName, String lastName)
    {
     this.username = username;
     this.password = password;
     this.firstName = firstName;
     this.lastName = lastName;
    }

    public String getUsername()
    {
        return this.username;
    }

    public void setUsername(String un)
    {
        this.username = un;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPW ()
    {
        return this.password;
    }

    public String getFirstName ()
    {
        return this.firstName;
    }

    public String lastName()
    {
        return this.lastName;
    }

    public void setFN(String fn)
    {
        this.firstName = fn;
    }

    public void setLN(String ln)
    {
        this.lastName = ln;
    }
}

